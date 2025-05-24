package com.wout.weather.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wout.weather.entity.QWeatherData.weatherData
import com.wout.weather.entity.WeatherData
import java.time.LocalDateTime

/**
 * packageName    : com.wout.weather.repository
 * fileName       : WeatherDataRepositoryImpl
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : WeatherDataRepository QueryDSL 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 */
class WeatherDataRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : WeatherDataRepositoryCustom {

    /**
     * 특정 도시의 최신 날씨 데이터 조회 (지정된 시간 이후)
     */
    override fun findLatestByCityName(cityName: String, since: LocalDateTime): WeatherData? {
        return queryFactory
            .selectFrom(weatherData)
            .where(
                cityNameEq(cityName),
                weatherData.createdAt.goe(since)
            )
            .orderBy(weatherData.createdAt.desc())
            .limit(1)
            .fetchOne()
    }

    /**
     * 특정 도시의 최신 날씨 데이터 조회 (시간 제한 없음)
     */
    override fun findLatestByCityName(cityName: String): WeatherData? {
        return queryFactory
            .selectFrom(weatherData)
            .where(cityNameEq(cityName))
            .orderBy(weatherData.createdAt.desc())
            .limit(1)
            .fetchOne()
    }

    /**
     * 특정 위치 근처의 최신 날씨 데이터 조회 (반경 기반)
     * Haversine 공식 사용
     */
    override fun findNearestWeatherData(latitude: Double, longitude: Double, since: LocalDateTime): WeatherData? {
        val earthRadius = 6371.0

        val distanceExpression = Expressions.numberTemplate(
            Double::class.java,
            "({0} * acos(cos(radians({1})) * cos(radians({2})) * cos(radians({3}) - radians({4})) + sin(radians({1})) * sin(radians({2}))))",
            earthRadius,
            latitude,
            weatherData.latitude,
            weatherData.longitude,
            longitude
        )

        return queryFactory
            .selectFrom(weatherData)
            .where(weatherData.createdAt.goe(since))
            .orderBy(distanceExpression.asc())
            .limit(1)
            .fetchOne()
    }

    /**
     * 모든 주요 도시의 최신 날씨 데이터 조회
     */
    override fun findLatestWeatherForAllCities(since: LocalDateTime): List<WeatherData> {
        // 각 도시별 최신 createdAt 서브쿼리
        val subQuery = queryFactory
            .select(weatherData.createdAt.max())
            .from(weatherData)
            .where(weatherData.createdAt.goe(since))
            .groupBy(weatherData.cityName)

        return queryFactory
            .selectFrom(weatherData)
            .where(
                weatherData.createdAt.`in`(subQuery),
                weatherData.createdAt.goe(since)
            )
            .orderBy(weatherData.cityName.asc())
            .fetch()
    }

    /**
     * 오래된 날씨 데이터 삭제
     */
    override fun deleteByCreatedAtBefore(cutoffDate: LocalDateTime): Long {
        return queryFactory
            .delete(weatherData)
            .where(weatherData.createdAt.lt(cutoffDate))
            .execute()
    }

    /**
     * 특정 기간 내 날씨 데이터 조회
     */
    override fun findByCityNameAndDateRange(
        cityName: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<WeatherData> {
        return queryFactory
            .selectFrom(weatherData)
            .where(
                cityNameEq(cityName),
                weatherData.createdAt.between(startDate, endDate)
            )
            .orderBy(weatherData.createdAt.desc())
            .fetch()
    }

    /**
     * 특정 도시의 날씨 데이터 개수 조회
     */
    override fun countByCityName(cityName: String): Long {
        return queryFactory
            .select(weatherData.count())
            .from(weatherData)
            .where(cityNameEq(cityName))
            .fetchOne() ?: 0L
    }

    /**
     * 도시명 동적 조건
     */
    private fun cityNameEq(cityName: String?): BooleanExpression? {
        return if (!cityName.isNullOrBlank()) {
            weatherData.cityName.eq(cityName)
        } else null
    }
}