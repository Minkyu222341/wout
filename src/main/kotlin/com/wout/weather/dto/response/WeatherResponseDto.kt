package com.wout.weather.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * packageName    : com.wout.weather.dto
 * fileName       : WeatherResponseDto
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 날씨 정보 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Schema(description = "날씨 정보 응답")
data class WeatherResponseDto(
    @Schema(description = "온도 (섭씨)", example = "23.5")
    val temperature: Double,

    @Schema(description = "체감 온도 (섭씨)", example = "24.2")
    val feelsLike: Double,

    @Schema(description = "습도 (%)", example = "65")
    val humidity: Int,

    @Schema(description = "풍속 (m/s)", example = "2.5")
    val windSpeed: Double,

    @Schema(description = "날씨 상태 (Clear, Rain 등)", example = "Clear")
    val weatherState: String,

    @Schema(description = "날씨 상세 설명", example = "맑음")
    val weatherDescription: String,

    @Schema(description = "대기질 정보")
    val airQuality: AirQualityDto?,

    @Schema(description = "업데이트 시간", example = "2025-05-21T14:30:00")
    val updatedAt: LocalDateTime
)

@Schema(description = "대기질 정보")
data class AirQualityDto(
    @Schema(description = "미세먼지 PM2.5 (μg/m³)", example = "15.2")
    val pm25: Double?,

    @Schema(description = "미세먼지 PM10 (μg/m³)", example = "38.6")
    val pm10: Double?,

    @Schema(description = "대기질 지수 (좋음, 보통, 나쁨, 매우나쁨)", example = "보통")
    val airQualityIndex: String
)