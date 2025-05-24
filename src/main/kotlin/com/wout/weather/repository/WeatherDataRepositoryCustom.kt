package com.wout.weather.repository

import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * packageName    : com.wout.weather.repository
 * fileName       : WeatherDataRepositoryCustom
 * author         : MinKyu Park
 * date           : 2025-05-24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-24        MinKyu Park       최초 생성
 */
@Repository
interface WeatherDataRepositoryCustom {
    fun findLatestByCityName(cityName: String, since: LocalDateTime): WeatherData?
    fun findLatestByCityName(cityName: String): WeatherData?
    fun findNearestWeatherData(latitude: Double, longitude: Double, since: LocalDateTime): WeatherData?
    fun findLatestWeatherForAllCities(since: LocalDateTime): List<WeatherData>
    fun deleteByCreatedAtBefore(cutoffDate: LocalDateTime): Long
    fun findByCityNameAndDateRange(cityName: String, startDate: LocalDateTime, endDate: LocalDateTime): List<WeatherData>
    fun countByCityName(cityName: String): Long
}