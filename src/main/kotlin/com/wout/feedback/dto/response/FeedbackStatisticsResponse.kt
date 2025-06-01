package com.wout.feedback.dto.response

import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : FeedbackStatisticsResponse
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 통계 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackStatisticsResponse(
    val period: String,
    val totalFeedbackCount: Int,
    val feedbackDistribution: FeedbackDistribution,
    val averageReliabilityScore: Double,
    val learningProgress: LearningProgress,
    val temperatureAnalysis: TemperatureAnalysis,
    val lastFeedbackDate: LocalDateTime?
)