package com.wout.member.repository

import com.wout.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * packageName    : com.wout.member.repository
 * fileName       : MemberRepository
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 회원 정보 데이터 접근 계층
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
interface MemberRepository : JpaRepository<Member, Long> {

    /**
     * deviceId로 회원 조회 (앱 실행 시 기존 사용자 확인용)
     */
    fun findByDeviceId(deviceId: String): Optional<Member>

    /**
     * deviceId 존재 여부 확인 (중복 가입 방지용)
     */
    fun existsByDeviceId(deviceId: String): Boolean
}