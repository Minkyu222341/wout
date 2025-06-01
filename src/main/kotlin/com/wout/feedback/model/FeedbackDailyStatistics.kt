package com.wout.feedback.model

import com.wout.feedback.entity.FeedbackType
import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.model
 * fileName       : FeedbackDailyStatistics
 * author         : MinKyu Park
 * date           : 2025-05-31
 * description    : 일별 피드백 통계 모델 (Repository 쿼리 결과용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-31        MinKyu Park       최초 생성
 */
data class FeedbackDailyStatistics(
    val date: LocalDateTime,
    val totalCount: Long,
    val averageAdjustment: Double,
    val mostFrequentType: FeedbackType
)