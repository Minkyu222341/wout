package com.wout.infra.openweather.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

/**
 * packageName    : com.wout.infra.openweather.config
 * fileName       : OpenWeatherConfig
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : OpenWeather API 관련 설정 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Configuration
@EnableFeignClients(basePackages = ["com.wout.infra.openweather.client"])
class OpenWeatherConfig {

    @Value("\${openweather.api.key}")
    lateinit var apiKey: String

    @Value("\${openweather.api.base-url}")
    lateinit var baseUrl: String

    // 필요한 경우 추가 설정이나 Bean 등록
}