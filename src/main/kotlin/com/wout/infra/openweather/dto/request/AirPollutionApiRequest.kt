package com.wout.infra.openweather.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank

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
 * 25. 5. 24.        MinKyu Park       위도/경도 유효성 검증 추가
 */
/**
 * AirPollution API 요청 DTO
 */
data class AirPollutionApiRequest(
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
    val lat: Double,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
    val lon: Double,

    @field:NotBlank(message = "API 키는 필수입니다")
    val appid: String
)