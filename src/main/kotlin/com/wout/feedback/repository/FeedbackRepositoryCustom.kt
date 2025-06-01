package com.wout.feedback.repository

import com.wout.feedback.entity.Feedback
import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.repository
 * fileName       : FeedbackRepositoryCustom
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : QueryDSL 인터페이스 MVP 버전 (필수 기능만)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성 (MVP 필수 2개만)
 */
interface FeedbackRepositoryCustom {

    /**
     * 통계용 최근 피드백 조회
     * MVP 핵심: 최근 30일 피드백으로 간단한 통계 표시
     */
    fun findRecentFeedbacks(memberId: Long, days: Int = 30): List<Feedback>

    /**
     * 오늘 피드백 개수 (정확한 날짜 범위)
     * MVP 핵심: 일일 제한 기능의 정확한 구현
     */
    fun countTodayFeedbacks(memberId: Long, todayStart: LocalDateTime, todayEnd: LocalDateTime): Long
}