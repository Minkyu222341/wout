package com.wout.feedback.repository

import com.wout.feedback.entity.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.repository
 * fileName       : FeedbackRepository
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 Repository MVP 버전 (핵심 기능만)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성 (MVP 핵심 3개 메서드만)
 */
interface FeedbackRepository : JpaRepository<Feedback, Long>, FeedbackRepositoryCustom {

    /**
     * 피드백 히스토리 조회 (페이징)
     * MVP 핵심: 사용자가 자신의 피드백 기록을 볼 수 있어야 함
     */
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long, pageable: Pageable): Page<Feedback>

    /**
     * 일일 피드백 제한 체크용
     * MVP 핵심: 하루 10개 제한 기능
     */
    fun countByMemberIdAndCreatedAtAfter(memberId: Long, createdAt: LocalDateTime): Int

    /**
     * 중복 피드백 방지
     * MVP 핵심: 같은 날씨 데이터에 중복 피드백 불가
     */
    fun existsByMemberIdAndWeatherDataId(memberId: Long, weatherDataId: Long): Boolean
}