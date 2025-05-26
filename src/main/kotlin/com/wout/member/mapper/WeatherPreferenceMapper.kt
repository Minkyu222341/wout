package com.wout.member.mapper

import com.wout.member.dto.request.WeatherPreferenceSetupRequest
import com.wout.member.dto.request.WeatherPreferenceUpdateRequest
import com.wout.member.dto.response.MemberWithPreferenceResponse
import com.wout.member.dto.response.WeatherPreferenceResponse
import com.wout.member.entity.Member
import com.wout.member.entity.WeatherPreference
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.member.mapper
 * fileName       : WeatherPreferenceMapper
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : WeatherPreference 엔티티와 DTO 간 변환 매퍼
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
@Component
class WeatherPreferenceMapper(
    private val memberMapper: MemberMapper
) {

    /**
     * SetupRequest -> Entity 변환 (새 설정)
     */
    fun toEntity(memberId: Long, request: WeatherPreferenceSetupRequest): WeatherPreference {
        return WeatherPreference.createFromSetup(
            memberId = memberId,
            priorityFirst = request.priorityFirst,
            prioritySecond = request.prioritySecond,
            comfortTemperature = request.comfortTemperature,
            skinReaction = request.skinReaction,
            humidityReaction = request.humidityReaction,
            temperatureWeight = request.temperatureWeight,
            humidityWeight = request.humidityWeight,
            windWeight = request.windWeight,
            uvWeight = request.uvWeight,
            airQualityWeight = request.airQualityWeight
        )
    }

    /**
     * Entity -> Response 변환
     */
    fun toResponse(preference: WeatherPreference): WeatherPreferenceResponse {
        return WeatherPreferenceResponse(
            id = preference.id,
            memberId = preference.memberId,
            priorityFirst = preference.priorityFirst,
            prioritySecond = preference.prioritySecond,
            priorities = preference.getPriorityList(),
            comfortTemperature = preference.comfortTemperature,
            skinReaction = preference.skinReaction,
            humidityReaction = preference.humidityReaction,
            temperatureWeight = preference.temperatureWeight,
            humidityWeight = preference.humidityWeight,
            windWeight = preference.windWeight,
            uvWeight = preference.uvWeight,
            airQualityWeight = preference.airQualityWeight,
            personalTempCorrection = preference.personalTempCorrection,
            isSetupCompleted = preference.isSetupCompleted,
            createdAt = preference.createdAt,
            updatedAt = preference.updatedAt
        )
    }

    /**
     * Member + WeatherPreference -> MemberWithPreferenceResponse 변환
     */
    fun toMemberWithPreferenceResponse(
        member: Member,
        preference: WeatherPreference?
    ): MemberWithPreferenceResponse {
        return MemberWithPreferenceResponse(
            member = memberMapper.toResponse(member),
            weatherPreference = preference?.let { toResponse(it) }
        )
    }

    /**
     * UpdateRequest로 기존 엔티티 업데이트
     */
    fun updateEntity(
        existing: WeatherPreference,
        request: WeatherPreferenceUpdateRequest
    ): WeatherPreference {
        // null이 아닌 필드만 업데이트
        val newComfortTemp = request.comfortTemperature ?: existing.comfortTemperature
        val newTempWeight = request.temperatureWeight ?: existing.temperatureWeight
        val newHumidityWeight = request.humidityWeight ?: existing.humidityWeight
        val newWindWeight = request.windWeight ?: existing.windWeight
        val newUvWeight = request.uvWeight ?: existing.uvWeight
        val newAirQualityWeight = request.airQualityWeight ?: existing.airQualityWeight

        // 새로운 개인 보정값 계산
        val newPersonalCorrection = (newComfortTemp - 20) * 0.5

        // 불변 엔티티를 새로 생성하여 반환
        return WeatherPreference.createFromSetup(
            memberId = existing.memberId,
            priorityFirst = existing.priorityFirst,
            prioritySecond = existing.prioritySecond,
            comfortTemperature = newComfortTemp,
            skinReaction = existing.skinReaction,
            humidityReaction = existing.humidityReaction,
            temperatureWeight = newTempWeight,
            humidityWeight = newHumidityWeight,
            windWeight = newWindWeight,
            uvWeight = newUvWeight,
            airQualityWeight = newAirQualityWeight
        )
    }
}