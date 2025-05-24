package com.wout.infra.openweather.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.infra.openweather.dto
 * fileName       : AirPollutionDto
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 대기오염 API 응답을 매핑하기 위한 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Schema(description = "대기오염 API 응답")
data class AirPollutionResponse(
    @Schema(description = "좌표 정보")
    val coord: Coord,

    @Schema(description = "대기오염 데이터 목록")
    val list: List<AirPollutionData>
)

@Schema(description = "대기오염 데이터")
data class AirPollutionData(
    @Schema(description = "대기질 지수 정보")
    val main: AirQualityMain,

    @Schema(description = "대기 오염물질 구성 요소")
    val components: AirComponents,

    @Schema(description = "데이터 측정 시간 (Unix timestamp)")
    val dt: Long
)

@Schema(description = "대기질 지수 정보")
data class AirQualityMain(
    @Schema(description = "대기질 지수 (1: 좋음 ~ 5: 매우 나쁨)", example = "2")
    val aqi: Int
)

@Schema(description = "대기 오염물질 구성 요소")
data class AirComponents(
    @Schema(description = "일산화탄소 농도 (μg/m³)", example = "320.5")
    val co: Double,

    @Schema(description = "일산화질소 농도 (μg/m³)", example = "2.1")
    val no: Double,

    @Schema(description = "이산화질소 농도 (μg/m³)", example = "15.3")
    val no2: Double,

    @Schema(description = "오존 농도 (μg/m³)", example = "120.4")
    val o3: Double,

    @Schema(description = "이산화황 농도 (μg/m³)", example = "5.2")
    val so2: Double,

    @Schema(description = "미세먼지 PM2.5 농도 (μg/m³)", example = "15.2")
    @JsonProperty("pm2_5")
    val pm25: Double,

    @Schema(description = "미세먼지 PM10 농도 (μg/m³)", example = "38.6")
    val pm10: Double,

    @Schema(description = "암모니아 농도 (μg/m³)", example = "3.7")
    val nh3: Double
)