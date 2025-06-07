package com.wout.outfit.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

/**
 * packageName    : com.wout.outfit.dto.request
 * fileName       : SatisfactionFeedbackRequest
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 만족도 피드백 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Schema(description = "아웃핏 추천 만족도 피드백 요청")
data class SatisfactionFeedbackRequest(

    @field:Min(value = 1, message = "만족도 점수는 1 이상이어야 합니다")
    @field:Max(value = 5, message = "만족도 점수는 5 이하여야 합니다")
    @Schema(description = "만족도 점수 (1: 매우 불만족, 5: 매우 만족)", example = "4")
    val satisfactionScore: Int,

    @field:Size(max = 500, message = "추가 의견은 500자 이하여야 합니다")
    @Schema(description = "추가 의견 (선택사항)", example = "조금 더 따뜻한 옷을 추천해주세요")
    val feedback: String? = null
)