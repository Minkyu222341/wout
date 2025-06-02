package com.wout.outfit.service

import com.wout.common.exception.ApiException
import com.wout.common.exception.ErrorCode.*
import com.wout.member.entity.Member
import com.wout.member.entity.WeatherPreference
import com.wout.member.repository.MemberRepository
import com.wout.member.repository.WeatherPreferenceRepository
import com.wout.outfit.dto.response.OutfitRecommendationResponse
import com.wout.outfit.dto.response.OutfitRecommendationSummary
import com.wout.outfit.entity.OutfitRecommendation
import com.wout.outfit.entity.enums.BottomCategory
import com.wout.outfit.entity.enums.OuterCategory
import com.wout.outfit.entity.enums.TopCategory
import com.wout.outfit.mapper.OutfitRecommendationMapper
import com.wout.outfit.repository.OutfitRecommendationRepository
import com.wout.outfit.util.OutfitItemDatabase
import com.wout.outfit.util.OutfitRecommendationEngine
import com.wout.weather.entity.WeatherData
import com.wout.weather.repository.WeatherDataRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.service
 * fileName       : OutfitRecommendationService
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 비즈니스 로직 (실용적 DDD - Orchestrator 역할)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Service
@Transactional(readOnly = true)
class OutfitRecommendationService(
    private val memberRepository: MemberRepository,
    private val weatherPreferenceRepository: WeatherPreferenceRepository,
    private val weatherDataRepository: WeatherDataRepository,
    private val outfitRecommendationRepository: OutfitRecommendationRepository,
    private val outfitRecommendationEngine: OutfitRecommendationEngine,
    private val outfitItemDatabase: OutfitItemDatabase,
    private val outfitRecommendationMapper: OutfitRecommendationMapper
) {

    /**
     * 개인화된 아웃핏 추천 생성
     */
    @Transactional
    fun generatePersonalizedOutfitRecommendation(
        deviceId: String,
        weatherDataId: Long
    ): OutfitRecommendationResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)
        validateWeatherDataId(weatherDataId)

        // 2) 데이터 조회 (여러 엔티티 조합 - 서비스 책임)
        val member = findMemberByDeviceId(deviceId)
        val weatherPreference = findWeatherPreferenceByMemberId(member.id)
        val weatherData = findWeatherDataById(weatherDataId)

        // 3) 기존 추천이 있는지 확인 (중복 방지)
        val existingRecommendation = outfitRecommendationRepository
            .findLatestByMemberIdAndWeatherDataId(member.id, weatherDataId)

        if (existingRecommendation != null && isRecentRecommendation(existingRecommendation)) {
            return outfitRecommendationMapper.toResponse(existingRecommendation)
        }

        // 4) 새로운 추천 생성 (복잡한 알고리즘 - 서비스 책임)
        val recommendation = generateNewRecommendation(member, weatherPreference, weatherData)

        // 5) 저장 및 응답
        val savedRecommendation = outfitRecommendationRepository.save(recommendation)
        return outfitRecommendationMapper.toResponse(savedRecommendation)
    }

    /**
     * 위치 기반 즉시 추천 (WeatherService와 연동)
     */
    @Transactional
    fun generateInstantRecommendation(
        deviceId: String,
        latitude: Double,
        longitude: Double
    ): OutfitRecommendationResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)
        validateCoordinates(latitude, longitude)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)
        val weatherPreference = findWeatherPreferenceByMemberId(member.id)

        // 3) 가장 가까운 날씨 데이터 찾기 (서비스 책임)
        val nearestWeatherData = findNearestWeatherData(latitude, longitude)

        // 4) 추천 생성
        val recommendation = generateNewRecommendation(member, weatherPreference, nearestWeatherData)

        // 5) 저장 및 응답
        val savedRecommendation = outfitRecommendationRepository.save(recommendation)
        return outfitRecommendationMapper.toResponse(savedRecommendation)
    }

    /**
     * 사용자의 최근 추천 히스토리 조회
     */
    fun getRecommendationHistory(deviceId: String, limit: Int = 10): List<OutfitRecommendationSummary> {
        validateDeviceId(deviceId)

        val member = findMemberByDeviceId(deviceId)
        val recommendations = outfitRecommendationRepository
            .findRecentByMemberId(member.id, limit)

        return outfitRecommendationMapper.toSummaryList(recommendations)
    }

    /**
     * 추천에 대한 만족도 업데이트 (피드백 연동)
     */
    @Transactional
    fun updateRecommendationSatisfaction(
        recommendationId: Long,
        satisfactionScore: Int,
        feedback: String?
    ): OutfitRecommendationResponse {
        // 1) 입력값 검증
        validateSatisfactionScore(satisfactionScore)

        // 2) 추천 조회
        val recommendation = findRecommendationById(recommendationId)

        // 3) 기존 피드백 시스템과 연동 (향후 Feedback 엔티티 활용)
        // TODO: FeedbackService.createOutfitFeedback(recommendationId, satisfactionScore, feedback)

        return outfitRecommendationMapper.toResponse(recommendation)
    }

    // ===== 입력값 검증 메서드들 =====

    private fun validateDeviceId(deviceId: String) {
        if (deviceId.isBlank()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateWeatherDataId(weatherDataId: Long) {
        if (weatherDataId <= 0) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateCoordinates(latitude: Double, longitude: Double) {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateSatisfactionScore(score: Int) {
        if (score !in 1..5) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    // ===== 공통 조회 메서드들 =====

    private fun findMemberByDeviceId(deviceId: String): Member {
        return memberRepository.findByDeviceId(deviceId)
            ?: throw ApiException(MEMBER_NOT_FOUND)
    }

    private fun findWeatherPreferenceByMemberId(memberId: Long): WeatherPreference {
        return weatherPreferenceRepository.findByMemberId(memberId)
            ?: throw ApiException(SENSITIVITY_PROFILE_NOT_FOUND)
    }

    private fun findWeatherDataById(weatherDataId: Long): WeatherData {
        return weatherDataRepository.findById(weatherDataId).orElseThrow {
            ApiException(WEATHER_DATA_NOT_FOUND)
        }
    }

    private fun findRecommendationById(recommendationId: Long): OutfitRecommendation {
        return outfitRecommendationRepository.findById(recommendationId).orElseThrow {
            ApiException(RESOURCE_NOT_FOUND)
        }
    }

    private fun findNearestWeatherData(latitude: Double, longitude: Double): WeatherData {
        // WeatherService의 getCurrentWeatherData와 동일한 로직
        // TODO: WeatherService와 연동하거나 공통 유틸리티로 분리
        return weatherDataRepository.findAll()
            .minByOrNull {
                calculateDistance(latitude, longitude, it.latitude, it.longitude)
            } ?: throw ApiException(WEATHER_DATA_NOT_FOUND)
    }

    // ===== 비즈니스 로직 메서드들 (서비스 책임) =====

    /**
     * 새로운 추천 생성 (복잡한 알고리즘 - 서비스 책임)
     */
    private fun generateNewRecommendation(
        member: Member,
        weatherPreference: WeatherPreference,
        weatherData: WeatherData
    ): OutfitRecommendation {
        // 1) 개인화된 체감온도 계산 (엔티티 도메인 로직 활용)
        val feelsLikeTemperature = weatherPreference.calculateFeelsLikeTemperature(
            actualTemp = weatherData.temperature,
            windSpeed = weatherData.windSpeed,
            humidity = weatherData.humidity.toDouble()
        )

        // 2) 추천 엔진으로 카테고리 결정 (복잡한 알고리즘 - 유틸리티 클래스)
        val categoryRecommendation = outfitRecommendationEngine.determineOptimalCategories(
            feelsLikeTemperature = feelsLikeTemperature,
            weatherData = weatherData,
            weatherPreference = weatherPreference
        )

        // 3) 카테고리별 구체적 아이템 선택 (데이터베이스 조회)
        val topItems = outfitItemDatabase.getTopItems(
            category = categoryRecommendation.topCategory,
            temperature = feelsLikeTemperature,
            preferences = weatherPreference
        )

        val bottomItems = outfitItemDatabase.getBottomItems(
            category = categoryRecommendation.bottomCategory,
            temperature = feelsLikeTemperature,
            preferences = weatherPreference
        )

        val outerItems = categoryRecommendation.outerCategory?.let { category ->
            outfitItemDatabase.getOuterItems(category, feelsLikeTemperature, weatherPreference)
        }

        val accessoryItems = outfitItemDatabase.getAccessoryItems(
            temperature = feelsLikeTemperature,
            weatherData = weatherData,
            preferences = weatherPreference
        )

        // 4) 추천 근거 생성 (메시지 생성 로직)
        val recommendationReason = generateRecommendationReason(
            feelsLikeTemperature, weatherData, weatherPreference
        )

        val personalTip = generatePersonalTip(weatherPreference, weatherData)

        // 5) 날씨 점수 계산 (기존 WeatherScoreCalculator 활용)
        val weatherScore = calculateWeatherScore(weatherData, weatherPreference)

        // 6) 엔티티 생성 (팩토리 메서드 사용)
        return OutfitRecommendation.create(
            memberId = member.id,
            weatherDataId = weatherData.id!!,
            temperature = weatherData.temperature,
            feelsLikeTemperature = feelsLikeTemperature,
            weatherScore = weatherScore,
            topCategory = categoryRecommendation.topCategory,
            topItems = topItems,
            bottomCategory = categoryRecommendation.bottomCategory,
            bottomItems = bottomItems,
            outerCategory = categoryRecommendation.outerCategory,
            outerItems = outerItems,
            accessoryItems = accessoryItems,
            recommendationReason = recommendationReason,
            personalTip = personalTip
        )
    }

    /**
     * 최근 추천인지 확인 (1시간 이내)
     */
    private fun isRecentRecommendation(recommendation: OutfitRecommendation): Boolean {
        val oneHourAgo = LocalDateTime.now().minusHours(1)
        return recommendation.createdAt.isAfter(oneHourAgo)
    }

    /**
     * 추천 근거 메시지 생성
     */
    private fun generateRecommendationReason(
        feelsLikeTemperature: Double,
        weatherData: WeatherData,
        preference: WeatherPreference
    ): String {
        val baseReason = when {
            feelsLikeTemperature <= 5 -> "체감온도가 매우 낮아서 보온이 중요해요"
            feelsLikeTemperature <= 15 -> "쌀쌀한 날씨라 따뜻하게 입는 게 좋아요"
            feelsLikeTemperature <= 25 -> "적당한 온도라 편안한 옷차림이면 충분해요"
            else -> "더운 날씨라 시원하고 통풍이 잘 되는 옷이 좋아요"
        }

        val windFactor = if (weatherData.windSpeed >= 5.0) {
            " 바람이 강해서 체감온도가 더 낮게 느껴질 수 있어요."
        } else ""

        val humidityFactor = if (weatherData.humidity >= 80) {
            " 습도가 높아서 끈적할 수 있어요."
        } else ""

        return baseReason + windFactor + humidityFactor
    }

    /**
     * 개인 맞춤 팁 생성
     */
    private fun generatePersonalTip(
        preference: WeatherPreference,
        weatherData: WeatherData
    ): String? {
        val tips = mutableListOf<String>()

        // 개인 특성 기반 팁
        if (preference.isColdSensitive()) {
            tips.add("평소 추위를 많이 타시니까 한 겹 더 입는 걸 추천해요")
        }

        if (preference.isHeatSensitive()) {
            tips.add("더위를 많이 타시는 편이라 통풍이 잘 되는 소재를 선택하세요")
        }

        // 날씨 조건 기반 팁
        if (weatherData.uvIndex != null && weatherData.uvIndex!! >= 7) {
            tips.add("자외선이 강하니 모자나 선글라스를 챙기세요")
        }

        if (weatherData.pm25 != null && weatherData.pm25!! >= 75) {
            tips.add("미세먼지가 나쁘니 마스크를 착용하세요")
        }

        if (weatherData.rain1h != null && weatherData.rain1h!! > 0) {
            tips.add("비가 올 예정이니 우산이나 우비를 준비하세요")
        }

        return tips.firstOrNull()
    }

    /**
     * 날씨 점수 계산 (기존 WeatherScoreCalculator 연동)
     */
    private fun calculateWeatherScore(
        weatherData: WeatherData,
        preference: WeatherPreference
    ): Int {
        // TODO: WeatherScoreCalculator와 연동
        // 임시로 기본값 반환
        return when {
            weatherData.temperature >= 20 && weatherData.temperature <= 25 -> 85
            weatherData.temperature >= 15 && weatherData.temperature <= 30 -> 75
            else -> 60
        }
    }

    /**
     * 두 지점 간의 거리 계산 (단순 유클리드 거리)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latDiff = lat1 - lat2
        val lonDiff = lon1 - lon2
        return kotlin.math.sqrt(latDiff * latDiff + lonDiff * lonDiff)
    }
}

/**
 * 카테고리 추천 결과 데이터 클래스
 */
data class CategoryRecommendation(
    val topCategory: TopCategory,
    val bottomCategory: BottomCategory,
    val outerCategory: OuterCategory?
)