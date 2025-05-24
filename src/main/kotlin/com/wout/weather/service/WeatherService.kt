package com.wout.weather.service

import com.wout.common.exception.ApiException
import com.wout.common.exception.ErrorCode.WEATHER_DATA_NOT_FOUND
import com.wout.weather.dto.response.WeatherResponse
import com.wout.weather.dto.response.WeatherSummary
import com.wout.weather.entity.enums.KoreanMajorCity
import com.wout.weather.mapper.WeatherMapper
import com.wout.weather.repository.WeatherDataRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * packageName    : com.wout.weather.service
 * fileName       : WeatherService
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 날씨 서비스 (DB 조회 전담)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       WeatherMapper 적용, 중복 로직 제거
 * 25. 5. 25.        MinKyu Park       ApiException 통일, WeatherSummary DTO 적용
 */
@Service
@Transactional(readOnly = true)
class WeatherService(
    private val weatherRepository: WeatherDataRepository,
    private val weatherMapper: WeatherMapper
) {

    /**
     * 사용자 위치 기반 날씨 정보 조회
     */
    fun getWeatherByLocation(userLat: Double, userLon: Double): WeatherResponse {
        val nearestCity = KoreanMajorCity.findNearestCity(userLat, userLon)

        val weatherData = weatherRepository.findLatestByCityName(
            cityName = nearestCity.cityName,
            since = LocalDateTime.now().minusHours(1)
        ) ?: weatherRepository.findLatestByCityName(nearestCity.cityName)
        ?: throw ApiException(
            WEATHER_DATA_NOT_FOUND,
            WEATHER_DATA_NOT_FOUND.message
        )

        return weatherMapper.toResponseDto(weatherData)
    }

    /**
     * 특정 도시의 날씨 정보 조회
     */
    fun getWeatherByCity(cityName: String): WeatherResponse {
        val weatherData = weatherRepository.findLatestByCityName(cityName)
            ?: throw ApiException(
                WEATHER_DATA_NOT_FOUND,
                WEATHER_DATA_NOT_FOUND.message
            )

        return weatherMapper.toResponseDto(weatherData)
    }

    /**
     * 모든 주요 도시의 날씨 정보 조회
     */
    fun getAllMajorCitiesWeather(): List<WeatherResponse> {
        val since = LocalDateTime.now().minusHours(2)

        return KoreanMajorCity.entries.mapNotNull { city ->
            weatherRepository.findLatestByCityName(city.cityName, since)?.let {
                weatherMapper.toResponseDto(it)
            }
        }
    }

    /**
     * 특정 도시의 날씨 히스토리 조회 (최근 24시간)
     */
    fun getWeatherHistory(cityName: String): List<WeatherResponse> {
        val now = LocalDateTime.now()
        val yesterday = now.minusHours(24)

        return weatherRepository.findByCityNameAndDateRange(cityName, yesterday, now)
            .map { weatherMapper.toResponseDto(it) }
    }

    /**
     * 현재 날씨 상태 요약 정보 조회
     */
    fun getWeatherSummary(): WeatherSummary {
        val since = LocalDateTime.now().minusHours(1)
        val allCitiesWeather = weatherRepository.findLatestWeatherForAllCities(since)

        if (allCitiesWeather.isEmpty()) {
            return WeatherSummary(
                availableCities = 0,
                averageTemperature = 0.0,
                maxTemperature = 0.0,
                minTemperature = 0.0,
                averageHumidity = 0.0,
                averageWindSpeed = 0.0,
                maxTemperatureCity = "",
                minTemperatureCity = "",
                lastUpdated = LocalDateTime.now(),
                message = "현재 날씨 데이터를 조회할 수 없습니다."
            )
        }

        val avgTemperature = allCitiesWeather.map { it.temperature }.average()
        val maxTemperature = allCitiesWeather.maxOf { it.temperature }
        val minTemperature = allCitiesWeather.minOf { it.temperature }
        val avgHumidity = allCitiesWeather.map { it.humidity }.average()
        val avgWindSpeed = allCitiesWeather.map { it.windSpeed }.average()

        val maxTempCity = allCitiesWeather.maxByOrNull { it.temperature }?.cityName ?: ""
        val minTempCity = allCitiesWeather.minByOrNull { it.temperature }?.cityName ?: ""
        val lastUpdated = allCitiesWeather.maxOf { it.createdAt }

        // 날씨 상황에 따른 메시지 생성
        val message = when {
            avgTemperature >= 25 -> "전국적으로 더운 날씨입니다"
            avgTemperature <= 10 -> "전국적으로 추운 날씨입니다"
            maxTemperature - minTemperature >= 15 -> "지역별 기온차가 큰 날씨입니다"
            else -> "전국적으로 쾌적한 날씨입니다"
        }

        return WeatherSummary(
            availableCities = allCitiesWeather.size,
            averageTemperature = String.format("%.1f", avgTemperature).toDouble(),
            maxTemperature = maxTemperature,
            minTemperature = minTemperature,
            averageHumidity = String.format("%.1f", avgHumidity).toDouble(),
            averageWindSpeed = String.format("%.1f", avgWindSpeed).toDouble(),
            maxTemperatureCity = maxTempCity,
            minTemperatureCity = minTempCity,
            lastUpdated = lastUpdated,
            message = message
        )
    }
}