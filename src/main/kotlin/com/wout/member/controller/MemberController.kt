package com.wout.member.controller

import com.wout.common.response.ApiResponse
import com.wout.member.dto.request.LocationUpdateRequest
import com.wout.member.dto.request.NicknameUpdateRequest
import com.wout.member.dto.request.WeatherPreferenceSetupRequest
import com.wout.member.dto.request.WeatherPreferenceUpdateRequest
import com.wout.member.dto.response.MemberResponse
import com.wout.member.dto.response.MemberStatusResponse
import com.wout.member.dto.response.MemberWithPreferenceResponse
import com.wout.member.dto.response.WeatherPreferenceResponse
import com.wout.member.service.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.*

/**
 * packageName    : com.wout.member.controller
 * fileName       : MemberController
 * author         : MinKyu Park
 * date           : 2025-06-08
 * description    : 회원 관리 API 컨트롤러 (더티체킹 패턴 적용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       개발 가이드에 맞게 수정
 * 2025-06-08        MinKyu Park       더티체킹 패턴 적용
 */
@RestController
@RequestMapping("/api/members")
@Tag(name = "Member API", description = "회원 관리 API")
class MemberController(
    private val memberService: MemberService
) {

    // ===== 회원 상태 조회 API (스플래시용) =====

    @Operation(
        summary = "회원 상태 확인",
        description = "회원 존재 여부 및 설정 완료 상태를 확인합니다. (스플래시 화면용)"
    )
    @GetMapping("/status/{deviceId}")
    fun checkMemberStatus(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String
    ): ApiResponse<MemberStatusResponse> {
        val result = memberService.checkMemberStatus(deviceId)
        return ApiResponse.success(result)
    }

    // ===== 회원 생성 + 민감도 설정 API =====

    @Operation(
        summary = "민감도 설정과 동시에 회원 생성",
        description = "신규 사용자의 회원 생성 + 5단계 민감도 설정을 원자적으로 처리합니다."
    )
    @PostMapping("/{deviceId}/setup-with-preference")
    fun setupWithPreference(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String,
        @Valid @RequestBody request: WeatherPreferenceSetupRequest
    ): ApiResponse<WeatherPreferenceResponse> {
        val result = memberService.setupWeatherPreferenceWithMember(deviceId, request)
        return ApiResponse.success(result)
    }

    // ===== 회원 정보 조회 API =====

    @Operation(
        summary = "회원 정보 + 민감도 통합 조회",
        description = "기존 회원의 정보와 날씨 선호도를 함께 조회합니다. (대시보드용)"
    )
    @GetMapping("/{deviceId}")
    fun getMemberWithPreference(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String
    ): ApiResponse<MemberWithPreferenceResponse> {
        val result = memberService.getMemberWithPreference(deviceId)
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "회원 정보 조회",
        description = "기본 회원 정보만 조회합니다."
    )
    @GetMapping("/{deviceId}/info")
    fun getMemberInfo(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String
    ): ApiResponse<MemberResponse> {
        val result = memberService.getMemberByDeviceId(deviceId)
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "날씨 선호도 조회",
        description = "회원의 날씨 선호도 설정을 조회합니다."
    )
    @GetMapping("/{deviceId}/weather-preference")
    fun getWeatherPreference(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String
    ): ApiResponse<WeatherPreferenceResponse> {
        val result = memberService.getWeatherPreference(deviceId)
        return ApiResponse.success(result)
    }

    // ===== 회원 정보 수정 API (✅ 더티체킹 적용) =====

    @Operation(
        summary = "닉네임 수정",
        description = "회원의 닉네임을 수정합니다."
    )
    @PatchMapping("/{deviceId}/nickname")
    fun updateNickname(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String,
        request: NicknameUpdateRequest
    ): ApiResponse<MemberResponse> {
        val result = memberService.updateNickname(deviceId, request.nickname)
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "기본 위치 정보 수정",
        description = "회원의 기본 위치 정보를 수정합니다."
    )
    @PatchMapping("/{deviceId}/location")
    fun updateLocation(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String,
        @Valid @RequestBody request: LocationUpdateRequest
    ): ApiResponse<MemberResponse> {
        val result = memberService.updateLocation(deviceId, request)
        return ApiResponse.success(result)
    }

    // ===== 날씨 선호도 수정 API (✅ 더티체킹 적용) =====

    @Operation(
        summary = "날씨 선호도 수정",
        description = "기존 회원의 날씨 선호도를 수정합니다. (마이페이지 → 민감도 재조정용)"
    )
    @PutMapping("/{deviceId}/weather-preference")
    fun updateWeatherPreference(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String,
        @Valid @RequestBody request: WeatherPreferenceUpdateRequest
    ): ApiResponse<WeatherPreferenceResponse> {
        val result = memberService.updateWeatherPreference(deviceId, request)
        return ApiResponse.success(result)
    }

    // ===== 회원 탈퇴 API (✅ 더티체킹 적용) =====

    @Operation(
        summary = "회원 탈퇴",
        description = "회원 계정을 비활성화합니다."
    )
    @DeleteMapping("/{deviceId}")
    fun deactivateMember(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable @NotBlank(message = "Device ID는 필수입니다") deviceId: String
    ): ApiResponse<MemberResponse> {
        val result = memberService.deactivateMember(deviceId)
        return ApiResponse.success(result)
    }
}