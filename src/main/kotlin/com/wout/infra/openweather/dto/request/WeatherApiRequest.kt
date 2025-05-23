package com.wout.infra.openweather.dto.request

/**
 * packageName    : com.wout.infra.openweather.dto.request
 * fileName       : WeatherApiRequest
 * author         : MinKyu Park
 * date           : 25. 5. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 22.        MinKyu Park       최초 생성
 */
/**
 * OpenWeather API 공통 요청 DTO
 */
data class WeatherApiRequest(
    val lat: Double,
    val lon: Double,
    val appid: String,
    val units: String = "metric",
    val lang: String = "kr"
)