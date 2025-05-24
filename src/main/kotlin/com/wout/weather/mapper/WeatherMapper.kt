package com.wout.weather.mapper

import com.wout.weather.dto.response.*
import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * packageName    : com.wout.weather.mapper
 * fileName       : WeatherMapper
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : WeatherData 엔티티를 WeatherResponseDto로 변환하는 매퍼
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 */
@Component
class WeatherMapper {

    /**
     * WeatherData 엔티티를 WeatherResponseDto로 변환
     */
    fun toResponseDto(weatherData: WeatherData): WeatherResponseDto {
        return WeatherResponseDto(
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
            dataTime = LocalDateTime.ofEpochSecond(weatherData.dataTimestamp, 0, ZoneOffset.ofHours(9)),
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
                sunrise = LocalDateTime.ofEpochSecond(weatherData.sunrise!!, 0, ZoneOffset.ofHours(9)),
                sunset = LocalDateTime.ofEpochSecond(weatherData.sunset!!, 0, ZoneOffset.ofHours(9))
            )
        } else null
    }

    /**
     * 미세먼지 수치 기반 대기질 지수 계산 (WHO 기준)
     */
    private fun calculateAirQualityIndex(pm25: Double?, pm10: Double?): String {
        val pm25Level = pm25?.let {
            when {
                it <= 15 -> 1  // 좋음
                it <= 35 -> 2  // 보통
                it <= 75 -> 3  // 나쁨
                else -> 4      // 매우나쁨
            }
        } ?: 1

        val pm10Level = pm10?.let {
            when {
                it <= 30 -> 1   // 좋음
                it <= 80 -> 2   // 보통
                it <= 150 -> 3  // 나쁨
                else -> 4       // 매우나쁨
            }
        } ?: 1

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