package com.wout.feedback.entity

import com.wout.common.entity.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * packageName    : com.wout.feedback.entity
 * fileName       : Feedback
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 사용자 피드백 엔티티 (실용적 DDD 적용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       가이드 v2.0 적용 (Entity 중심 도메인 로직)
 */
@Entity
@Comment("사용자 날씨 추천 피드백")
class Feedback private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("피드백 ID")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    @Comment("회원 ID")
    val memberId: Long,

    @Column(name = "weather_data_id", nullable = false)
    @Comment("날씨 데이터 ID")
    val weatherDataId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 20)
    @Comment("피드백 유형")
    private var _feedbackType: FeedbackType,

    @Column(name = "actual_temperature", nullable = false)
    @Comment("실제 기온")
    val actualTemperature: Double,

    @Column(name = "feels_like_temperature", nullable = false)
    @Comment("체감온도 (개인화)")
    val feelsLikeTemperature: Double,

    @Column(name = "weather_score", nullable = false)
    @Comment("날씨 점수 (피드백 시점)")
    val weatherScore: Int,

    @Column(name = "adjustment_amount", nullable = false)
    @Comment("가중치 조정량")
    private var _adjustmentAmount: Double,

    @Column(name = "previous_comfort_temp", nullable = false)
    @Comment("조정 전 쾌적온도")
    val previousComfortTemp: Int,

    @Column(name = "updated_comfort_temp", nullable = false)
    @Comment("조정 후 쾌적온도")
    private var _updatedComfortTemp: Int,

    @Column(name = "comments", length = 500)
    @Comment("추가 의견")
    val comments: String? = null,

    @Column(name = "is_confirmed", nullable = false)
    @Comment("확인 여부")
    val isConfirmed: Boolean = true
) : BaseTimeEntity() {

    // 읽기 전용 프로퍼티
    val feedbackType: FeedbackType get() = _feedbackType
    val adjustmentAmount: Double get() = _adjustmentAmount
    val updatedComfortTemp: Int get() = _updatedComfortTemp

    companion object {
        /**
         * 피드백 생성 팩토리 메서드
         */
        fun create(
            memberId: Long,
            weatherDataId: Long,
            feedbackType: FeedbackType,
            actualTemperature: Double,
            feelsLikeTemperature: Double,
            weatherScore: Int,
            previousComfortTemp: Int,
            comments: String? = null,
            isConfirmed: Boolean = true
        ): Feedback {
            require(memberId > 0) { "Member ID는 양수여야 합니다" }
            require(weatherDataId > 0) { "Weather Data ID는 양수여야 합니다" }
            require(actualTemperature >= -50 && actualTemperature <= 60) { "기온은 -50~60도 범위여야 합니다" }
            require(weatherScore in 0..100) { "날씨 점수는 0-100 범위여야 합니다" }
            require(previousComfortTemp in 10..30) { "쾌적온도는 10-30도 범위여야 합니다" }

            val adjustmentAmount = feedbackType.calculateAdjustment()
            val updatedComfortTemp = calculateUpdatedComfortTemp(previousComfortTemp, adjustmentAmount)

            return Feedback(
                memberId = memberId,
                weatherDataId = weatherDataId,
                _feedbackType = feedbackType,
                actualTemperature = actualTemperature,
                feelsLikeTemperature = feelsLikeTemperature,
                weatherScore = weatherScore,
                _adjustmentAmount = adjustmentAmount,
                previousComfortTemp = previousComfortTemp,
                _updatedComfortTemp = updatedComfortTemp,
                comments = comments,
                isConfirmed = isConfirmed
            )
        }

        private fun calculateUpdatedComfortTemp(previousTemp: Int, adjustmentAmount: Double): Int {
            val newTemp = previousTemp + adjustmentAmount.toInt()
            return newTemp.coerceIn(10, 30) // 10-30도 범위 강제
        }
    }

    // ===== 도메인 로직 (개인 속성 기반) =====

    /**
     * 피드백이 긍정적인지 확인
     */
    fun isPositiveFeedback(): Boolean {
        return _feedbackType == FeedbackType.PERFECT
    }

    /**
     * 피드백이 온도 조정이 필요한지 확인
     */
    fun needsTemperatureAdjustment(): Boolean {
        return _feedbackType != FeedbackType.PERFECT
    }

    /**
     * 추위 관련 피드백인지 확인
     */
    fun isColdFeedback(): Boolean {
        return _feedbackType in listOf(FeedbackType.TOO_COLD, FeedbackType.SLIGHTLY_COLD)
    }

    /**
     * 더위 관련 피드백인지 확인
     */
    fun isHotFeedback(): Boolean {
        return _feedbackType in listOf(FeedbackType.TOO_HOT, FeedbackType.SLIGHTLY_HOT)
    }

    /**
     * 강한 피드백인지 확인 (TOO_COLD, TOO_HOT)
     */
    fun isStrongFeedback(): Boolean {
        return _feedbackType in listOf(FeedbackType.TOO_COLD, FeedbackType.TOO_HOT)
    }

    /**
     * 피드백 강도 반환 (1: 약함, 2: 강함)
     */
    fun getFeedbackIntensity(): Int {
        return when (_feedbackType) {
            FeedbackType.TOO_COLD, FeedbackType.TOO_HOT -> 2
            FeedbackType.SLIGHTLY_COLD, FeedbackType.SLIGHTLY_HOT -> 1
            FeedbackType.PERFECT -> 0
        }
    }

    /**
     * 온도 차이 분석 (실제온도 vs 체감온도)
     */
    fun getTemperatureDifference(): Double {
        return feelsLikeTemperature - actualTemperature
    }

    /**
     * 피드백 신뢰도 점수 계산 (확인 여부, 강도 등 고려)
     */
    fun calculateReliabilityScore(): Double {
        var score = 1.0

        // 확인 여부
        if (!isConfirmed) score *= 0.7

        // 피드백 강도 (강한 피드백이 더 신뢰성 높음)
        score *= when (getFeedbackIntensity()) {
            2 -> 1.0  // 강함
            1 -> 0.8  // 약함
            else -> 0.5  // 완벽
        }

        // 추가 의견이 있으면 신뢰도 증가
        if (!comments.isNullOrBlank()) score *= 1.1

        return score.coerceIn(0.0, 1.0)
    }

    /**
     * 학습 가중치 반환 (신뢰도 기반)
     */
    fun getLearningWeight(): Double {
        return calculateReliabilityScore() * 0.1  // 기본 학습률 10%
    }
}

/**
 * 피드백 타입 열거형
 */
enum class FeedbackType(
    val score: Int,
    val displayName: String,
    val emoji: String,
    val description: String
) {
    TOO_COLD(-2, "너무 추웠어요", "🥶", "더 두껍게 입을걸..."),
    SLIGHTLY_COLD(-1, "약간 추웠어요", "😐", "살짝 서늘했어요"),
    PERFECT(0, "딱 맞았어요", "😊", "완벽한 추천!"),
    SLIGHTLY_HOT(1, "약간 더웠어요", "😅", "좀 더 가볍게..."),
    TOO_HOT(2, "너무 더웠어요", "🔥", "땀이 났어요");

    /**
     * 쾌적온도 조정량 계산 (도메인 로직)
     */
    fun calculateAdjustment(): Double {
        return when (this) {
            TOO_COLD -> -2.0      // 쾌적온도 2도 감소 (더 추위 민감)
            SLIGHTLY_COLD -> -1.0 // 쾌적온도 1도 감소
            PERFECT -> 0.0        // 조정 없음
            SLIGHTLY_HOT -> 1.0   // 쾌적온도 1도 증가
            TOO_HOT -> 2.0        // 쾌적온도 2도 증가 (더위 민감)
        }
    }

    /**
     * 온도 방향성 반환
     */
    fun getTemperatureDirection(): String {
        return when {
            score < 0 -> "COLD"
            score > 0 -> "HOT"
            else -> "NEUTRAL"
        }
    }

    companion object {
        fun fromScore(score: Int): FeedbackType {
            return FeedbackType.entries.find { it.score == score }
                ?: throw IllegalArgumentException("Invalid feedback score: $score")
        }

        fun fromString(value: String): FeedbackType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid feedback type: $value")
            }
        }
    }
}