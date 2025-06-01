package com.wout.feedback.mapper

import com.wout.feedback.dto.response.*
import com.wout.feedback.entity.Feedback
import com.wout.feedback.entity.FeedbackType
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.feedback.mapper
 * fileName       : FeedbackMapper
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 매퍼 (복잡한 변환 로직 처리)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       가이드 v2.0 적용 (도메인 로직 활용)
 */
@Component
class FeedbackMapper {

    /**
     * Feedback Entity → FeedbackResponse 변환
     */
    fun toResponse(feedback: Feedback): FeedbackResponse {
        return FeedbackResponse(
            id = feedback.id,
            memberId = feedback.memberId,
            weatherDataId = feedback.weatherDataId,
            feedbackType = toFeedbackTypeInfo(feedback.feedbackType),
            actualTemperature = feedback.actualTemperature,
            feelsLikeTemperature = feedback.feelsLikeTemperature,
            weatherScore = feedback.weatherScore,
            adjustmentAmount = feedback.adjustmentAmount,
            previousComfortTemp = feedback.previousComfortTemp,
            updatedComfortTemp = feedback.updatedComfortTemp,
            comments = feedback.comments,
            isConfirmed = feedback.isConfirmed,
            reliabilityScore = feedback.calculateReliabilityScore(),  // 도메인 로직 활용
            learningWeight = feedback.getLearningWeight(),            // 도메인 로직 활용
            temperatureDifference = feedback.getTemperatureDifference(), // 도메인 로직 활용
            createdAt = feedback.createdAt
        )
    }

    /**
     * Feedback Entity → FeedbackSummaryResponse 변환 (리스트용)
     */
    fun toSummaryResponse(feedback: Feedback): FeedbackSummaryResponse {
        return FeedbackSummaryResponse(
            id = feedback.id,
            feedbackType = toFeedbackTypeInfo(feedback.feedbackType),
            actualTemperature = feedback.actualTemperature,
            feelsLikeTemperature = feedback.feelsLikeTemperature,
            weatherScore = feedback.weatherScore,
            adjustmentAmount = feedback.adjustmentAmount,
            comments = feedback.comments,
            reliabilityScore = feedback.calculateReliabilityScore(), // 도메인 로직 활용
            createdAt = feedback.createdAt
        )
    }

    /**
     * Page<Feedback> → FeedbackHistoryResponse 변환
     */
    fun toHistoryResponse(feedbackPage: Page<Feedback>): FeedbackHistoryResponse {
        return FeedbackHistoryResponse(
            feedbacks = feedbackPage.content.map { toSummaryResponse(it) },
            totalCount = feedbackPage.totalElements,
            currentPage = feedbackPage.number,
            totalPages = feedbackPage.totalPages,
            hasNext = feedbackPage.hasNext(),
            hasPrevious = feedbackPage.hasPrevious()
        )
    }

    /**
     * List<Feedback> → FeedbackStatisticsResponse 변환 (복잡한 통계 계산)
     */
    fun toStatisticsResponse(
        feedbacks: List<Feedback>,
        days: Int = 30
    ): FeedbackStatisticsResponse {
        if (feedbacks.isEmpty()) {
            return createEmptyStatistics(days)
        }

        val distribution = calculateFeedbackDistribution(feedbacks)
        val learningProgress = calculateLearningProgress(feedbacks)
        val temperatureAnalysis = calculateTemperatureAnalysis(feedbacks)

        return FeedbackStatisticsResponse(
            period = "최근 ${days}일",  // 동적 설정
            totalFeedbackCount = feedbacks.size,
            feedbackDistribution = distribution,
            averageReliabilityScore = feedbacks.map { it.calculateReliabilityScore() }.average(),
            learningProgress = learningProgress,
            temperatureAnalysis = temperatureAnalysis,
            lastFeedbackDate = feedbacks.maxByOrNull { it.createdAt }?.createdAt
        )
    }

    // ===== Private 변환 메서드들 =====

    /**
     * FeedbackType → FeedbackTypeInfo 변환
     */
    private fun toFeedbackTypeInfo(feedbackType: FeedbackType): FeedbackTypeInfo {
        return FeedbackTypeInfo(
            code = feedbackType.name,
            score = feedbackType.score,
            displayName = feedbackType.displayName,
            emoji = feedbackType.emoji,
            description = feedbackType.description,
            direction = feedbackType.getTemperatureDirection(), // Enum 도메인 로직 활용
            intensity = when (feedbackType) {
                FeedbackType.TOO_COLD, FeedbackType.TOO_HOT -> 2
                FeedbackType.SLIGHTLY_COLD, FeedbackType.SLIGHTLY_HOT -> 1
                FeedbackType.PERFECT -> 0
            }
        )
    }

    // ===== 복잡한 통계 계산 메서드들 (매퍼 책임) =====

    /**
     * 피드백 분포 계산
     */
    private fun calculateFeedbackDistribution(feedbacks: List<Feedback>): FeedbackDistribution {
        return FeedbackDistribution(
            perfect = feedbacks.count { it.isPositiveFeedback() },        // 도메인 로직 활용
            cold = feedbacks.count { it.isColdFeedback() },               // 도메인 로직 활용
            hot = feedbacks.count { it.isHotFeedback() },                 // 도메인 로직 활용
            strongFeedback = feedbacks.count { it.isStrongFeedback() },   // 도메인 로직 활용
            withComments = feedbacks.count { !it.comments.isNullOrBlank() }
        )
    }

    /**
     * 학습 진행 상황 계산
     */
    private fun calculateLearningProgress(feedbacks: List<Feedback>): LearningProgress {
        val totalAdjustment = feedbacks.sumOf { it.adjustmentAmount }
        val averageAdjustment = if (feedbacks.isNotEmpty()) totalAdjustment / feedbacks.size else 0.0
        val trend = determineTrend(feedbacks)
        val accuracyScore = calculateAccuracyScore(feedbacks)

        return LearningProgress(
            totalAdjustment = totalAdjustment,
            averageAdjustmentPerFeedback = averageAdjustment,
            trend = trend,
            accuracyScore = accuracyScore
        )
    }

    /**
     * 온도 분석 계산
     */
    private fun calculateTemperatureAnalysis(feedbacks: List<Feedback>): TemperatureAnalysis {
        val temperatureDifferences = feedbacks.map { it.getTemperatureDifference() } // 도메인 로직 활용
        val averageDifference = temperatureDifferences.average()

        val coldBias = feedbacks.filter { it.isColdFeedback() }      // 도메인 로직 활용
            .map { it.adjustmentAmount }.takeIf { it.isNotEmpty() }?.average() ?: 0.0

        val hotBias = feedbacks.filter { it.isHotFeedback() }        // 도메인 로직 활용
            .map { it.adjustmentAmount }.takeIf { it.isNotEmpty() }?.average() ?: 0.0

        val optimalRange = calculateOptimalTemperatureRange(feedbacks)

        return TemperatureAnalysis(
            averageTemperatureDifference = averageDifference,
            coldBias = coldBias,
            hotBias = hotBias,
            optimalTemperatureRange = optimalRange
        )
    }

    // ===== 헬퍼 메서드들 =====

    /**
     * 학습 트렌드 결정
     */
    private fun determineTrend(feedbacks: List<Feedback>): String {
        if (feedbacks.size < 3) return "INSUFFICIENT_DATA"

        val recent = feedbacks.takeLast(feedbacks.size / 2)
        val earlier = feedbacks.take(feedbacks.size / 2)

        val recentPerfectRatio = recent.count { it.isPositiveFeedback() }.toDouble() / recent.size
        val earlierPerfectRatio = earlier.count { it.isPositiveFeedback() }.toDouble() / earlier.size

        return when {
            recentPerfectRatio > earlierPerfectRatio + 0.1 -> "IMPROVING"
            recentPerfectRatio < earlierPerfectRatio - 0.1 -> "DECLINING"
            else -> "STABLE"
        }
    }

    /**
     * 정확도 점수 계산
     */
    private fun calculateAccuracyScore(feedbacks: List<Feedback>): Double {
        val perfectCount = feedbacks.count { it.isPositiveFeedback() } // 도메인 로직 활용
        val totalCount = feedbacks.size

        return if (totalCount > 0) (perfectCount.toDouble() / totalCount) * 100 else 0.0
    }

    /**
     * 최적 온도 범위 계산
     */
    private fun calculateOptimalTemperatureRange(feedbacks: List<Feedback>): String {
        val perfectFeedbacks = feedbacks.filter { it.isPositiveFeedback() } // 도메인 로직 활용

        if (perfectFeedbacks.isEmpty()) {
            return "데이터 부족"
        }

        val temperatures = perfectFeedbacks.map { it.actualTemperature }
        val minTemp = temperatures.minOrNull()?.toInt() ?: 0
        val maxTemp = temperatures.maxOrNull()?.toInt() ?: 0

        return if (minTemp == maxTemp) {
            "${minTemp}°C"
        } else {
            "${minTemp}°C ~ ${maxTemp}°C"
        }
    }

    /**
     * 빈 통계 응답 생성
     */
    private fun createEmptyStatistics(days: Int): FeedbackStatisticsResponse {
        return FeedbackStatisticsResponse(
            period = "최근 ${days}일",
            totalFeedbackCount = 0,
            feedbackDistribution = FeedbackDistribution(0, 0, 0, 0, 0),
            averageReliabilityScore = 0.0,
            learningProgress = LearningProgress(0.0, 0.0, "NO_DATA", 0.0),
            temperatureAnalysis = TemperatureAnalysis(0.0, 0.0, 0.0, "데이터 없음"),
            lastFeedbackDate = null
        )
    }
}