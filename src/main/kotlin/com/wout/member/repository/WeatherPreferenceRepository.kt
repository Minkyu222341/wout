package com.wout.member.repository

import com.wout.member.entity.WeatherPreference
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * packageName    : com.wout.member.repository
 * fileName       : WeatherPreferenceRepository
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 날씨 선호도 데이터 접근 계층
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
interface WeatherPreferenceRepository : JpaRepository<WeatherPreference, Long> {

    /**
     * 회원 ID로 날씨 선호도 조회 (5단계 설정 완료 여부 확인용)
     */
    fun findByMemberId(memberId: Long): Optional<WeatherPreference>

    /**
     * 회원 ID의 선호도 설정 존재 여부 확인
     */
    fun existsByMemberId(memberId: Long): Boolean
}