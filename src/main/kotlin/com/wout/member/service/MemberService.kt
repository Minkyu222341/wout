package com.wout.member.service

import com.wout.common.exception.ApiException
import com.wout.common.exception.ErrorCode.*
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
 * date           : 2025-06-01
 * description    : 회원 관리 비즈니스 로직 처리 (개발 가이드 준수)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       개발 가이드에 맞게 수정
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
        // 1) 입력값 검증
        validateDeviceId(request.deviceId)

        // 2) 데이터 조회 (기존 사용자 확인)
        val member = memberRepository.findByDeviceId(request.deviceId)
            ?: createAndSaveMember(request)

        // 3) 날씨 선호도 조회
        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)

        // 4) 응답 생성
        return weatherPreferenceMapper.toMemberWithPreferenceResponse(member, weatherPreference)
    }

    /**
     * 회원 정보 조회 (deviceId 기반)
     */
    fun getMemberByDeviceId(deviceId: String): MemberResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)

        // 3) 응답 생성
        return memberMapper.toResponse(member)
    }

    /**
     * 회원 정보 + 날씨 선호도 통합 조회
     */
    fun getMemberWithPreference(deviceId: String): MemberWithPreferenceResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)
        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)

        // 3) 응답 생성
        return weatherPreferenceMapper.toMemberWithPreferenceResponse(member, weatherPreference)
    }

    /**
     * 닉네임 수정
     */
    @Transactional
    fun updateNickname(deviceId: String, nickname: String): MemberResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)

        // 3) 도메인 로직 실행 (엔티티에 위임)
        val updatedMember = member.updateNickname(nickname)

        // 4) 저장 및 응답
        val savedMember = memberRepository.save(updatedMember)
        return memberMapper.toResponse(savedMember)
    }

    /**
     * 기본 위치 정보 수정
     */
    @Transactional
    fun updateLocation(deviceId: String, request: LocationUpdateRequest): MemberResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)

        // 3) 도메인 로직 실행 (엔티티에 위임)
        val updatedMember = member.updateDefaultLocation(
            latitude = request.latitude,
            longitude = request.longitude,
            cityName = request.cityName
        )

        // 4) 저장 및 응답
        val savedMember = memberRepository.save(updatedMember)
        return memberMapper.toResponse(savedMember)
    }

    /**
     * 5단계 날씨 선호도 설정 완료
     */
    @Transactional
    fun setupWeatherPreference(deviceId: String, request: WeatherPreferenceSetupRequest): WeatherPreferenceResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)
        validateWeatherPreferenceSetupRequest(request)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)
        validateWeatherPreferenceNotExists(member.id)

        // 3) 비즈니스 로직 실행
        val weatherPreference = weatherPreferenceMapper.toEntity(member.id, request)

        // 4) 저장 및 응답
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
        // 1) 입력값 검증
        validateDeviceId(deviceId)
        validateWeatherPreferenceUpdateRequest(request)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)
        val existingPreference = findWeatherPreferenceByMemberId(member.id)

        // 3) 도메인 로직 실행 (엔티티에 위임)
        val updatedPreference = existingPreference.update(
            comfortTemperature = request.comfortTemperature,
            temperatureWeight = request.temperatureWeight,
            humidityWeight = request.humidityWeight,
            windWeight = request.windWeight,
            uvWeight = request.uvWeight,
            airQualityWeight = request.airQualityWeight
        )

        // 4) 저장 및 응답
        val savedPreference = weatherPreferenceRepository.save(updatedPreference)
        return weatherPreferenceMapper.toResponse(savedPreference)
    }

    /**
     * 날씨 선호도 조회
     */
    fun getWeatherPreference(deviceId: String): WeatherPreferenceResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)
        val weatherPreference = findWeatherPreferenceByMemberId(member.id)

        // 3) 응답 생성
        return weatherPreferenceMapper.toResponse(weatherPreference)
    }

    /**
     * 회원 탈퇴 (계정 비활성화)
     */
    @Transactional
    fun deactivateMember(deviceId: String): MemberResponse {
        // 1) 입력값 검증
        validateDeviceId(deviceId)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)

        // 3) 도메인 로직 실행 (엔티티에 위임)
        val deactivatedMember = member.deactivate()

        // 4) 저장 및 응답
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
        // 1) 입력값 검증
        validateDeviceId(deviceId)

        // 2) 데이터 조회
        val member = findMemberByDeviceId(deviceId)
        val weatherPreference = findWeatherPreferenceByMemberId(member.id)

        // 3) 도메인 로직 실행 (엔티티에 위임)
        return weatherPreference.calculateFeelsLikeTemperature(actualTemp, windSpeed, humidity)
    }

    /**
     * 5단계 설정 완료 여부 확인
     */
    fun isWeatherPreferenceSetupCompleted(deviceId: String): Boolean {
        if (deviceId.isBlank()) return false

        val member = memberRepository.findByDeviceId(deviceId) ?: return false

        return weatherPreferenceRepository.findByMemberId(member.id)
            ?.isSetupCompleted ?: false
    }

    // ===== 입력값 검증 메서드들 =====

    private fun validateDeviceId(deviceId: String) {
        if (deviceId.isBlank()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateWeatherPreferenceSetupRequest(request: WeatherPreferenceSetupRequest) {
        if (!request.isValidPriorities()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateWeatherPreferenceUpdateRequest(request: WeatherPreferenceUpdateRequest) {
        if (!request.hasUpdates()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }
    }

    private fun validateWeatherPreferenceNotExists(memberId: Long) {
        if (weatherPreferenceRepository.existsByMemberId(memberId)) {
            throw ApiException(WEATHER_PREFERENCE_ALREADY_EXISTS)
        }
    }

    // ===== 공통 조회 메서드들 =====

    private fun findMemberByDeviceId(deviceId: String): Member {
        return memberRepository.findByDeviceId(deviceId)
            ?: throw ApiException(MEMBER_NOT_FOUND)
    }

    private fun findWeatherPreferenceByMemberId(memberId: Long): com.wout.member.entity.WeatherPreference {
        return weatherPreferenceRepository.findByMemberId(memberId)
            ?: throw ApiException(SENSITIVITY_PROFILE_NOT_FOUND)
    }

    // ===== 비즈니스 로직 메서드들 =====

    private fun createAndSaveMember(request: MemberCreateRequest): Member {
        // Member.from() 팩토리 메서드 사용
        val newMember = Member.from(request)
        return memberRepository.save(newMember)
    }
}