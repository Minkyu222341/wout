package com.wout.member.service

import com.wout.common.exception.ApiException
import com.wout.common.exception.ErrorCode
import com.wout.member.dto.request.LocationUpdateRequest
import com.wout.member.dto.request.MemberCreateRequest
import com.wout.member.dto.request.WeatherPreferenceSetupRequest
import com.wout.member.dto.request.WeatherPreferenceUpdateRequest
import com.wout.member.dto.response.MemberResponse
import com.wout.member.dto.response.MemberWithPreferenceResponse
import com.wout.member.dto.response.WeatherPreferenceResponse
import com.wout.member.entity.Member
import com.wout.member.mapper.MemberMapper
import com.wout.member.mapper.WeatherPreferenceMapper
import com.wout.member.repository.MemberRepository
import com.wout.member.repository.WeatherPreferenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * packageName    : com.wout.member.service
 * fileName       : MemberService
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 회원 관리 비즈니스 로직 처리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val weatherPreferenceRepository: WeatherPreferenceRepository,
    private val memberMapper: MemberMapper,
    private val weatherPreferenceMapper: WeatherPreferenceMapper
) {

    /**
     * 앱 실행 시 deviceId로 기존 사용자 확인 또는 자동 회원가입
     */
    @Transactional
    fun getOrCreateMember(request: MemberCreateRequest): MemberWithPreferenceResponse {
        val deviceId = request.deviceId


        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        // 기존 사용자 확인
        val member = memberRepository.findByDeviceId(deviceId)
            .orElseGet {
                // 없으면 자동 회원가입
                val newMember = Member.create(deviceId)
                memberRepository.save(newMember)
            }

        // 날씨 선호도 확인
        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)
            .orElse(null)

        return weatherPreferenceMapper.toMemberWithPreferenceResponse(member, weatherPreference)
    }

    /**
     * 회원 정보 조회 (deviceId 기반)
     */
    fun getMemberByDeviceId(deviceId: String): MemberResponse {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND) }

        return memberMapper.toResponse(member)
    }

    /**
     * 회원 정보 + 날씨 선호도 통합 조회
     */
    fun getMemberWithPreference(deviceId: String): MemberWithPreferenceResponse {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND) }

        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)
            .orElse(null)

        return weatherPreferenceMapper.toMemberWithPreferenceResponse(member, weatherPreference)
    }


    /**
     * 닉네임 수정
     */
    @Transactional
    fun updateNickname(deviceId: String, nickname: String): MemberResponse {
        val member = findMemberByDeviceId(deviceId)

        // ✅ 닉네임 업데이트만 집중
        val updatedMember = member.updateNickname(nickname)
        val savedMember = memberRepository.save(updatedMember)

        return memberMapper.toResponse(savedMember)
    }

    /**
     * 기본 위치 정보 수정
     */
    @Transactional
    fun updateLocation(deviceId: String, request: LocationUpdateRequest): MemberResponse {
        val member = findMemberByDeviceId(deviceId)

        // ✅ 위치 정보 업데이트만 집중
        val updatedMember = member.updateDefaultLocation(
            latitude = request.latitude,
            longitude = request.longitude,
            cityName = request.cityName
        )
        val savedMember = memberRepository.save(updatedMember)

        return memberMapper.toResponse(savedMember)
    }

    /**
     * 공통 회원 조회 메서드
     */
    private fun findMemberByDeviceId(deviceId: String): Member {
        return memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND, "회원을 찾을 수 없습니다: $deviceId") }
    }

    /**
     * 5단계 날씨 선호도 설정 완료
     */
    @Transactional
    fun setupWeatherPreference(deviceId: String, request: WeatherPreferenceSetupRequest): WeatherPreferenceResponse {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        if (!request.isValidPriorities()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND) }

        if (weatherPreferenceRepository.existsByMemberId(member.id)) {
            throw ApiException(ErrorCode.WEATHER_PREFERENCE_ALREADY_EXISTS)
        }

        val weatherPreference = weatherPreferenceMapper.toEntity(member.id, request)
        val savedPreference = weatherPreferenceRepository.save(weatherPreference)

        return weatherPreferenceMapper.toResponse(savedPreference)
    }

    /**
     * 날씨 선호도 부분 수정 (5단계 설정 완료 후)
     */
    @Transactional
    fun updateWeatherPreference(
        deviceId: String,
        request: WeatherPreferenceUpdateRequest
    ): WeatherPreferenceResponse {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        if (!request.hasUpdates()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND) }

        val existingPreference = weatherPreferenceRepository.findByMemberId(member.id)
            .orElseThrow { ApiException(ErrorCode.SENSITIVITY_PROFILE_NOT_FOUND) }

        val updatedPreference = weatherPreferenceMapper.updateEntity(existingPreference, request)
        val savedPreference = weatherPreferenceRepository.save(updatedPreference)

        return weatherPreferenceMapper.toResponse(savedPreference)
    }

    /**
     * 날씨 선호도 조회
     */
    fun getWeatherPreference(deviceId: String): WeatherPreferenceResponse {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND) }

        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)
            .orElseThrow { ApiException(ErrorCode.SENSITIVITY_PROFILE_NOT_FOUND) }

        return weatherPreferenceMapper.toResponse(weatherPreference)
    }

    /**
     * 회원 탈퇴 (계정 비활성화)
     */
    @Transactional
    fun deactivateMember(deviceId: String): MemberResponse {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND) }

        val deactivatedMember = member.deactivate()
        val savedMember = memberRepository.save(deactivatedMember)

        return memberMapper.toResponse(savedMember)
    }

    /**
     * 체감온도 계산 (날씨 서비스에서 사용)
     */
    fun calculateFeelsLikeTemperature(
        deviceId: String,
        actualTemp: Double,
        windSpeed: Double,
        humidity: Double
    ): Double {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(ErrorCode.MEMBER_NOT_FOUND) }

        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)
            .orElseThrow { ApiException(ErrorCode.SENSITIVITY_PROFILE_NOT_FOUND) }

        return weatherPreference.calculateFeelsLikeTemperature(actualTemp, windSpeed, humidity)
    }

    /**
     * 5단계 설정 완료 여부 확인
     */
    fun isWeatherPreferenceSetupCompleted(deviceId: String): Boolean {
        if (deviceId.isBlank()) {
            throw ApiException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElse(null) ?: return false

        return weatherPreferenceRepository.findByMemberId(member.id)
            .map { it.isSetupCompleted }
            .orElse(false)
    }
}