package com.wout.weather.repository

import com.wout.weather.entity.WeatherData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * packageName    : com.wout.weather.repository
 * fileName       : WeatherDataRepository
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 날씨 데이터 저장소
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Repository
interface WeatherDataRepository : JpaRepository<WeatherData, Long> , WeatherDataRepositoryCustom {

    /**
     * 특정 시간 이후의 날씨 데이터 조회
     */
    fun findByLatitudeAndLongitudeAndCreatedAtAfter(
        latitude: Double,
        longitude: Double,
        createdAt: LocalDateTime
    ): Optional<WeatherData>
}