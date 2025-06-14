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
import kotlin.math.max
import kotlin.math.min

/**
 * packageName    : com.wout.feedback.service
 * fileName       : FeedbackService
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 서비스 (학습률 0.22 & 8개/일 정책 반영)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       MVP 최초
 * 2025-06-08        MinKyu Park       빠른 학습(3~7일)용 리튠:
 *                                    • LEARNING_BASE_RATE = 0.22
 *                                    • DAILY_LIMIT = 8
 *                                    • 신뢰도 0.5 미만 → 미세 학습(0.05)
 *                                    • 보정 캡: 가중치 ±2, 쾌적온도 ±1℃
 */
@Service
@Transactional(readOnly = true)
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val memberRepository: MemberRepository,
    private val weatherPreferenceRepository: WeatherPreferenceRepository,
    private val weatherDataRepository: WeatherDataRepository,
    private val weatherScoreCalculator: WeatherScoreCalculator,
    private val feedbackMapper: FeedbackMapper
) {

    companion object {
        private const val MAX_DAILY_FEEDBACKS = 8               // 🔹 일일 제출 한도
        private const val STATISTICS_DAYS = 30
        private const val LEARNING_BASE_RATE = 0.22             // 🔹 기본 학습률
        private const val MIN_RELIABILITY = 0.5                 // 🔹 신뢰도 하한
        private const val SMALL_LEARNING_RATE = 0.05            // 🔹 저신뢰도 학습률
        private const val MAX_TEMP_ADJUST = 1                   // 🔹 쾌적온도 ±1℃
        private const val MAX_WEIGHT_ADJUST = 2                 // 🔹 가중치 ±2
        private const val MIN_WEIGHT = 25
        private const val MAX_WEIGHT = 75
    }

    /* ======================== MVP 핵심 기능 ======================== */

    /** 피드백 제출 */
    @Transactional
    fun submitFeedback(deviceId: String, request: FeedbackSubmitRequest): FeedbackResponse {
        validateDeviceId(deviceId)
        validateFeedbackRequest(request)

        val member = findMemberByDeviceId(deviceId)
        val weatherData = findWeatherDataById(request.weatherDataId)
        val preference = findWeatherPreferenceByMemberId(member.id)

        validateDailyFeedbackLimit(member.id)
        validateDuplicateFeedback(member.id, request.weatherDataId)

        val feedback = createFeedback(member, weatherData, preference, request)
        val saved = feedbackRepository.save(feedback)
        applyImmediateLearning(preference, saved)

        return feedbackMapper.toResponse(saved)
    }

    /** 피드백 히스토리 */
    fun getFeedbackHistory(deviceId: String, pageable: Pageable): FeedbackHistoryResponse {
        val member = findMemberByDeviceId(deviceId)
        return feedbackMapper.toHistoryResponse(
            feedbackRepository.findByMemberIdOrderByCreatedAtDesc(member.id, pageable)
        )
    }

    /** 30일 통계 */
    fun getFeedbackStatistics(deviceId: String): FeedbackStatisticsResponse {
        val member = findMemberByDeviceId(deviceId)
        val list = feedbackRepository.findRecentFeedbacks(member.id, STATISTICS_DAYS)
        return feedbackMapper.toStatisticsResponse(list, STATISTICS_DAYS)
    }

    /** 오늘 가능 여부 */
    fun canSubmitFeedbackToday(deviceId: String): Map<String, Any> {
        val member = findMemberByDeviceId(deviceId)
        val todayCnt = getTodayFeedbackCount(member.id)
        return mapOf(
            "canSubmit" to (todayCnt < MAX_DAILY_FEEDBACKS),
            "remainingCount" to max(0, MAX_DAILY_FEEDBACKS - todayCnt),
            "maxDailyLimit" to MAX_DAILY_FEEDBACKS,
            "todaySubmittedCount" to todayCnt
        )
    }

    /* ==================== Validation & Query Utils ==================== */

    private fun validateDeviceId(id: String) { if (id.isBlank()) throw ApiException(INVALID_INPUT_VALUE) }

    private fun validateFeedbackRequest(r: FeedbackSubmitRequest) {
        try { FeedbackType.fromString(r.feedbackType) } catch (_: IllegalArgumentException) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateDailyFeedbackLimit(memberId: Long) {
        if (getTodayFeedbackCount(memberId) >= MAX_DAILY_FEEDBACKS) throw ApiException(FEEDBACK_LIMIT_EXCEEDED)
    }

    private fun validateDuplicateFeedback(memberId: Long, weatherDataId: Long) {
        if (feedbackRepository.existsByMemberIdAndWeatherDataId(memberId, weatherDataId))
            throw ApiException(DUPLICATE_FEEDBACK)
    }

    private fun getTodayFeedbackCount(memberId: Long): Int {
        val now = LocalDateTime.now()
        val start = now.toLocalDate().atStartOfDay()
        val end = start.plusDays(1).minusNanos(1)
        return feedbackRepository.countTodayFeedbacks(memberId, start, end).toInt()
    }

    private fun findMemberByDeviceId(id: String): Member =
        memberRepository.findByDeviceId(id) ?: throw ApiException(MEMBER_NOT_FOUND)

    private fun findWeatherPreferenceByMemberId(id: Long): WeatherPreference =
        weatherPreferenceRepository.findByMemberId(id) ?: throw ApiException(SENSITIVITY_PROFILE_NOT_FOUND)

    private fun findWeatherDataById(id: Long): WeatherData =
        weatherDataRepository.findById(id).orElseThrow { ApiException(WEATHER_DATA_NOT_FOUND) }

    /* =========================== Core =========================== */

    private fun createFeedback(
        member: Member,
        data: WeatherData,
        pref: WeatherPreference,
        req: FeedbackSubmitRequest
    ): Feedback {
        val feels = pref.calculateFeelsLikeTemperature(data.temperature, data.windSpeed, data.humidity.toDouble())
        val score = weatherScoreCalculator.calculateTotalScore(
            data.temperature, data.humidity.toDouble(), data.windSpeed,
            data.uvIndex ?: 0.0, data.pm25 ?: 0.0, data.pm10 ?: 0.0, pref
        ).totalScore.toInt()

        return Feedback.create(
            memberId = member.id,
            weatherDataId = req.weatherDataId,
            feedbackType = FeedbackType.fromString(req.feedbackType),
            actualTemperature = data.temperature,
            feelsLikeTemperature = feels,
            weatherScore = score,
            previousComfortTemp = pref.comfortTemperature,
            comments = req.comments,
            isConfirmed = req.isConfirmed
        )
    }

    /** 빠른 학습 적용 */
    @Transactional
    fun applyImmediateLearning(pref: WeatherPreference, fb: Feedback) {
        if (!fb.needsTemperatureAdjustment()) return  // PERFECT

        val reliability = fb.calculateReliabilityScore()
        val baseRate = if (reliability < MIN_RELIABILITY) SMALL_LEARNING_RATE else LEARNING_BASE_RATE
        val learningRate = reliability * baseRate
        if (learningRate < 0.01) return     // 무시할 정도로 낮음

        /* ---- 온도 조정 ---- */
        val tempDeltaRaw = fb.adjustmentAmount * learningRate
        val tempDelta = when {
            tempDeltaRaw > 0 -> min(tempDeltaRaw, MAX_TEMP_ADJUST.toDouble())
            tempDeltaRaw < 0 -> max(tempDeltaRaw, -MAX_TEMP_ADJUST.toDouble())
            else -> 0.0
        }.toInt()

        /* ---- 가중치 조정 (온도가중치 only) ---- */
        val weightDelta = if (tempDelta == 0) 0 else if (tempDelta > 0) MAX_WEIGHT_ADJUST else -MAX_WEIGHT_ADJUST
        val newWeight = (pref.temperatureWeight + weightDelta).coerceIn(MIN_WEIGHT, MAX_WEIGHT)
        val newComfortTemp = (pref.comfortTemperature + tempDelta).coerceIn(10, 30)

        pref.updatePreferences(
            comfortTemperature = newComfortTemp,
            temperatureWeight = newWeight
        )

    }
}
