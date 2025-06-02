package com.wout.outfit.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wout.outfit.entity.OutfitRecommendation
import com.wout.outfit.entity.QOutfitRecommendation.outfitRecommendation
import com.wout.outfit.entity.enums.TopCategory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.repository
 * fileName       : OutfitRecommendationRepositoryImpl
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 QueryDSL 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Repository
class OutfitRecommendationRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : OutfitRecommendationRepositoryCustom {

    override fun findRecentByMemberId(memberId: Long, limit: Int): List<OutfitRecommendation> {
        return queryFactory
            .selectFrom(outfitRecommendation)
            .where(outfitRecommendation.memberId.eq(memberId))
            .orderBy(outfitRecommendation.createdAt.desc())
            .limit(limit.toLong())
            .fetch()
    }

    override fun findLatestByMemberIdAndWeatherDataId(
        memberId: Long,
        weatherDataId: Long
    ): OutfitRecommendation? {
        return queryFactory
            .selectFrom(outfitRecommendation)
            .where(
                outfitRecommendation.memberId.eq(memberId),
                outfitRecommendation.weatherDataId.eq(weatherDataId)
            )
            .orderBy(outfitRecommendation.createdAt.desc())
            .limit(1)
            .fetchOne()
    }

    override fun findRecommendationsWithComplexConditions(
        memberId: Long?,
        temperatureRange: ClosedRange<Double>?,
        topCategories: List<TopCategory>?,
        minConfidenceScore: Int?,
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        limit: Int
    ): List<OutfitRecommendation> {
        return queryFactory
            .selectFrom(outfitRecommendation)
            .where(
                memberIdEq(memberId),
                temperatureInRange(temperatureRange),
                topCategoryIn(topCategories),
                confidenceScoreGoe(minConfidenceScore),
                createdAtAfter(fromDate),
                createdAtBefore(toDate)
            )
            .orderBy(outfitRecommendation.createdAt.desc())
            .limit(limit.toLong())
            .fetch()
    }

    override fun findRecommendationStatsByWeatherScore(
        memberId: Long,
        fromDate: LocalDateTime?
    ): List<OutfitRecommendationStats> {
        return queryFactory
            .select(
                Projections.constructor(
                    OutfitRecommendationStats::class.java,
                    outfitRecommendation.weatherScore.divide(10).multiply(10).stringValue().concat("점대"),
                    outfitRecommendation.count(),
                    outfitRecommendation._confidenceScore.avg()
                )
            )
            .from(outfitRecommendation)
            .where(
                outfitRecommendation.memberId.eq(memberId),
                createdAtAfter(fromDate)
            )
            .groupBy(outfitRecommendation.weatherScore.divide(10))
            .orderBy(outfitRecommendation.weatherScore.divide(10).desc())
            .fetch()
    }

    override fun findSimilarWeatherRecommendations(
        memberId: Long,
        temperature: Double,
        feelsLikeTemperature: Double,
        temperatureTolerance: Double,
        limit: Int
    ): List<OutfitRecommendation> {
        val tempMin = temperature - temperatureTolerance
        val tempMax = temperature + temperatureTolerance
        val feelsLikeMin = feelsLikeTemperature - temperatureTolerance
        val feelsLikeMax = feelsLikeTemperature + temperatureTolerance

        return queryFactory
            .selectFrom(outfitRecommendation)
            .where(
                outfitRecommendation.memberId.eq(memberId),
                outfitRecommendation.temperature.between(tempMin, tempMax),
                outfitRecommendation.feelsLikeTemperature.between(feelsLikeMin, feelsLikeMax)
            )
            .orderBy(
                outfitRecommendation._confidenceScore.desc(),
                outfitRecommendation.createdAt.desc()
            )
            .limit(limit.toLong())
            .fetch()
    }

    // ===== Private Helper Methods =====

    private fun memberIdEq(memberId: Long?): BooleanExpression? {
        return memberId?.let { outfitRecommendation.memberId.eq(it) }
    }

    private fun temperatureInRange(temperatureRange: ClosedRange<Double>?): BooleanExpression? {
        return temperatureRange?.let {
            outfitRecommendation.temperature.between(it.start, it.endInclusive)
        }
    }

    private fun topCategoryIn(topCategories: List<TopCategory>?): BooleanExpression? {
        return topCategories?.takeIf { it.isNotEmpty() }?.let {
            outfitRecommendation._topCategory.`in`(it)
        }
    }

    private fun confidenceScoreGoe(minConfidenceScore: Int?): BooleanExpression? {
        return minConfidenceScore?.let { outfitRecommendation._confidenceScore.goe(it) }
    }

    private fun createdAtAfter(fromDate: LocalDateTime?): BooleanExpression? {
        return fromDate?.let { outfitRecommendation.createdAt.after(it) }
    }

    private fun createdAtBefore(toDate: LocalDateTime?): BooleanExpression? {
        return toDate?.let { outfitRecommendation.createdAt.before(it) }
    }
}