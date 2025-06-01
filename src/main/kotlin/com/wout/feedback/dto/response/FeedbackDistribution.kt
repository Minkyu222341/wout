package com.wout.feedback.dto.response

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : FeedbackDistribution
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 분포 정보 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackDistribution(
    val perfect: Int,
    val cold: Int,      // TOO_COLD + SLIGHTLY_COLD
    val hot: Int,       // TOO_HOT + SLIGHTLY_HOT
    val strongFeedback: Int,  // TOO_COLD + TOO_HOT
    val withComments: Int
)