package com.wout.member.controller

import com.wout.common.response.ApiResponse
import com.wout.member.dto.request.MemberUpdateRequest
import com.wout.member.dto.request.WeatherPreferenceSetupRequest
import com.wout.member.dto.request.WeatherPreferenceUpdateRequest
import com.wout.member.dto.response.MemberResponse
import com.wout.member.dto.response.MemberWithPreferenceResponse
import com.wout.member.dto.response.WeatherPreferenceResponse
import com.wout.member.service.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/**
 * packageName    : com.wout.member.controller
 * fileName       : MemberController
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 회원 관련 REST API 엔드포인트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
@Tag(name = "Member API", description = "회원 관리 및 날씨 선호도 설정 API")
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {

    /**
     * 앱 실행 시 기존 사용자 확인 또는 자동 회원가입
     */
    @Operation(
        summary = "앱 초기화",
        description = "deviceId로 기존 사용자를 확인하거나 자동으로 회원가입을 진행합니다. 앱 최초 실행 시 호출하는 API입니다."
    )
    @PostMapping("/init")
    fun initializeMember(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @RequestParam deviceId: String
    ): ApiResponse<MemberWithPreferenceResponse> {
        val result = memberService.getOrCreateMember(deviceId)
        return ApiResponse.success(result)
    }

    /**
     * 5단계 날씨 선호도 설정 완료
     */
    @Operation(
        summary = "날씨 선호도 설정",
        description = "5단계 질문을 통한 날씨 선호도 초기 설정을 완료합니다."
    )
    @PostMapping("/{deviceId}/weather-preference")
    fun setupWeatherPreference(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable deviceId: String,
        @Valid @RequestBody request: WeatherPreferenceSetupRequest
    ): ApiResponse<WeatherPreferenceResponse> {
        val result = memberService.setupWeatherPreference(deviceId, request)
        return ApiResponse.success(result)
    }

    /**
     * 날씨 선호도 수정
     */
    @Operation(
        summary = "날씨 선호도 수정",
        description = "설정된 날씨 선호도를 수정합니다. (마이페이지에서 사용)"
    )
    @PutMapping("/{deviceId}/weather-preference")
    fun updateWeatherPreference(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable deviceId: String,
        @Valid @RequestBody request: WeatherPreferenceUpdateRequest
    ): ApiResponse<WeatherPreferenceResponse> {
        val result = memberService.updateWeatherPreference(deviceId, request)
        return ApiResponse.success(result)
    }

    /**
     * 회원 프로필 수정 (닉네임, 위치)
     */
    @Operation(
        summary = "회원 프로필 수정",
        description = "회원의 닉네임, 기본 위치 정보를 수정합니다."
    )
    @PutMapping("/{deviceId}/profile")
    fun updateProfile(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable deviceId: String,
        @Valid @RequestBody request: MemberUpdateRequest
    ): ApiResponse<MemberResponse> {
        val result = memberService.updateMemberInfo(deviceId, request)
        return ApiResponse.success(result)
    }

    /**
     * 5단계 설정 완료 여부 확인
     */
    @Operation(
        summary = "설정 완료 여부 확인",
        description = "5단계 날씨 선호도 설정이 완료되었는지 확인합니다."
    )
    @GetMapping("/{deviceId}/weather-preference/status")
    fun checkSetupStatus(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable deviceId: String
    ): ApiResponse<Map<String, Boolean>> {
        val isCompleted = memberService.isWeatherPreferenceSetupCompleted(deviceId)
        val result = mapOf("isSetupCompleted" to isCompleted)
        return ApiResponse.success(result)
    }

    /**
     * 회원 탈퇴 (계정 비활성화)
     */
    @Operation(
        summary = "회원 탈퇴",
        description = "회원 계정을 비활성화합니다. (완전 삭제가 아닌 비활성화 처리)"
    )
    @DeleteMapping("/{deviceId}")
    fun deactivateMember(
        @Parameter(description = "디바이스 고유 식별자", required = true)
        @PathVariable deviceId: String
    ): ApiResponse<MemberResponse> {
        val result = memberService.deactivateMember(deviceId)
        return ApiResponse.success(result)
    }
}