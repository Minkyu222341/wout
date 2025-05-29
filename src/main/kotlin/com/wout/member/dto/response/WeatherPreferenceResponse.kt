package com.wout.member.dto.response

import java.time.LocalDateTime

/**
 * packageName    : com.wout.member.dto.response
 * fileName       : WeatherPreferenceResponse
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 날씨 선호도 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
data class WeatherPreferenceResponse(
    val id: Long,
    val memberId: Long,

    // 우선순위 정보
    val priorityFirst: String?,
    val prioritySecond: String?,
    val priorities: List<String>, // 우선순위 리스트로 가공

    // 기본 설정
    val comfortTemperature: Int,
    val skinReaction: String?,
    val humidityReaction: String?,

    // 가중치 설정
    val temperatureWeight: Int,
    val humidityWeight: Int,
    val windWeight: Int,
    val uvWeight: Int,
    val airQualityWeight: Int,

    // 계산된 값
    val personalTempCorrection: Double,

    // 상태
    val isSetupCompleted: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)