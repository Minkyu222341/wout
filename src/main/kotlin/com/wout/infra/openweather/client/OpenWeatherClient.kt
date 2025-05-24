package com.wout.infra.openweather.client

import com.wout.infra.openweather.dto.request.AirPollutionApiRequest
import com.wout.infra.openweather.dto.request.WeatherApiRequest
import com.wout.infra.openweather.dto.response.AirPollutionResponse
import com.wout.infra.openweather.dto.response.OpenWeatherResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.GetMapping

/**
 * packageName    : com.wout.infra.openweather.client
 * fileName       : OpenWeatherClient
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : OpenWeather API 통합 클라이언트 (날씨 + 대기질)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       AirPollution API 통합
 */
@FeignClient(
    name = "openWeatherClient",           // API 호출용 (변경 금지)
    contextId = "openWeatherClient",      // Bean 충돌 방지용
    url = "\${openweather.api.base-url}",
    configuration = [OpenWeatherClientConfig::class]
)
interface OpenWeatherClient {

    /**
     * 현재 날씨 정보 조회
     * https://api.openweathermap.org/data/2.5/weather
     */
    @GetMapping("/weather")
    fun getCurrentWeather(
        @SpringQueryMap request: WeatherApiRequest
    ): OpenWeatherResponse

    /**
     * 대기질 정보 조회
     * https://api.openweathermap.org/data/2.5/air_pollution
     */
    @GetMapping("/air_pollution")
    fun getAirPollution(
        @SpringQueryMap request: AirPollutionApiRequest
    ): AirPollutionResponse
}