package com.wout.weather.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * packageName    : com.wout.weather.dto.response
 * fileName       : WeatherResponseDto
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 날씨 정보 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       OpenWeatherMap 전체 필드 추가
 */
@Schema(description = "날씨 정보 응답")
data class WeatherResponseDto(
    @Schema(description = "위치 정보")
    val location: LocationDto,

    @Schema(description = "온도 정보")
    val temperature: TemperatureDto,

    @Schema(description = "대기 정보")
    val atmosphere: AtmosphereDto,

    @Schema(description = "바람 정보")
    val wind: WindDto,

    @Schema(description = "날씨 상태")
    val weather: WeatherStateDto,

    @Schema(description = "강수량 정보")
    val precipitation: PrecipitationDto?,

    @Schema(description = "적설량 정보")
    val snowfall: SnowfallDto?,

    @Schema(description = "대기질 정보")
    val airQuality: AirQualityDto?,

    @Schema(description = "태양 정보")
    val sun: SunInfoDto?,

    @Schema(description = "자외선 지수", example = "5.2")
    val uvIndex: Double?,

    @Schema(description = "가시거리 (km)", example = "10.0")
    val visibility: Double?,

    @Schema(description = "구름량 (%)", example = "75")
    val cloudiness: Int,

    @Schema(description = "데이터 측정 시간")
    val dataTime: LocalDateTime,

    @Schema(description = "업데이트 시간")
    val updatedAt: LocalDateTime
)

@Schema(description = "위치 정보")
data class LocationDto(
    @Schema(description = "위도", example = "37.5665")
    val latitude: Double,

    @Schema(description = "경도", example = "126.9780")
    val longitude: Double,

    @Schema(description = "도시명", example = "Seoul")
    val cityName: String
)

@Schema(description = "온도 정보")
data class TemperatureDto(
    @Schema(description = "현재 온도 (섭씨)", example = "23.5")
    val current: Double,

    @Schema(description = "체감 온도 (섭씨)", example = "24.2")
    val feelsLike: Double,

    @Schema(description = "최저 온도 (섭씨)", example = "18.3")
    val min: Double,

    @Schema(description = "최고 온도 (섭씨)", example = "27.1")
    val max: Double
)

@Schema(description = "대기 정보")
data class AtmosphereDto(
    @Schema(description = "습도 (%)", example = "65")
    val humidity: Int,

    @Schema(description = "기압 (hPa)", example = "1013")
    val pressure: Int,

    @Schema(description = "해수면 기압 (hPa)", example = "1013")
    val seaLevelPressure: Int?,

    @Schema(description = "지면 기압 (hPa)", example = "1008")
    val groundLevelPressure: Int?
)

@Schema(description = "바람 정보")
data class WindDto(
    @Schema(description = "풍속 (m/s)", example = "2.5")
    val speed: Double,

    @Schema(description = "풍향 (도)", example = "180")
    val direction: Int?,

    @Schema(description = "돌풍 (m/s)", example = "4.2")
    val gust: Double?
)

@Schema(description = "날씨 상태")
data class WeatherStateDto(
    @Schema(description = "날씨 상태 (Clear, Rain 등)", example = "Clear")
    val main: String,

    @Schema(description = "날씨 상세 설명", example = "맑음")
    val description: String,

    @Schema(description = "날씨 아이콘 코드", example = "01d")
    val icon: String
)

@Schema(description = "강수량 정보")
data class PrecipitationDto(
    @Schema(description = "1시간 강수량 (mm)", example = "2.5")
    val oneHour: Double?,

    @Schema(description = "3시간 강수량 (mm)", example = "7.8")
    val threeHours: Double?
)

@Schema(description = "적설량 정보")
data class SnowfallDto(
    @Schema(description = "1시간 적설량 (mm)", example = "1.2")
    val oneHour: Double?,

    @Schema(description = "3시간 적설량 (mm)", example = "3.8")
    val threeHours: Double?
)

@Schema(description = "대기질 정보")
data class AirQualityDto(
    @Schema(description = "미세먼지 PM2.5 (μg/m³)", example = "15.2")
    val pm25: Double?,

    @Schema(description = "미세먼지 PM10 (μg/m³)", example = "38.6")
    val pm10: Double?,

    @Schema(description = "일산화탄소 CO (μg/m³)", example = "233.4")
    val co: Double?,

    @Schema(description = "이산화질소 NO2 (μg/m³)", example = "18.4")
    val no2: Double?,

    @Schema(description = "오존 O3 (μg/m³)", example = "168.8")
    val ozone: Double?,

    @Schema(description = "아황산가스 SO2 (μg/m³)", example = "8.2")
    val so2: Double?,

    @Schema(description = "대기질 지수 (좋음, 보통, 나쁨, 매우나쁨)", example = "보통")
    val airQualityIndex: String
)

@Schema(description = "태양 정보")
data class SunInfoDto(
    @Schema(description = "일출 시간")
    val sunrise: LocalDateTime,

    @Schema(description = "일몰 시간")
    val sunset: LocalDateTime
)