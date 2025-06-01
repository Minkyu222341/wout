package com.wout.feedback.dto.response

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : FeedbackTypeInfo
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 타입 정보 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackTypeInfo(
    val code: String,
    val score: Int,
    val displayName: String,
    val emoji: String,
    val description: String,
    val direction: String,
    val intensity: Int
)