package com.wout.feedback.dto.response

import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : FeedbackSummaryResponse
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 요약 응답 DTO (리스트용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackSummaryResponse(
    val id: Long,
    val feedbackType: FeedbackTypeInfo,
    val actualTemperature: Double,
    val feelsLikeTemperature: Double,
    val weatherScore: Int,
    val adjustmentAmount: Double,
    val comments: String?,
    val reliabilityScore: Double,
    val createdAt: LocalDateTime
)