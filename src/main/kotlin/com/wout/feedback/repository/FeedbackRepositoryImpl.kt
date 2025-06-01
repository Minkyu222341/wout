package com.wout.feedback.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wout.feedback.entity.Feedback
import com.wout.feedback.entity.QFeedback.feedback
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.repository
 * fileName       : FeedbackRepositoryImpl
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : QueryDSL 구현체 MVP 버전 (핵심 기능만)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성 (MVP 필수 2개 메서드만 구현)
 */
@Repository
class FeedbackRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : FeedbackRepositoryCustom {

    override fun findRecentFeedbacks(
        memberId: Long,
        days: Int
    ): List<Feedback> {
        val cutoffDate = LocalDateTime.now().minusDays(days.toLong())

        return queryFactory
            .selectFrom(feedback)
            .where(
                memberIdEq(memberId),
                feedback.createdAt.after(cutoffDate)
            )
            .orderBy(feedback.createdAt.desc())
            .fetch()
    }

    override fun countTodayFeedbacks(
        memberId: Long,
        todayStart: LocalDateTime,
        todayEnd: LocalDateTime
    ): Long {
        return queryFactory
            .select(feedback.count())
            .from(feedback)
            .where(
                memberIdEq(memberId),
                feedback.createdAt.between(todayStart, todayEnd)
            )
            .fetchOne() ?: 0L
    }

    // ===== 헬퍼 메서드 =====

    private fun memberIdEq(memberId: Long?): BooleanExpression? {
        return if (memberId != null && memberId > 0) {
            feedback.memberId.eq(memberId)
        } else null
    }
}