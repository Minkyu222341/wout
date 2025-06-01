package com.wout.feedback.service

import com.wout.common.exception.ApiException
import com.wout.common.exception.ErrorCode.*
import com.wout.feedback.dto.request.FeedbackSubmitRequest
import com.wout.feedback.dto.response.FeedbackHistoryResponse
import com.wout.feedback.dto.response.FeedbackResponse
import com.wout.feedback.dto.response.FeedbackStatisticsResponse
import com.wout.feedback.entity.Feedback
import com.wout.feedback.entity.FeedbackType
import com.wout.feedback.mapper.FeedbackMapper
import com.wout.feedback.repository.FeedbackRepository
import com.wout.member.entity.Member
import com.wout.member.entity.WeatherPreference
import com.wout.member.repository.MemberRepository
import com.wout.member.repository.WeatherPreferenceRepository
import com.wout.member.util.WeatherScoreCalculator
import com.wout.weather.entity.WeatherData
import com.wout.weather.repository.WeatherDataRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.service
 * fileName       : FeedbackService
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 서비스 MVP 버전 (핵심 기능만)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성 (MVP 필수 기능만)
 * 2025-06-01        MinKyu Park       가이드 v2.0 적용 (Orchestrator 역할)
 * 2025-06-02        MinKyu Park       코드 리뷰 반영 (에러 코드, 트랜잭션, 학습 로직 수정)
 */
@Service
@Transactional(readOnly = true)
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val memberRepository: MemberRepository,
    private val weatherPreferenceRepository: WeatherPreferenceRepository,
    private val weatherDataRepository: WeatherDataRepository,
    private val weatherScoreCalculator: WeatherScoreCalculator,  // ✅ 추가: 실제 점수 계산용
    private val feedbackMapper: FeedbackMapper
) {

    companion object {
        private const val MAX_DAILY_FEEDBACKS = 10
        private const val STATISTICS_DAYS = 30
    }

    // ===== MVP 핵심 기능들 =====

    /**
     * 피드백 제출 (MVP 핵심 기능)
     */
    @Transactional
    fun submitFeedback(deviceId: String, request: FeedbackSubmitRequest): FeedbackResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)
        validateFeedbackRequest(request)

        // 2) 데이터 조회 (여러 엔티티 조합 - 서비스 책임)
        val member = findMemberByDeviceId(deviceId)
        val weatherData = findWeatherDataById(request.weatherDataId)
        val weatherPreference = findWeatherPreferenceByMemberId(member.id)

        // 3) 비즈니스 규칙 검증
        validateDailyFeedbackLimit(member.id)
        validateDuplicateFeedback(member.id, request.weatherDataId)

        // 4) 피드백 생성 (엔티티 팩토리 메서드 사용)
        val feedback = createFeedback(
            member = member,
            weatherData = weatherData,
            weatherPreference = weatherPreference,
            request = request
        )

        // 5) 저장 및 즉시 학습 적용
        val savedFeedback = feedbackRepository.save(feedback)
        applyImmediateLearning(weatherPreference, savedFeedback) // ✅ 수정: @Transactional 제거로 동일 트랜잭션에서 실행

        return feedbackMapper.toResponse(savedFeedback)
    }

    /**
     * 피드백 히스토리 조회 (MVP 핵심 기능)
     */
    fun getFeedbackHistory(deviceId: String, pageable: Pageable): FeedbackHistoryResponse {
        validateDeviceId(deviceId)

        val member = findMemberByDeviceId(deviceId)
        val feedbackPage = feedbackRepository.findByMemberIdOrderByCreatedAtDesc(member.id, pageable)

        return feedbackMapper.toHistoryResponse(feedbackPage)
    }

    /**
     * 피드백 통계 조회 (MVP 기본 통계)
     */
    fun getFeedbackStatistics(deviceId: String): FeedbackStatisticsResponse {
        validateDeviceId(deviceId)

        val member = findMemberByDeviceId(deviceId)
        val recentFeedbacks = feedbackRepository.findRecentFeedbacks(member.id, STATISTICS_DAYS)

        return feedbackMapper.toStatisticsResponse(recentFeedbacks, STATISTICS_DAYS)
    }

    /**
     * 오늘 피드백 제출 가능 여부 확인 (MVP 유틸리티)
     */
    fun canSubmitFeedbackToday(deviceId: String): Map<String, Any> {
        validateDeviceId(deviceId)

        val member = findMemberByDeviceId(deviceId)
        val todayCount = getTodayFeedbackCount(member.id)

        val canSubmit = todayCount < MAX_DAILY_FEEDBACKS
        val remainingCount = maxOf(0, MAX_DAILY_FEEDBACKS - todayCount)

        return mapOf(
            "canSubmit" to canSubmit,
            "remainingCount" to remainingCount,
            "maxDailyLimit" to MAX_DAILY_FEEDBACKS,
            "todaySubmittedCount" to todayCount
        )
    }

    // ===== 입력값 검증 메서드들 =====

    private fun validateDeviceId(deviceId: String) {
        if (deviceId.isBlank()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateFeedbackRequest(request: FeedbackSubmitRequest) {
        try {
            FeedbackType.fromString(request.feedbackType)
        } catch (_: IllegalArgumentException) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateDailyFeedbackLimit(memberId: Long) {
        val todayCount = getTodayFeedbackCount(memberId)
        if (todayCount >= MAX_DAILY_FEEDBACKS) {
            throw ApiException(FEEDBACK_LIMIT_EXCEEDED)
        }
    }

    private fun validateDuplicateFeedback(memberId: Long, weatherDataId: Long) {
        if (feedbackRepository.existsByMemberIdAndWeatherDataId(memberId, weatherDataId)) {
            throw ApiException(DUPLICATE_FEEDBACK)  // ✅ 수정: INVALID_INPUT_VALUE → DUPLICATE_FEEDBACK
        }
    }

    // ===== 공통 조회 메서드들 =====

    private fun findMemberByDeviceId(deviceId: String): Member {
        return memberRepository.findByDeviceId(deviceId)
            ?: throw ApiException(MEMBER_NOT_FOUND)
    }

    private fun findWeatherDataById(weatherDataId: Long): WeatherData {
        return weatherDataRepository.findById(weatherDataId).orElseThrow {
            ApiException(WEATHER_DATA_NOT_FOUND)
        }
    }

    private fun findWeatherPreferenceByMemberId(memberId: Long): WeatherPreference {
        return weatherPreferenceRepository.findByMemberId(memberId)
            ?: throw ApiException(SENSITIVITY_PROFILE_NOT_FOUND)
    }

    // ===== 비즈니스 로직 메서드들 (서비스 책임) =====

    /**
     * 피드백 생성 (여러 엔티티 조합 필요 - 서비스 책임)
     */
    private fun createFeedback(
        member: Member,
        weatherData: WeatherData,
        weatherPreference: WeatherPreference,
        request: FeedbackSubmitRequest
    ): Feedback {
        // WeatherPreference의 도메인 로직을 활용해서 개인화된 체감온도 계산
        val personalizedFeelsLike = weatherPreference.calculateFeelsLikeTemperature(
            actualTemp = weatherData.temperature,
            windSpeed = weatherData.windSpeed,
            humidity = weatherData.humidity.toDouble()
        )

        // ✅ 수정: 실제 날씨 점수 계산 (하드코딩 85 제거)
        val actualWeatherScore = calculateActualWeatherScore(weatherData, weatherPreference)

        return Feedback.create(
            memberId = member.id,
            weatherDataId = request.weatherDataId,
            feedbackType = FeedbackType.fromString(request.feedbackType),
            actualTemperature = weatherData.temperature,
            feelsLikeTemperature = personalizedFeelsLike,
            weatherScore = actualWeatherScore,  // ✅ 실제 계산된 점수 사용
            previousComfortTemp = weatherPreference.comfortTemperature,
            comments = request.comments,
            isConfirmed = request.isConfirmed
        )
    }

    /**
     * 실제 날씨 점수 계산
     * WeatherScoreCalculator 발생하는 예외는 그대로 전파
     */
    private fun calculateActualWeatherScore(
        weatherData: WeatherData,
        weatherPreference: WeatherPreference
    ): Int {
        val weatherScoreResult = weatherScoreCalculator.calculateTotalScore(
            temperature = weatherData.temperature,
            humidity = weatherData.humidity.toDouble(),
            windSpeed = weatherData.windSpeed,
            uvIndex = weatherData.uvIndex ?: 0.0,
            pm25 = weatherData.pm25 ?: 0.0,
            pm10 = weatherData.pm10 ?: 0.0,
            weatherPreference = weatherPreference
        )
        return weatherScoreResult.totalScore.toInt()
    }

    /**
     * 즉시 학습 적용 (여러 엔티티 조합 필요 - 서비스 책임)
     * ✅ 수정: @Transactional 제거로 중첩 트랜잭션 방지
     */
    fun applyImmediateLearning(preference: WeatherPreference, feedback: Feedback) {
        // 피드백의 신뢰도가 충분한지 확인 (도메인 로직 활용)
        if (!feedback.needsTemperatureAdjustment()) {
            return // 완벽한 피드백은 학습 불필요
        }

        val learningWeight = feedback.getLearningWeight() // 도메인 로직 활용
        if (learningWeight < 0.3) {
            return // 신뢰도가 낮으면 학습 스킵
        }

        val updatedPreference = when {
            feedback.isColdFeedback() -> {
                // 추위 피드백: 쾌적온도를 높여서 더 따뜻해야 쾌적하다고 학습
                val adjustment = (feedback.adjustmentAmount * learningWeight).toInt()
                preference.update(
                    comfortTemperature = (preference.comfortTemperature + adjustment).coerceIn(10, 30),
                    temperatureWeight = (preference.temperatureWeight + 2).coerceIn(30, 70)  // ✅ 수정: 30-70 범위
                )
            }
            feedback.isHotFeedback() -> {
                // 더위 피드백: 쾌적온도를 낮춰서 더 시원해야 쾌적하다고 학습
                val adjustment = (feedback.adjustmentAmount * learningWeight).toInt()
                preference.update(
                    comfortTemperature = (preference.comfortTemperature - adjustment).coerceIn(10, 30),  // ✅ 수정: 빼기로 변경
                    temperatureWeight = (preference.temperatureWeight + 2).coerceIn(30, 70)  // ✅ 수정: 30-70 범위
                )
            }
            else -> preference // 완벽한 피드백은 변경 없음
        }

        weatherPreferenceRepository.save(updatedPreference)
    }

    /**
     * 오늘 피드백 개수 조회 (정확한 날짜 범위)
     */
    private fun getTodayFeedbackCount(memberId: Long): Int {
        val now = LocalDateTime.now()
        val todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
        val todayEnd = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999)

        return feedbackRepository.countTodayFeedbacks(memberId, todayStart, todayEnd).toInt()
    }
}