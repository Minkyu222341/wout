package com.wout.outfit.service

import com.wout.common.exception.ApiException
import com.wout.common.exception.ErrorCode.*
import com.wout.member.entity.Member
import com.wout.member.entity.WeatherPreference
import com.wout.member.repository.MemberRepository
import com.wout.member.repository.WeatherPreferenceRepository
import com.wout.member.util.WeatherScoreCalculator
import com.wout.outfit.dto.response.OutfitRecommendationResponse
import com.wout.outfit.dto.response.OutfitRecommendationSummary
import com.wout.outfit.entity.OutfitRecommendation
import com.wout.outfit.mapper.OutfitRecommendationMapper
import com.wout.outfit.repository.OutfitRecommendationRepository
import com.wout.outfit.util.OutfitRecommendationEngine
import com.wout.weather.entity.WeatherData
import com.wout.weather.repository.WeatherDataRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.*

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
 * 2025-06-03        MinKyu Park       다중 추천 지원 + Engine 완전 활용 + 실제 메서드명 반영
 */
@Service
@Transactional(readOnly = true)
class OutfitRecommendationService(
    private val memberRepository: MemberRepository,
    private val weatherPreferenceRepository: WeatherPreferenceRepository,
    private val weatherDataRepository: WeatherDataRepository,
    private val outfitRecommendationRepository: OutfitRecommendationRepository,
    private val outfitRecommendationEngine: OutfitRecommendationEngine,
    private val outfitRecommendationMapper: OutfitRecommendationMapper,
    private val weatherScoreCalculator: WeatherScoreCalculator
) {

    /**
     * 🆕 개인화된 다중 아웃핏 추천 생성 (스타일별 추천)
     */
    @Transactional
    fun generatePersonalizedOutfitRecommendations(
        deviceId: String,
        weatherDataId: Long
    ): List<OutfitRecommendationResponse> {
        // 1) 입력값 검증
        validateDeviceId(deviceId)
        validateWeatherDataId(weatherDataId)

        // 2) 데이터 조회 (여러 엔티티 조합 - 서비스 책임)
        val member = findMemberByDeviceId(deviceId)
        val weatherPreference = findWeatherPreferenceByMemberId(member.id)
        val weatherData = findWeatherDataById(weatherDataId)

        // 3) 기존 추천이 있는지 확인 (중복 방지 - 1시간 이내)
        val oneHourAgo = LocalDateTime.now().minusHours(1)
        val existingRecommendation = outfitRecommendationRepository
            .findByMemberIdAndWeatherDataIdAndCreatedAtAfter(member.id, weatherDataId, oneHourAgo)

        if (existingRecommendation != null) {
            // 🔧 기존 추천이 있으면 같은 조건의 모든 추천을 반환
            val allRecentRecommendations = outfitRecommendationRepository
                .findByMemberIdAndWeatherDataIdOrderByCreatedAtDesc(member.id, weatherDataId)
                .filter { it.createdAt.isAfter(oneHourAgo) }

            return allRecentRecommendations.map { outfitRecommendationMapper.toResponse(it) }
        }

        // 4) 🔧 실제 WeatherScoreCalculator 사용
        val weatherScoreResult = weatherScoreCalculator.calculateTotalScore(
            temperature = weatherData.temperature,
            humidity = weatherData.humidity.toDouble(),
            windSpeed = weatherData.windSpeed,
            uvIndex = weatherData.uvIndex ?: 0.0,
            pm25 = weatherData.pm25 ?: 0.0,
            pm10 = weatherData.pm10 ?: 0.0,
            weatherPreference = weatherPreference
        )

        // 5) Engine으로 다중 추천 생성
        val recommendations = outfitRecommendationEngine.generateMultipleRecommendations(
            weatherData = weatherData,
            preference = weatherPreference,
            personalScore = weatherScoreResult.totalScore.toInt()
        )

        // 6) ✅ 방안 1: 각 추천을 Entity로 변환 및 저장 → 저장된 Entity를 Response로 변환
        val savedRecommendations = recommendations.map { recommendation ->
            val entity = convertResponseToEntity(
                recommendation = recommendation,
                member = member,
                weatherData = weatherData,
                weatherScore = weatherScoreResult.totalScore.toInt(),
                weatherPreference = weatherPreference
            )
            outfitRecommendationRepository.save(entity)
        }

        // ✅ 저장된 Entity를 Response로 변환하여 반환 (실제 DB ID, 생성시간 포함)
        return savedRecommendations.map { outfitRecommendationMapper.toResponse(it) }
    }

    /**
     * 사용자의 최근 추천 히스토리 조회
     */
    fun getRecommendationHistory(deviceId: String, limit: Int = 10): List<OutfitRecommendationSummary> {
        validateDeviceId(deviceId)

        val member = findMemberByDeviceId(deviceId)
        val recommendations = outfitRecommendationRepository.findRecentByMemberId(member.id, limit)

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

        // 3) TODO: 향후 피드백 시스템과 연동
        // FeedbackService.createOutfitFeedback(recommendationId, satisfactionScore, feedback)

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
    // ===== 비즈니스 로직 메서드들 (서비스 책임) =====

    /**
     * 🆕 OutfitRecommendationResponse를 Entity로 변환
     */
    private fun convertResponseToEntity(
        recommendation: OutfitRecommendationResponse,
        member: Member,
        weatherData: WeatherData,
        weatherScore: Int,
        weatherPreference: WeatherPreference
    ): OutfitRecommendation {
        // 개인화된 체감온도 계산
        val feelsLikeTemperature = weatherPreference.calculateFeelsLikeTemperature(
            actualTemp = weatherData.temperature,
            windSpeed = weatherData.windSpeed,
            humidity = weatherData.humidity.toDouble()
        )

        return OutfitRecommendation.create(
            memberId = member.id,
            weatherDataId = weatherData.id!!,
            temperature = weatherData.temperature,
            feelsLikeTemperature = feelsLikeTemperature,
            weatherScore = weatherScore,
            topCategory = recommendation.topCategory,
            topItems = recommendation.categories.top.items,
            bottomCategory = recommendation.bottomCategory,
            bottomItems = recommendation.categories.bottom.items,
            outerCategory = recommendation.outerCategory,
            outerItems = recommendation.categories.outer.items,
            accessoryItems = recommendation.categories.accessories.items,
            recommendationReason = recommendation.recommendationReason,
            personalTip = recommendation.personalTip
        )
    }

    /**
     * 🔧 Haversine 공식을 사용한 정확한 거리 계산
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // 지구 반지름 (km)

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}