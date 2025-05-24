package com.wout.weather.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

/**
 * packageName    : com.wout.weather.dto.request
 * fileName       : WeatherRequestDto
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : 날씨 조회 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 */
@Schema(description = "날씨 조회 요청")
data class WeatherRequest(

    @field:NotNull(message = "위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다")
    @Schema(
        description = "위도",
        example = "37.5665",
        minimum = "-90.0",
        maximum = "90.0",
        required = true
    )
    val latitude: Double,

    @field:NotNull(message = "경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다")
    @Schema(
        description = "경도",
        example = "126.9780",
        minimum = "-180.0",
        maximum = "180.0",
        required = true
    )
    val longitude: Double
)