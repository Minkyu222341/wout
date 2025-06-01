package com.wout.feedback.dto.response

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : LearningProgress
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 학습 진행 상황 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class LearningProgress(
    val totalAdjustment: Double,
    val averageAdjustmentPerFeedback: Double,
    val trend: LearningTrend,
    val accuracyScore: Double
)