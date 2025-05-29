package com.wout.member.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

/**
 * packageName    : com.wout.member.dto.request
 * fileName       : WeatherPreferenceSetupRequest
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 5단계 날씨 선호도 설정 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
data class WeatherPreferenceSetupRequest(
    // === 1단계: 우선순위 (괴로운 날씨 2개 선택) ===
    @field:Pattern(
        regexp = "^(heat|cold|humidity|wind|uv|pollution)$",
        message = "1순위는 heat, cold, humidity, wind, uv, pollution 중 하나여야 합니다"
    )
    val priorityFirst: String? = null,

    @field:Pattern(
        regexp = "^(heat|cold|humidity|wind|uv|pollution)$",
        message = "2순위는 heat, cold, humidity, wind, uv, pollution 중 하나여야 합니다"
    )
    val prioritySecond: String? = null,

    // === 2단계: 체감온도 기준점 ===
    @field:NotNull(message = "쾌적 온도는 필수입니다")
    @field:Min(value = 10, message = "쾌적 온도는 10도 이상이어야 합니다")
    @field:Max(value = 30, message = "쾌적 온도는 30도 이하여야 합니다")
    val comfortTemperature: Int,

    // === 3단계: 피부 반응 ===
    @field:Pattern(
        regexp = "^(high|medium|low)$",
        message = "피부 반응은 high, medium, low 중 하나여야 합니다"
    )
    val skinReaction: String? = null,

    // === 4단계: 습도 민감도 ===
    @field:Pattern(
        regexp = "^(high|medium|low)$",
        message = "습도 반응은 high, medium, low 중 하나여야 합니다"
    )
    val humidityReaction: String? = null,

    // === 5단계: 세부 조정 (선택사항) ===
    @field:Min(value = 1, message = "온도 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "온도 가중치는 100 이하여야 합니다")
    val temperatureWeight: Int = 50,

    @field:Min(value = 1, message = "습도 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "습도 가중치는 100 이하여야 합니다")
    val humidityWeight: Int = 50,

    @field:Min(value = 1, message = "바람 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "바람 가중치는 100 이하여야 합니다")
    val windWeight: Int = 50,

    @field:Min(value = 1, message = "자외선 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "자외선 가중치는 100 이하여야 합니다")
    val uvWeight: Int = 50,

    @field:Min(value = 1, message = "대기질 가중치는 1 이상이어야 합니다")
    @field:Max(value = 100, message = "대기질 가중치는 100 이하여야 합니다")
    val airQualityWeight: Int = 50
) {
    /**
     * 우선순위 중복 검증
     */
    fun isValidPriorities(): Boolean {
        return if (priorityFirst != null && prioritySecond != null) {
            priorityFirst != prioritySecond
        } else {
            true
        }
    }

    /**
     * 우선순위 리스트 반환
     */
    fun getPriorityList(): List<String> {
        return listOfNotNull(priorityFirst, prioritySecond)
    }
}