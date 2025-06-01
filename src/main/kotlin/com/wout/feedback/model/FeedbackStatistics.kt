package com.wout.feedback.model

import com.wout.feedback.entity.FeedbackType

/**
 * packageName    : com.wout.feedback.model
 * fileName       : FeedbackStatistics
 * author         : MinKyu Park
 * date           : 2025-05-31
 * description    : 피드백 통계 모델 (Service 계층용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-31        MinKyu Park       최초 생성
 */
data class FeedbackStatistics(
    val totalFeedbacks: Int,
    val feedbackDistribution: Map<FeedbackType, Int>,
    val averageAdjustment: Double,
    val learningTrend: String
)