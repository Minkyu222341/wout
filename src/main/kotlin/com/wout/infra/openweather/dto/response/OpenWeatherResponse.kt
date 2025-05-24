package com.wout.infra.openweather.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.infra.openweather.dto.response
 * fileName       : OpenWeatherResponse
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : OpenWeather API 응답을 매핑하기 위한 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       Rain, Snow 클래스 추가, 필드명 수정
 */
@Schema(description = "OpenWeather API 응답")
data class OpenWeatherResponse(
    @Schema(description = "좌표 정보")
    val coord: Coord,

    @Schema(description = "날씨 정보 목록")
    val weather: List<Weather>,

    @Schema(description = "내부 파라미터")
    val base: String,

    @Schema(description = "주요 날씨 정보")
    val main: Main,

    @Schema(description = "가시성 (미터)")
    val visibility: Int? = null,

    @Schema(description = "바람 정보")
    val wind: Wind,

    @Schema(description = "구름 정보")
    val clouds: Clouds,

    @Schema(description = "강수량 정보")
    val rain: Rain? = null,

    @Schema(description = "적설량 정보")
    val snow: Snow? = null,

    @Schema(description = "데이터 시간 (Unix timestamp)")
    val dt: Long,

    @Schema(description = "시스템 정보")
    val sys: Sys,

    @Schema(description = "타임존 (UTC 기준 초)")
    val timezone: Int,

    @Schema(description = "도시 ID")
    val id: Long,

    @Schema(description = "도시명")
    val name: String,

    @Schema(description = "응답 코드")
    val cod: Int
)

@Schema(description = "좌표 정보")
data class Coord(
    @Schema(description = "경도", example = "126.9780")
    val lon: Double,

    @Schema(description = "위도", example = "37.5665")
    val lat: Double
)

@Schema(description = "날씨 상태 정보")
data class Weather(
    @Schema(description = "날씨 ID", example = "800")
    val id: Int,

    @Schema(description = "주요 날씨 (Clear, Rain, Snow 등)", example = "Clear")
    val main: String,

    @Schema(description = "상세 설명", example = "맑음")
    val description: String,

    @Schema(description = "아이콘 코드", example = "01d")
    val icon: String
)

@Schema(description = "주요 날씨 지표")
data class Main(
    @Schema(description = "현재 온도 (섭씨)", example = "23.5")
    val temp: Double,

    @Schema(description = "체감 온도 (섭씨)", example = "24.2")
    @JsonProperty("feels_like")
    val feelsLike: Double,

    @Schema(description = "최저 온도 (섭씨)", example = "21.8")
    @JsonProperty("temp_min")
    val tempMin: Double,

    @Schema(description = "최고 온도 (섭씨)", example = "25.1")
    @JsonProperty("temp_max")
    val tempMax: Double,

    @Schema(description = "기압 (hPa)", example = "1013")
    val pressure: Int,

    @Schema(description = "습도 (%)", example = "65")
    val humidity: Int,

    @Schema(description = "해수면 기압 (hPa)", example = "1013")
    @JsonProperty("sea_level")
    val seaLevel: Int? = null,

    @Schema(description = "지면 기압 (hPa)", example = "1010")
    @JsonProperty("grnd_level")
    val grndLevel: Int? = null
)

@Schema(description = "바람 정보")
data class Wind(
    @Schema(description = "풍속 (m/s)", example = "2.5")
    val speed: Double,

    @Schema(description = "풍향 (도)", example = "180")
    val deg: Int? = null,

    @Schema(description = "돌풍 (m/s)", example = "4.2")
    val gust: Double? = null
)

@Schema(description = "구름 정보")
data class Clouds(
    @Schema(description = "구름량 (%)", example = "25")
    val all: Int
)

@Schema(description = "강수량 정보")
data class Rain(
    @Schema(description = "1시간 강수량 (mm)")
    @JsonProperty("1h")
    val oneHour: Double? = null,

    @Schema(description = "3시간 강수량 (mm)")
    @JsonProperty("3h")
    val threeHours: Double? = null
)

@Schema(description = "적설량 정보")
data class Snow(
    @Schema(description = "1시간 적설량 (mm)")
    @JsonProperty("1h")
    val oneHour: Double? = null,

    @Schema(description = "3시간 적설량 (mm)")
    @JsonProperty("3h")
    val threeHours: Double? = null
)

@Schema(description = "시스템 정보")
data class Sys(
    @Schema(description = "내부 파라미터 타입")
    val type: Int? = null,

    @Schema(description = "내부 파라미터 ID")
    val id: Int? = null,

    @Schema(description = "국가 코드", example = "KR")
    val country: String,

    @Schema(description = "일출 시간 (Unix timestamp)", example = "1685135722")
    val sunrise: Long,

    @Schema(description = "일몰 시간 (Unix timestamp)", example = "1685187483")
    val sunset: Long
)