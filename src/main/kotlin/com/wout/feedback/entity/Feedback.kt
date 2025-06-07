package com.wout.feedback.entity

import com.wout.common.entity.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * packageName    : com.wout.feedback.entity
 * fileName       : Feedback
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 사용자 피드백 엔티티 (언더바 제거, QueryDSL 최적화)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       가이드 v2.0 적용 (Entity 중심 도메인 로직)
 * 2025-06-03        MinKyu Park       언더바 제거, 불변성 강화, QueryDSL 친화적으로 개선
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
    val feedbackType: FeedbackType,

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
    val adjustmentAmount: Double,

    @Column(name = "previous_comfort_temp", nullable = false)
    @Comment("조정 전 쾌적온도")
    val previousComfortTemp: Int,

    @Column(name = "updated_comfort_temp", nullable = false)
    @Comment("조정 후 쾌적온도")
    val updatedComfortTemp: Int,

    @Column(name = "comments", length = 500)
    @Comment("추가 의견")
    val comments: String? = null,

    @Column(name = "is_confirmed", nullable = false)
    @Comment("확인 여부")
    val isConfirmed: Boolean = true
) : BaseTimeEntity() {

    protected constructor() : this(
        memberId = 0L, weatherDataId = 0L, feedbackType = FeedbackType.PERFECT,
        actualTemperature = 0.0, feelsLikeTemperature = 0.0, weatherScore = 0,
        adjustmentAmount = 0.0, previousComfortTemp = 20, updatedComfortTemp = 20
    )

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
                feedbackType = feedbackType,
                actualTemperature = actualTemperature,
                feelsLikeTemperature = feelsLikeTemperature,
                weatherScore = weatherScore,
                adjustmentAmount = adjustmentAmount,
                previousComfortTemp = previousComfortTemp,
                updatedComfortTemp = updatedComfortTemp,
                comments = comments,
                isConfirmed = isConfirmed
            )
        }

        /**
         * FeedbackSubmitRequest로부터 생성
         */
        fun from(
            memberId: Long,
            weatherDataId: Long,
            request: com.wout.feedback.dto.request.FeedbackSubmitRequest,
            actualTemperature: Double,
            feelsLikeTemperature: Double,
            weatherScore: Int,
            previousComfortTemp: Int
        ): Feedback {
            val feedbackType = FeedbackType.fromString(request.feedbackType)

            return create(
                memberId = memberId,
                weatherDataId = weatherDataId,
                feedbackType = feedbackType,
                actualTemperature = actualTemperature,
                feelsLikeTemperature = feelsLikeTemperature,
                weatherScore = weatherScore,
                previousComfortTemp = previousComfortTemp,
                comments = request.comments,
                isConfirmed = true
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
        return feedbackType == FeedbackType.PERFECT
    }

    /**
     * 피드백이 온도 조정이 필요한지 확인
     */
    fun needsTemperatureAdjustment(): Boolean {
        return feedbackType != FeedbackType.PERFECT
    }

    /**
     * 추위 관련 피드백인지 확인
     */
    fun isColdFeedback(): Boolean {
        return feedbackType in listOf(FeedbackType.TOO_COLD, FeedbackType.SLIGHTLY_COLD)
    }

    /**
     * 더위 관련 피드백인지 확인
     */
    fun isHotFeedback(): Boolean {
        return feedbackType in listOf(FeedbackType.TOO_HOT, FeedbackType.SLIGHTLY_HOT)
    }

    /**
     * 강한 피드백인지 확인 (TOO_COLD, TOO_HOT)
     */
    fun isStrongFeedback(): Boolean {
        return feedbackType in listOf(FeedbackType.TOO_COLD, FeedbackType.TOO_HOT)
    }

    /**
     * 약한 피드백인지 확인 (SLIGHTLY_COLD, SLIGHTLY_HOT)
     */
    fun isMildFeedback(): Boolean {
        return feedbackType in listOf(FeedbackType.SLIGHTLY_COLD, FeedbackType.SLIGHTLY_HOT)
    }

    /**
     * 피드백 강도 반환 (0: 완벽, 1: 약함, 2: 강함)
     */
    fun getFeedbackIntensity(): Int {
        return when (feedbackType) {
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
     * 개인화 정확도 분석 (체감온도가 얼마나 잘 맞았는지)
     */
    fun getPersonalizationAccuracy(): Double {
        val tempDiff = kotlin.math.abs(getTemperatureDifference())
        return when {
            tempDiff <= 1.0 -> 1.0    // 매우 정확
            tempDiff <= 2.0 -> 0.8    // 정확
            tempDiff <= 3.0 -> 0.6    // 보통
            tempDiff <= 5.0 -> 0.4    // 부정확
            else -> 0.2               // 매우 부정확
        }
    }

    /**
     * 피드백 신뢰도 점수 계산 (확인 여부, 강도, 코멘트 등 고려)
     */
    fun calculateReliabilityScore(): Double {
        var score = 1.0

        // 확인 여부
        if (!isConfirmed) score *= 0.7

        // 피드백 강도 (강한 피드백이 더 신뢰성 높음)
        score *= when (getFeedbackIntensity()) {
            2 -> 1.0  // 강함
            1 -> 0.8  // 약함
            else -> 0.5  // 완벽 (조정 필요성 낮음)
        }

        // 추가 의견이 있으면 신뢰도 증가
        if (!comments.isNullOrBlank()) score *= 1.1

        // 온도 차이가 크면 신뢰도 감소 (개인화가 잘못되었을 가능성)
        val tempDiffPenalty = kotlin.math.abs(getTemperatureDifference()) * 0.1
        score *= (1.0 - tempDiffPenalty.coerceAtMost(0.5))

        return score.coerceIn(0.0, 1.0)
    }

    /**
     * 학습 가중치 반환 (신뢰도 기반)
     */
    fun getLearningWeight(): Double {
        return calculateReliabilityScore() * 0.1  // 기본 학습률 10%
    }

    /**
     * 피드백 방향성 (추움/더움/중립)
     */
    fun getDirectionality(): String {
        return feedbackType.getTemperatureDirection()
    }

    /**
     * 쾌적온도 조정이 유효한지 확인
     */
    fun isValidAdjustment(): Boolean {
        return updatedComfortTemp in 10..30 &&
                kotlin.math.abs(adjustmentAmount) <= 5.0  // 최대 5도까지만 조정
    }

    /**
     * 피드백 요약 메시지 생성
     */
    fun generateFeedbackSummary(): String {
        val intensityText = when (getFeedbackIntensity()) {
            2 -> "강하게"
            1 -> "약간"
            else -> ""
        }

        val directionText = when {
            isColdFeedback() -> "추위를 느꼈습니다"
            isHotFeedback() -> "더위를 느꼈습니다"
            else -> "적절했습니다"
        }

        return if (intensityText.isNotEmpty()) {
            "$intensityText $directionText"
        } else {
            directionText
        }
    }

    /**
     * 개선 제안사항 생성
     */
    fun generateImprovementSuggestion(): String? {
        return when (feedbackType) {
            FeedbackType.TOO_COLD -> "다음에는 더 따뜻한 옷을 추천드리겠습니다"
            FeedbackType.SLIGHTLY_COLD -> "조금 더 따뜻하게 입는 것을 고려해보세요"
            FeedbackType.TOO_HOT -> "다음에는 더 시원한 옷을 추천드리겠습니다"
            FeedbackType.SLIGHTLY_HOT -> "조금 더 가볍게 입는 것을 고려해보세요"
            FeedbackType.PERFECT -> null
        }
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

    /**
     * 만족도 점수 반환 (0-100)
     */
    fun getSatisfactionScore(): Int {
        return when (this) {
            PERFECT -> 100
            SLIGHTLY_COLD, SLIGHTLY_HOT -> 70
            TOO_COLD, TOO_HOT -> 30
        }
    }

    /**
     * 개선 우선순위 반환 (1: 높음, 2: 보통, 3: 낮음)
     */
    fun getImprovementPriority(): Int {
        return when (this) {
            TOO_COLD, TOO_HOT -> 1      // 높은 우선순위
            SLIGHTLY_COLD, SLIGHTLY_HOT -> 2  // 보통 우선순위
            PERFECT -> 3                 // 낮은 우선순위
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

        /**
         * 모든 부정적 피드백 반환
         */
        fun getNegativeFeedbacks(): List<FeedbackType> {
            return listOf(TOO_COLD, SLIGHTLY_COLD, SLIGHTLY_HOT, TOO_HOT)
        }

        /**
         * 추위 관련 피드백 반환
         */
        fun getColdFeedbacks(): List<FeedbackType> {
            return listOf(TOO_COLD, SLIGHTLY_COLD)
        }

        /**
         * 더위 관련 피드백 반환
         */
        fun getHotFeedbacks(): List<FeedbackType> {
            return listOf(SLIGHTLY_HOT, TOO_HOT)
        }
    }
}