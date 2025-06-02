package com.wout.outfit.repository

import com.wout.outfit.entity.OutfitRecommendation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.repository
 * fileName       : OutfitRecommendationRepository
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 기본 JPA Repository
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Repository
interface OutfitRecommendationRepository : JpaRepository<OutfitRecommendation, Long>, OutfitRecommendationRepositoryCustom {

    /**
     * 회원별 최근 추천 조회 (단순 JPQL)
     */
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<OutfitRecommendation>

    /**
     * 회원 + 날씨 데이터 조합으로 최근 추천 조회
     */
    fun findByMemberIdAndWeatherDataIdOrderByCreatedAtDesc(
        memberId: Long,
        weatherDataId: Long
    ): List<OutfitRecommendation>

    /**
     * 회원별 추천 존재 여부 확인
     */
    fun existsByMemberId(memberId: Long): Boolean

    /**
     * 특정 시간 이후 생성된 추천 조회 (최근 추천 중복 방지용)
     */
    fun findByMemberIdAndWeatherDataIdAndCreatedAtAfter(
        memberId: Long,
        weatherDataId: Long,
        createdAt: LocalDateTime
    ): OutfitRecommendation?

    /**
     * 회원별 추천 개수 조회
     */
    fun countByMemberId(memberId: Long): Long
}