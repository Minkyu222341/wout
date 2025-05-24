package com.wout.schedule.service

import com.wout.infra.openweather.client.OpenWeatherClient
import com.wout.infra.openweather.dto.request.AirPollutionApiRequest
import com.wout.infra.openweather.dto.request.WeatherApiRequest
import com.wout.schedule.mapper.WeatherApiMapper
import com.wout.weather.entity.WeatherData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * packageName    : com.wout.schedule.service
 * fileName       : OpenWeatherApiService
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : OpenWeather API 호출 및 데이터 변환 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 */
@Service
@Transactional
class OpenWeatherApiService(
    private val openWeatherClient: OpenWeatherClient,  // 통합 클라이언트 하나만!
    private val weatherApiMapper: WeatherApiMapper
) {

    private val log = LoggerFactory.getLogger(OpenWeatherApiService::class.java)

    @Value("\${openweather.api.key}")
    private lateinit var apiKey: String

    /**
     * 외부 API 호출을 통한 날씨 데이터 수집
     */
    fun fetchWeatherData(latitude: Double, longitude: Double, cityName: String): WeatherData {
        log.info("🌤️ ${cityName} 날씨 데이터 수집 시작")
        log.info("📍 좌표: lat=$latitude, lon=$longitude")

        // 🔍 API 키 확인 (첫 3자만 표시)
        val maskedApiKey = if (apiKey.length > 3) "${apiKey.take(3)}***" else "NOT_SET"
        log.info("🔑 API Key: $maskedApiKey")

        return try {
            // 1. 날씨 정보 API 호출
            val weatherRequest = WeatherApiRequest(
                lat = latitude,
                lon = longitude,
                appid = apiKey,
                units = "metric",
                lang = "kr"
            )

            log.info("📡 날씨 API 요청: $weatherRequest")
            val weatherResponse = openWeatherClient.getCurrentWeather(weatherRequest)
            log.info("✅ 날씨 API 응답 성공: ${weatherResponse.name}")

            // 2. 대기질 정보 API 호출 (같은 클라이언트!)
            val airRequest = AirPollutionApiRequest(
                lat = latitude,
                lon = longitude,
                appid = apiKey
            )

            log.info("🌬️ 대기질 API 요청: $airRequest")
            val airResponse = openWeatherClient.getAirPollution(airRequest)
            log.info("✅ 대기질 API 응답 성공: AQI=${airResponse.list.firstOrNull()?.main?.aqi}")

            // 3. API 응답을 WeatherData로 변환
            val weatherData = weatherApiMapper.toWeatherData(
                latitude = latitude,
                longitude = longitude,
                cityName = cityName,
                weatherResponse = weatherResponse,
                airResponse = airResponse
            )

            log.info("🎯 ${cityName} 날씨 데이터 수집 완료!")
            weatherData

        } catch (e: Exception) {
            log.error("❌ ${cityName} 날씨 데이터 수집 실패", e)
            log.error("🔍 실패 원인: ${e.message}")
            log.error("🔍 예외 타입: ${e::class.simpleName}")

            // 🚨 API 키가 문제인지 확인
            if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                log.error("🔑 API 키 문제 의심: $maskedApiKey")
            }

            // 🚨 좌표가 문제인지 확인
            if (e.message?.contains("400") == true || e.message?.contains("Bad Request") == true) {
                log.error("📍 좌표 문제 의심: lat=$latitude, lon=$longitude")
            }

            throw e
        }
    }
}