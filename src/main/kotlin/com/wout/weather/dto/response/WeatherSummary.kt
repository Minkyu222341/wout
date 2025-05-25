package com.wout.weather.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * packageName    : com.wout.weather.dto.response
 * fileName       : WeatherSummary
 * author         : MinKyu Park
 * date           : 25. 5. 25.
 * description    : 전국 날씨 현황 요약 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 25.        MinKyu Park       최초 생성
 */
@Schema(description = "전국 날씨 현황 요약")
data class WeatherSummary(

    @Schema(description = "데이터 제공 도시 수", example = "23")
    val availableCities: Int,

    @Schema(description = "전국 평균 기온", example = "18.5")
    val averageTemperature: Double,

    @Schema(description = "전국 최고 기온", example = "25.2")
    val maxTemperature: Double,

    @Schema(description = "전국 최저 기온", example = "12.1")
    val minTemperature: Double,

    @Schema(description = "전국 평균 습도", example = "65.3")
    val averageHumidity: Double,

    @Schema(description = "전국 평균 풍속", example = "3.2")
    val averageWindSpeed: Double,

    @Schema(description = "최고 기온 도시", example = "대구")
    val maxTemperatureCity: String,

    @Schema(description = "최저 기온 도시", example = "춘천")
    val minTemperatureCity: String,

    @Schema(description = "마지막 업데이트 시간")
    val lastUpdated: LocalDateTime,

    @Schema(description = "요약 메시지", example = "전국적으로 쌀쌀한 날씨입니다")
    val message: String? = null
)