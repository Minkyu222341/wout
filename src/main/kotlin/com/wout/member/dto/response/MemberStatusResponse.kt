package com.wout.member.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.member.dto.response
 * fileName       : MemberStatusResponse
 * author         : MinKyu Park
 * date           : 2025-06-08
 * description    : 회원 상태 확인 응답 DTO (스플래시 화면용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-08        MinKyu Park       최초 생성 (프로세스 개선)
 */
@Schema(description = "회원 상태 확인 응답")
data class MemberStatusResponse(

    @Schema(
        description = "회원 존재 여부",
        example = "true",
        required = true
    )
    val memberExists: Boolean,

    @Schema(
        description = "5단계 민감도 설정 완료 여부",
        example = "false",
        required = true
    )
    val isSetupCompleted: Boolean

) {

    /**
     * 신규 사용자인지 확인
     */
    fun isNewUser(): Boolean = !memberExists

    /**
     * 기존 회원이지만 설정이 미완료인지 확인
     */
    fun isExistingUserWithIncompleteSetup(): Boolean = memberExists && !isSetupCompleted

    /**
     * 모든 설정이 완료된 기존 회원인지 확인
     */
    fun isCompleteMember(): Boolean = memberExists && isSetupCompleted

    /**
     * 사용자 상태를 문자열로 반환 (디버깅/로깅용)
     */
    fun getUserStatusDescription(): String {
        return when {
            isNewUser() -> "신규 사용자"
            isExistingUserWithIncompleteSetup() -> "기존 회원 (설정 미완료)"
            isCompleteMember() -> "기존 회원 (설정 완료)"
            else -> "알 수 없는 상태"
        }
    }
}