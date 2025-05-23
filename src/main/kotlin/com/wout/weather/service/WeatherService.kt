package com.wout.weather.service

import com.wout.infra.openweather.client.AirPollutionClient
import com.wout.infra.openweather.client.OpenWeatherClient
import com.wout.weather.dto.response.AirQualityDto
import com.wout.weather.dto.response.WeatherResponseDto
import com.wout.weather.entity.WeatherData
import com.wout.weather.repository.WeatherDataRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * packageName    : com.wout.weather.service
 * fileName       : WeatherService
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 날씨 정보 관련 비즈니스 로직
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Service
class WeatherService(
    private val weatherDataRepository: WeatherDataRepository,
    private val openWeatherClient: OpenWeatherClient,
    private val airPollutionClient: AirPollutionClient
) {
    @Value("\${openweather.api.key}")
    private lateinit var apiKey: String

    /**
     * 특정 위치의 현재 날씨 정보를 조회
     */
    fun getCurrentWeather(latitude: Double, longitude: Double): WeatherResponseDto {
        // 1. 캐시 또는 DB에서 최근 날씨 조회 (15분 이내)
        val fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15)
        val cachedWeather = weatherDataRepository
            .findByLatitudeAndLongitudeAndCreatedAtAfter(latitude, longitude, fifteenMinutesAgo)

        // 2. 캐시된 데이터가 없으면 API 호출
        val weatherData = cachedWeather.orElseGet {
            fetchAndSaveWeatherData(latitude, longitude)
        }

        // 3. 응답 DTO 변환
        return WeatherResponseDto(
            temperature = weatherData.temperature,
            feelsLike = weatherData.feelsLike,
            humidity = weatherData.humidity,
            windSpeed = weatherData.windSpeed,
            weatherState = weatherData.weatherState,
            weatherDescription = weatherData.weatherDescription,
            airQuality = weatherData.pm25?.let {
                AirQualityDto(
                    pm25 = weatherData.pm25,
                    pm10 = weatherData.pm10,
                    airQualityIndex = calculateAirQualityIndex(weatherData.pm25, weatherData.pm10)
                )
            },
            updatedAt = weatherData.createdAt
        )
    }

    /**
     * OpenWeather API를 호출하여 날씨 데이터를 가져와 저장
     */
    private fun fetchAndSaveWeatherData(latitude: Double, longitude: Double): WeatherData {
        // 날씨 API 호출
        val weatherResponse = openWeatherClient.getCurrentWeather(latitude, longitude, apiKey)

        // 대기오염 API 호출
        val airQualityResponse = airPollutionClient.getAirPollution(latitude, longitude, apiKey)

        // 데이터 변환 및 저장
        val newWeatherData = WeatherData.create(
            latitude = latitude,
            longitude = longitude,
            temperature = weatherResponse.main.temp,
            feelsLike = weatherResponse.main.feelsLike,
            humidity = weatherResponse.main.humidity,
            windSpeed = weatherResponse.wind.speed,
            weatherState = weatherResponse.weather[0].main,
            weatherDescription = weatherResponse.weather[0].description,
            pm25 = airQualityResponse.list[0].components.pm25,
            pm10 = airQualityResponse.list[0].components.pm10
        )

        return weatherDataRepository.save(newWeatherData)
    }

    /**
     * 대기질 지수 계산
     */
    private fun calculateAirQualityIndex(pm25: Double?, pm10: Double?): String {
        // PM2.5와 PM10 기준으로 대기질 지수 계산 로직
        // 한국 환경부 기준 적용 예시
        return when {
            pm25 == null || pm10 == null -> "정보없음"
            pm25 <= 15 && pm10 <= 30 -> "좋음"
            pm25 <= 35 && pm10 <= 80 -> "보통"
            pm25 <= 75 && pm10 <= 150 -> "나쁨"
            else -> "매우나쁨"
        }
    }

}