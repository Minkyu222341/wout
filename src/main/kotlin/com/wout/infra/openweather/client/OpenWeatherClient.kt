package com.wout.infra.openweather.client

import com.wout.infra.openweather.dto.request.WeatherApiRequest
import com.wout.infra.openweather.dto.response.OpenWeatherResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.GetMapping

/**
 * packageName    : com.wout.infra.openweather.client
 * fileName       : OpenWeatherClient
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : OpenWeather API와 통신하기 위한 Feign 클라이언트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@FeignClient(
    name = "openWeatherClient",
    url = "\${openweather.api.base-url}"
)
interface OpenWeatherClient {

    /**
     * 현재 날씨 조회 API
     */
    @GetMapping("/weather")
    fun getCurrentWeather(
        @SpringQueryMap request: WeatherApiRequest
    ): OpenWeatherResponse
}