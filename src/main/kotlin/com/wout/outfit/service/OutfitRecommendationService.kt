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
import com.wout.outfit.mapper.OutfitRecommendationMapper
import com.wout.outfit.repository.OutfitRecommendationRepository
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
 * 2025-06-03        MinKyu Park       MVP 범위에 맞게 핵심 기능만 구현
 * 2025-06-03        MinKyu Park       OutfitRecommendationEngine 연동
 */
@Service
@Transactional(readOnly = true)
class OutfitRecommendationService(
    private val memberRepository: MemberRepository,
    private val weatherPreferenceRepository: WeatherPreferenceRepository,
    private val weatherDataRepository: WeatherDataRepository,
    private val outfitRecommendationRepository: OutfitRecommendationRepository,
    private val outfitRecommendationEngine: OutfitRecommendationEngine,
    private val outfitRecommendationMapper: OutfitRecommendationMapper
) {

    /**
     * 개인화된 아웃핏 추천 생성
     *
     * 비즈니스 규칙:
     * - 1시간 이내 동일 날씨 데이터로 추천이 있으면 기존 추천 반환
     * - 새로운 추천 생성 시 Engine에 완전히 위임
     * - 저장 후 실제 Entity 기반 Response 반환
     */
    @Transactional
    fun generatePersonalizedOutfitRecommendation(
        deviceId: String,
        weatherDataId: Long
    ): OutfitRecommendationResponse {

        // 1단계: 입력값 검증 - 간단한 null/blank 체크
        validateDeviceId(deviceId)
        validateWeatherDataId(weatherDataId)

        // 2단계: 데이터 조회 - Repository 통한 엔티티 조회
        val member = findMemberByDeviceId(deviceId)
        val weatherPreference = findWeatherPreferenceByMemberId(member.id)
        val weatherData = findWeatherDataById(weatherDataId)

        // 3단계: 중복 추천 확인 - 1시간 이내 동일 추천 방지
        val oneHourAgo = LocalDateTime.now().minusHours(1)
        val existingRecommendation = outfitRecommendationRepository
            .findByMemberIdAndWeatherDataIdAndCreatedAtAfter(member.id, weatherDataId, oneHourAgo)

        if (existingRecommendation != null) {
            return outfitRecommendationMapper.toResponse(existingRecommendation)
        }

        // 4단계: 비즈니스 로직 - Engine에 완전히 위임 (DDD 원칙)
        val recommendationEntity = outfitRecommendationEngine.generateOutfitRecommendation(
            weatherData = weatherData,
            preferences = weatherPreference,
            memberId = member.id
        )

        // 5단계: 저장 및 응답 - 결과 저장 후 DTO 변환
        val savedRecommendation = outfitRecommendationRepository.save(recommendationEntity)
        return outfitRecommendationMapper.toResponse(savedRecommendation)
    }

    /**
     * 사용자의 최근 추천 히스토리 조회
     *
     * 비즈니스 규칙:
     * - 최신순으로 정렬
     * - 요약 정보만 제공 (상세 정보는 개별 조회)
     * - 기본 10개 제한
     */
    fun getRecommendationHistory(
        deviceId: String,
        limit: Int = 10
    ): List<OutfitRecommendationSummary> {

        validateDeviceId(deviceId)
        validateLimit(limit)

        val member = findMemberByDeviceId(deviceId)
        val recommendations = outfitRecommendationRepository.findRecentByMemberId(member.id, limit)

        return outfitRecommendationMapper.toSummaryList(recommendations)
    }

    /**
     * 추천 상세 정보 조회
     *
     * 비즈니스 규칙:
     * - 본인의 추천만 조회 가능
     * - 존재하지 않는 추천 조회 시 예외 발생
     */
    fun getRecommendationDetail(
        deviceId: String,
        recommendationId: Long
    ): OutfitRecommendationResponse {

        validateDeviceId(deviceId)
        validateRecommendationId(recommendationId)

        val member = findMemberByDeviceId(deviceId)
        val recommendation = findRecommendationById(recommendationId)

        // 본인의 추천인지 확인
        if (recommendation.memberId != member.id) {
            throw ApiException(FORBIDDEN)
        }

        return outfitRecommendationMapper.toResponse(recommendation)
    }

    // ===== 입력값 검증 메서드들 =====

    /**
     * Device ID 검증 - 빈 값 체크
     */
    private fun validateDeviceId(deviceId: String) {
        if (deviceId.isBlank()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    /**
     * WeatherData ID 검증 - 양수 체크
     */
    private fun validateWeatherDataId(weatherDataId: Long) {
        if (weatherDataId <= 0) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    /**
     * Recommendation ID 검증 - 양수 체크
     */
    private fun validateRecommendationId(recommendationId: Long) {
        if (recommendationId <= 0) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    /**
     * Limit 검증 - 1-100 범위 체크
     */
    private fun validateLimit(limit: Int) {
        if (limit !in 1..100) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    // ===== 공통 조회 메서드들 =====

    /**
     * Device ID로 회원 조회 - 존재하지 않으면 예외
     */
    private fun findMemberByDeviceId(deviceId: String): Member {
        return memberRepository.findByDeviceId(deviceId)
            ?: throw ApiException(MEMBER_NOT_FOUND)
    }

    /**
     * Member ID로 날씨 선호도 조회 - 존재하지 않으면 예외
     */
    private fun findWeatherPreferenceByMemberId(memberId: Long): WeatherPreference {
        return weatherPreferenceRepository.findByMemberId(memberId)
            ?: throw ApiException(SENSITIVITY_PROFILE_NOT_FOUND)
    }

    /**
     * WeatherData ID로 날씨 데이터 조회 - 존재하지 않으면 예외
     */
    private fun findWeatherDataById(weatherDataId: Long): WeatherData {
        return weatherDataRepository.findById(weatherDataId).orElseThrow {
            ApiException(WEATHER_DATA_NOT_FOUND)
        }
    }

    /**
     * Recommendation ID로 추천 조회 - 존재하지 않으면 예외
     */
    private fun findRecommendationById(recommendationId: Long): OutfitRecommendation {
        return outfitRecommendationRepository.findById(recommendationId).orElseThrow {
            ApiException(RESOURCE_NOT_FOUND)
        }
    }
}