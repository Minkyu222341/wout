package com.wout.outfit.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

/**
 * packageName    : com.wout.outfit.dto.request
 * fileName       : OutfitRecommendationRequest
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 날씨 데이터 기반 아웃핏 추천 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Schema(description = "날씨 데이터 기반 아웃핏 추천 요청")
data class OutfitRecommendationRequest(

    @field:NotBlank(message = "디바이스 ID는 필수입니다")
    @field:Size(max = 100, message = "디바이스 ID는 100자 이하여야 합니다")
    @Schema(description = "디바이스 ID", example = "device_12345")
    val deviceId: String,

    @field:Positive(message = "날씨 데이터 ID는 양수여야 합니다")
    @Schema(description = "날씨 데이터 ID", example = "1")
    val weatherDataId: Long
)