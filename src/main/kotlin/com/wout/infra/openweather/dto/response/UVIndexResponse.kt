package com.wout.infra.openweather.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.infra.openweather.dto.response
 * fileName       : UVIndexResponse
 * author         : MinKyu Park
 * date           : 2025-05-25
 * description    : OpenWeather UV Index API 응답을 매핑하기 위한 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-25        MinKyu Park       최초 생성
 */
@Schema(description = "OpenWeather UV Index API 응답")
data class UVIndexResponse(
    @Schema(description = "위도", example = "37.5665")
    val lat: Double,

    @Schema(description = "경도", example = "126.9780")
    val lon: Double,

    @Schema(description = "ISO 날짜 문자열", example = "2025-05-25T12:00:00Z")
    @JsonProperty("date_iso")
    val dateIso: String,

    @Schema(description = "Unix timestamp", example = "1716649200")
    val date: Long,

    @Schema(description = "UV Index 값", example = "7.5")
    val value: Double
)