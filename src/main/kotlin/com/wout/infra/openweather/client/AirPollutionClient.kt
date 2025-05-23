package com.wout.infra.openweather.client

import com.wout.infra.openweather.dto.response.AirPollutionResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * packageName    : com.wout.infra.openweather.client
 * fileName       : AirPollutionClient
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : OpenWeather 대기오염 API와 통신하기 위한 Feign 클라이언트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@FeignClient(
    name = "airPollutionClient",
    url = "\${openweather.api.base-url}"
)
interface AirPollutionClient {

    /**
     * 현재 대기오염 조회 API
     */
    @GetMapping("/air_pollution")
    fun getAirPollution(
        @RequestParam("lat") lat: Double,
        @RequestParam("lon") lon: Double,
        @RequestParam("appid") appid: String
    ): AirPollutionResponse
}