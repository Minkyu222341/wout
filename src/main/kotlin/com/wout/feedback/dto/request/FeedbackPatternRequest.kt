package com.wout.feedback.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

/**
 * packageName    : com.wout.feedback.dto.request
 * fileName       : FeedbackPatternRequest
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 패턴 분석 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackPatternRequest(
    @field:DecimalMin(value = "-50.0", message = "기온은 -50도 이상이어야 합니다")
    @field:DecimalMax(value = "60.0", message = "기온은 60도 이하여야 합니다")
    val temperature: Double,

    @field:DecimalMin(value = "0.1", message = "범위는 0.1도 이상이어야 합니다")
    @field:DecimalMax(value = "10.0", message = "범위는 10도 이하여야 합니다")
    val range: Double = 2.0
)