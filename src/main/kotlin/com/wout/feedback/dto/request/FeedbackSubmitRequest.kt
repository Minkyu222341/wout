package com.wout.feedback.dto.request

import jakarta.validation.constraints.*

/**
 * packageName    : com.wout.feedback.dto.request
 * fileName       : FeedbackSubmitRequest
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 제출 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackSubmitRequest(
    @field:NotNull(message = "날씨 데이터 ID는 필수입니다")
    @field:Positive(message = "날씨 데이터 ID는 양수여야 합니다")
    val weatherDataId: Long,

    @field:NotBlank(message = "피드백 타입은 필수입니다")
    @field:Pattern(
        regexp = "^(TOO_COLD|SLIGHTLY_COLD|PERFECT|SLIGHTLY_HOT|TOO_HOT)$",
        message = "유효하지 않은 피드백 타입입니다"
    )
    val feedbackType: String,

    @field:Size(max = 500, message = "의견은 500자 이내로 입력해주세요")
    val comments: String? = null,

    val isConfirmed: Boolean = true
)