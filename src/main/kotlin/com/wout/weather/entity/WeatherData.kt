package com.wout.weather.entity

import com.wout.common.entity.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * packageName    : com.wout.weather.entity
 * fileName       : WeatherData
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 날씨 데이터 엔티티
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Entity
class WeatherData(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("위도")
    val latitude: Double,

    @Column(nullable = false)
    @Comment("경도")
    val longitude: Double,

    @Column(nullable = false)
    @Comment("온도 (섭씨)")
    val temperature: Double,

    @Column(nullable = false)
    @Comment("체감 온도 (섭씨)")
    val feelsLike: Double,

    @Column(nullable = false)
    @Comment("습도 (%)")
    val humidity: Int,

    @Column(nullable = false)
    @Comment("풍속 (m/s)")
    val windSpeed: Double,

    @Column(nullable = false)
    @Comment("날씨 상태 (Clear, Rain 등)")
    val weatherState: String,

    @Column(nullable = false)
    @Comment("날씨 상세 설명")
    val weatherDescription: String,

    @Column
    @Comment("미세먼지 PM2.5")
    val pm25: Double? = null,

    @Column
    @Comment("미세먼지 PM10")
    val pm10: Double? = null

) : BaseTimeEntity()