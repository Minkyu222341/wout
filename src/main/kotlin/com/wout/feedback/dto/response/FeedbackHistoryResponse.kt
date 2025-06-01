package com.wout.feedback.dto.response

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : FeedbackHistoryResponse
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 히스토리 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackHistoryResponse(
    val feedbacks: List<FeedbackSummaryResponse>,
    val totalCount: Long,
    val currentPage: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)