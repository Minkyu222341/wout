package com.wout.outfit.repository

import com.wout.outfit.entity.OutfitRecommendation
import com.wout.outfit.entity.enums.TopCategory
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.repository
 * fileName       : OutfitRecommendationRepositoryCustom
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 QueryDSL 확장 인터페이스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
interface OutfitRecommendationRepositoryCustom {

    /**
     * 회원별 최근 추천 조회 (페이징 적용)
     */
    fun findRecentByMemberId(memberId: Long, limit: Int): List<OutfitRecommendation>

    /**
     * 회원 + 날씨 데이터의 가장 최근 추천 조회
     */
    fun findLatestByMemberIdAndWeatherDataId(memberId: Long, weatherDataId: Long): OutfitRecommendation?

    /**
     * 복잡한 조건으로 추천 조회
     */
    fun findRecommendationsWithComplexConditions(
        memberId: Long? = null,
        temperatureRange: ClosedRange<Double>? = null,
        topCategories: List<TopCategory>? = null,
        minConfidenceScore: Int? = null,
        fromDate: LocalDateTime? = null,
        toDate: LocalDateTime? = null,
        limit: Int = 20
    ): List<OutfitRecommendation>

    /**
     * 날씨 점수 범위별 추천 통계
     */
    fun findRecommendationStatsByWeatherScore(
        memberId: Long,
        fromDate: LocalDateTime? = null
    ): List<OutfitRecommendationStats>

    /**
     * 비슷한 날씨 조건의 과거 추천 조회 (학습용)
     */
    fun findSimilarWeatherRecommendations(
        memberId: Long,
        temperature: Double,
        feelsLikeTemperature: Double,
        temperatureTolerance: Double = 3.0,
        limit: Int = 5
    ): List<OutfitRecommendation>
}

/**
 * 추천 통계 데이터 클래스
 */
data class OutfitRecommendationStats(
    val weatherScoreRange: String,
    val recommendationCount: Long,
    val averageConfidenceScore: Double
)