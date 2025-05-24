package com.wout.weather.mapper

import com.wout.weather.dto.response.*
import com.wout.weather.entity.WeatherData
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * packageName    : com.wout.weather.mapper
 * fileName       : WeatherMapper
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : WeatherData 엔티티를 WeatherResponse로 변환하는 매퍼
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 * 25. 5. 25.        MinKyu Park       시간대 처리 개선 (설정 가능한 타임존)
 */
@Component
class WeatherMapper(
    @Value("\${app.timezone:Asia/Seoul}")
    private val timezoneString: String
) {

    private val zoneId: ZoneId by lazy { ZoneId.of(timezoneString) }

    /**
     * WeatherData 엔티티를 WeatherResponse로 변환
     */
    fun toResponseDto(weatherData: WeatherData): WeatherResponse {
        return WeatherResponse(
            location = LocationDto(
                latitude = weatherData.latitude,
                longitude = weatherData.longitude,
                cityName = weatherData.cityName
            ),
            temperature = TemperatureDto(
                current = weatherData.temperature,
                feelsLike = weatherData.feelsLike,
                min = weatherData.tempMin,
                max = weatherData.tempMax
            ),
            atmosphere = AtmosphereDto(
                humidity = weatherData.humidity,
                pressure = weatherData.pressure,
                seaLevelPressure = weatherData.seaLevelPressure,
                groundLevelPressure = weatherData.groundLevelPressure
            ),
            wind = WindDto(
                speed = weatherData.windSpeed,
                direction = weatherData.windDirection,
                gust = weatherData.windGust
            ),
            weather = WeatherStateDto(
                main = weatherData.weatherMain,
                description = weatherData.weatherDescription,
                icon = weatherData.weatherIcon
            ),
            precipitation = createPrecipitationDto(weatherData),
            snowfall = createSnowfallDto(weatherData),
            airQuality = createAirQualityDto(weatherData),
            sun = createSunInfoDto(weatherData),
            uvIndex = weatherData.uvIndex,
            visibility = weatherData.visibility?.div(1000.0), // 미터를 킬로미터로 변환
            cloudiness = weatherData.cloudiness,
            dataTime = convertTimestampToLocalDateTime(weatherData.dataTimestamp),
            updatedAt = weatherData.updatedAt
        )
    }

    private fun createPrecipitationDto(weatherData: WeatherData): PrecipitationDto? {
        return if (weatherData.rain1h != null || weatherData.rain3h != null) {
            PrecipitationDto(
                oneHour = weatherData.rain1h,
                threeHours = weatherData.rain3h
            )
        } else null
    }

    private fun createSnowfallDto(weatherData: WeatherData): SnowfallDto? {
        return if (weatherData.snow1h != null || weatherData.snow3h != null) {
            SnowfallDto(
                oneHour = weatherData.snow1h,
                threeHours = weatherData.snow3h
            )
        } else null
    }

    private fun createAirQualityDto(weatherData: WeatherData): AirQualityDto? {
        return if (weatherData.pm25 != null || weatherData.pm10 != null) {
            AirQualityDto(
                pm25 = weatherData.pm25,
                pm10 = weatherData.pm10,
                co = weatherData.co,
                no2 = weatherData.no2,
                ozone = weatherData.ozone,
                so2 = weatherData.so2,
                airQualityIndex = calculateAirQualityIndex(weatherData.pm25, weatherData.pm10)
            )
        } else null
    }

    private fun createSunInfoDto(weatherData: WeatherData): SunInfoDto? {
        return if (weatherData.sunrise != null && weatherData.sunset != null) {
            SunInfoDto(
                sunrise = convertTimestampToLocalDateTime(weatherData.sunrise ?: 0),
                sunset = convertTimestampToLocalDateTime(weatherData.sunset ?: 0)
            )
        } else null
    }

    /**
     * Unix timestamp를 설정된 시간대의 LocalDateTime으로 변환
     * 서머타임 등 시간대 변경사항을 자동으로 처리
     */
    private fun convertTimestampToLocalDateTime(timestamp: Long): LocalDateTime {
        val instant = Instant.ofEpochSecond(timestamp)
        val offset = zoneId.rules.getOffset(instant)
        return LocalDateTime.ofEpochSecond(timestamp, 0, offset)
    }

    /**
     * 미세먼지 수치 기반 대기질 지수 계산 (한국 환경공단 기준)
     * 참고: 한국 환경공단 대기환경기준 (24시간 평균 기준)
     * - PM2.5: 좋음(0-15), 보통(16-35), 나쁨(36-75), 매우나쁨(76+)
     * - PM10: 좋음(0-30), 보통(31-80), 나쁨(81-150), 매우나쁨(151+)
     */
    private fun calculateAirQualityIndex(pm25: Double?, pm10: Double?): String {
        val pm25Level = pm25?.let {
            when {
                it <= 15.0 -> 1  // 좋음 (0-15 µg/m³)
                it <= 35.0 -> 2  // 보통 (16-35 µg/m³)
                it <= 75.0 -> 3  // 나쁨 (36-75 µg/m³)
                else -> 4        // 매우나쁨 (76+ µg/m³)
            }
        } ?: 1

        val pm10Level = pm10?.let {
            when {
                it <= 30.0 -> 1   // 좋음 (0-30 µg/m³)
                it <= 80.0 -> 2   // 보통 (31-80 µg/m³)
                it <= 150.0 -> 3  // 나쁨 (81-150 µg/m³)
                else -> 4         // 매우나쁨 (151+ µg/m³)
            }
        } ?: 1

        // PM2.5와 PM10 중 더 나쁜 수치를 기준으로 판정
        val maxLevel = maxOf(pm25Level, pm10Level)

        return when (maxLevel) {
            1 -> "좋음"
            2 -> "보통"
            3 -> "나쁨"
            4 -> "매우나쁨"
            else -> "알수없음"
        }
    }
}