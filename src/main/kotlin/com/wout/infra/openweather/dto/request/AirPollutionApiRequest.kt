package com.wout.infra.openweather.dto.request

/**
 * packageName    : com.wout.infra.openweather.dto.request
 * fileName       : AirPollutionApiRequest
 * author         : MinKyu Park
 * date           : 25. 5. 22.
 * description    : OpenWeather 대기오염 API 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 22.        MinKyu Park       최초 생성
 */
/**
 * AirPollution API 요청 DTO
 */
data class AirPollutionApiRequest(
    val lat: Double,
    val lon: Double,
    val appid: String
)