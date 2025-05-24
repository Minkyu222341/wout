package com.wout.infra.openweather.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * packageName    : com.wout.infra.openweather.dto.request
 * fileName       : WeatherApiRequest
 * author         : MinKyu Park
 * date           : 25. 5. 22.
 * description    : OpenWeather API 공통 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 22.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       유효성 검증 및 일관성 개선
 */
/**
 * OpenWeather API 공통 요청 DTO
 */
data class WeatherApiRequest(
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
    val lat: Double,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    val lon: Double,

    @field:NotBlank(message = "API 키는 필수입니다")
    val appid: String,

    @field:Pattern(
        regexp = "^(standard|metric|imperial)$",
        message = "units는 standard, metric, imperial 중 하나여야 합니다"
    )
    val units: String = "metric",

    @field:Pattern(
        regexp = "^[a-z]{2}$",
        message = "언어 코드는 2자리 소문자여야 합니다 (예: kr, en)"
    )
    val lang: String = "kr"
)