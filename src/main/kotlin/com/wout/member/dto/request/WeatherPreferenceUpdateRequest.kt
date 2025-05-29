package com.wout.member.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * packageName    : com.wout.member.dto.request
 * fileName       : WeatherPreferenceUpdateRequest
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 날씨 선호도 부분 수정 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
data class WeatherPreferenceUpdateRequest(
    // 수정할 필드만 포함 (null이면 수정하지 않음)

    @field:Min(value = 10, message = "쾌적 온도는 10도 이상이어야 합니다")
    @field:Max(value = 30, message = "쾌적 온도는 30도 이하여야 합니다")
    val comfortTemperature: Int? = null,

    @field:Min(value = 1, message = "온도 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "온도 가중치는 100 이하여야 합니다")
    val temperatureWeight: Int? = null,

    @field:Min(value = 1, message = "습도 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "습도 가중치는 100 이하여야 합니다")
    val humidityWeight: Int? = null,

    @field:Min(value = 1, message = "바람 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "바람 가중치는 100 이하여야 합니다")
    val windWeight: Int? = null,

    @field:Min(value = 1, message = "자외선 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "자외선 가중치는 100 이하여야 합니다")
    val uvWeight: Int? = null,

    @field:Min(value = 1, message = "대기질 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "대기질 가중치는 100 이하여야 합니다")
    val airQualityWeight: Int? = null
) {
    /**
     * 수정할 내용이 있는지 확인
     */
    fun hasUpdates(): Boolean {
        return comfortTemperature != null ||
                temperatureWeight != null ||
                humidityWeight != null ||
                windWeight != null ||
                uvWeight != null ||
                airQualityWeight != null
    }
}