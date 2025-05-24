package com.wout.schedule.mapper

import com.wout.infra.openweather.dto.response.AirPollutionResponse
import com.wout.infra.openweather.dto.response.OpenWeatherResponse
import com.wout.infra.openweather.dto.response.UVIndexResponse
import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.schedule.mapper
 * fileName       : WeatherApiMapper
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : OpenWeather API 응답을 WeatherData 엔티티로 변환하는 매퍼
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       null safety 체크 추가
 * 25. 5. 24.        MinKyu Park       Builder 패턴 적용
 * 25. 5. 24.        MinKyu Park       UV Index API 추가
 */
@Component
class WeatherApiMapper {

    /**
     * OpenWeather API 응답을 WeatherData 엔티티로 변환
     */
    fun toWeatherData(
        latitude: Double,
        longitude: Double,
        cityName: String,
        weatherResponse: OpenWeatherResponse,
        airResponse: AirPollutionResponse,
        uvResponse: UVIndexResponse? = null  // UV 데이터는 선택적
    ): WeatherData {
        val weather = weatherResponse.weather.firstOrNull()
        val airData = airResponse.list.firstOrNull()?.components

        return WeatherData.builder()
            .location(
                latitude = latitude,
                longitude = longitude,
                cityName = cityName
            )
            .temperature(
                current = weatherResponse.main.temp,
                feelsLike = weatherResponse.main.feelsLike,
                min = weatherResponse.main.tempMin,
                max = weatherResponse.main.tempMax
            )
            .atmosphere(
                humidity = weatherResponse.main.humidity,
                pressure = weatherResponse.main.pressure,
                seaLevel = weatherResponse.main.seaLevel,
                groundLevel = weatherResponse.main.grndLevel
            )
            .wind(
                speed = weatherResponse.wind.speed,
                direction = weatherResponse.wind.deg,
                gust = weatherResponse.wind.gust
            )
            .weather(
                main = weather?.main ?: "Unknown",
                description = weather?.description ?: "No description",
                icon = weather?.icon ?: "unknown"
            )
            .visibility(weatherResponse.visibility)
            .cloudiness(weatherResponse.clouds.all)
            .precipitation(
                oneHour = weatherResponse.rain?.oneHour,
                threeHours = weatherResponse.rain?.threeHours
            )
            .snowfall(
                oneHour = weatherResponse.snow?.oneHour,
                threeHours = weatherResponse.snow?.threeHours
            )
            .uvIndex(uvResponse?.value)
            .airQuality(
                pm25 = airData?.pm25 ?: 0.0,
                pm10 = airData?.pm10 ?: 0.0,
                co = airData?.co ?: 0.0,
                no2 = airData?.no2 ?: 0.0,
                ozone = airData?.o3 ?: 0.0,
                so2 = airData?.so2 ?: 0.0
            )
            .sunInfo(
                sunrise = weatherResponse.sys.sunrise,
                sunset = weatherResponse.sys.sunset
            )
            .dataTimestamp(weatherResponse.dt)
            .build()
    }
}