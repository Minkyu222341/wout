package com.wout.member.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size

/**
 * packageName    : com.wout.member.dto.request
 * fileName       : MemberUpdateRequest
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 회원 정보 수정 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
data class MemberUpdateRequest(
    @field:Size(max = 50, message = "닉네임은 50자를 초과할 수 없습니다")
    val nickname: String? = null,

    @field:DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
    val latitude: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
    val longitude: Double? = null,

    @field:Size(max = 100, message = "지역명은 100자를 초과할 수 없습니다")
    val cityName: String? = null
) {
    /**
     * 위치 정보 유효성 검증
     */
    fun isValidLocation(): Boolean {
        return if (latitude != null || longitude != null) {
            latitude != null && longitude != null
        } else {
            true
        }
    }

    /**
     * 수정할 내용이 있는지 확인
     */
    fun hasUpdates(): Boolean {
        return nickname != null ||
                latitude != null ||
                longitude != null ||
                cityName != null
    }
}