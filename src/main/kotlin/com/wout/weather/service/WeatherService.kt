package com.wout.weather.service

import com.wout.weather.dto.response.WeatherResponseDto
import com.wout.weather.entity.KoreanMajorCity
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
    fun getWeatherByLocation(userLat: Double, userLon: Double): WeatherResponseDto {
        val nearestCity = KoreanMajorCity.findNearestCity(userLat, userLon)

        val weatherData = weatherRepository.findLatestByCityName(
            cityName = nearestCity.cityName,
            since = LocalDateTime.now().minusHours(1)
        ) ?: weatherRepository.findLatestByCityName(nearestCity.cityName)
        ?: throw IllegalStateException("${nearestCity.cityName}의 날씨 데이터를 찾을 수 없습니다.")

        return weatherMapper.toResponseDto(weatherData)
    }

    /**
     * 특정 도시의 날씨 정보 조회
     */
    fun getWeatherByCity(cityName: String): WeatherResponseDto {
        val weatherData = weatherRepository.findLatestByCityName(cityName)
            ?: throw IllegalArgumentException("${cityName}의 날씨 데이터를 찾을 수 없습니다.")

        return weatherMapper.toResponseDto(weatherData)
    }

    /**
     * 모든 주요 도시의 날씨 정보 조회
     */
    fun getAllMajorCitiesWeather(): List<WeatherResponseDto> {
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
    fun getWeatherHistory(cityName: String): List<WeatherResponseDto> {
        val now = LocalDateTime.now()
        val yesterday = now.minusHours(24)

        return weatherRepository.findByCityNameAndDateRange(cityName, yesterday, now)
            .map { weatherMapper.toResponseDto(it) }
    }

    /**
     * 현재 날씨 상태 요약 정보 조회
     */
    fun getWeatherSummary(): Map<String, Any> {
        val since = LocalDateTime.now().minusHours(1)
        val allCitiesWeather = weatherRepository.findLatestWeatherForAllCities(since)

        if (allCitiesWeather.isEmpty()) {
            return mapOf(
                "message" to "현재 날씨 데이터를 조회할 수 없습니다.",
                "availableCities" to 0
            )
        }

        val avgTemperature = allCitiesWeather.map { it.temperature }.average()
        val maxTemperature = allCitiesWeather.maxOf { it.temperature }
        val minTemperature = allCitiesWeather.minOf { it.temperature }
        val avgHumidity = allCitiesWeather.map { it.humidity }.average()

        return mapOf(
            "availableCities" to allCitiesWeather.size,
            "averageTemperature" to String.format("%.1f", avgTemperature),
            "maxTemperature" to maxTemperature,
            "minTemperature" to minTemperature,
            "averageHumidity" to String.format("%.1f", avgHumidity),
            "lastUpdated" to allCitiesWeather.maxOf { it.createdAt }
        )
    }
}