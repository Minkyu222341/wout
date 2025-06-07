package com.wout.outfit.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * packageName    : com.wout.outfit.dto.request
 * fileName       : InstantRecommendationRequest
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 위치 기반 즉시 아웃핏 추천 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Schema(description = "위치 기반 즉시 아웃핏 추천 요청")
data class InstantRecommendationRequest(

    @field:NotBlank(message = "디바이스 ID는 필수입니다")
    @field:Size(max = 100, message = "디바이스 ID는 100자 이하여야 합니다")
    @Schema(description = "디바이스 ID", example = "device_12345")
    val deviceId: String,

    @field:DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다")
    @Schema(description = "위도", example = "37.5665")
    val latitude: Double,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다")
    @Schema(description = "경도", example = "126.9780")
    val longitude: Double
)