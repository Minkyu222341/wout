package com.wout.member.dto.response

/**
 * packageName    : com.wout.member.dto.response
 * fileName       : MemberWithPreferenceResponse
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 회원 정보 + 날씨 선호도 통합 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
data class MemberWithPreferenceResponse(
    val member: MemberResponse,
    val weatherPreference: WeatherPreferenceResponse?
) {
    /**
     * 날씨 선호도 설정 완료 여부
     */
    val isPreferenceSetupCompleted: Boolean
        get() = weatherPreference?.isSetupCompleted ?: false

    /**
     * 체감온도 계산 가능 여부
     */
    val canCalculateFeelsLike: Boolean
        get() = weatherPreference != null && isPreferenceSetupCompleted
}