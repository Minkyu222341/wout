package com.wout.weather.entity

import com.wout.common.entity.BaseTimeEntity
import com.wout.weather.entity.builder.WeatherDataBuilder
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
 * 25. 5. 24.        MinKyu Park       OpenWeatherMap 전체 필드 추가
 * 25. 5. 24.        MinKyu Park       Builder 패턴을 별도 파일로 분리
 */
@Entity
class WeatherData(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 위치 정보
    @Column(nullable = false)
    @Comment("위도")
    val latitude: Double,

    @Column(nullable = false)
    @Comment("경도")
    val longitude: Double,

    @Column(nullable = false)
    @Comment("도시명")
    val cityName: String,

    // 기본 온도 정보
    @Column(nullable = false)
    @Comment("현재 온도 (섭씨)")
    val temperature: Double,

    @Column(nullable = false)
    @Comment("체감 온도 (섭씨)")
    val feelsLike: Double,

    @Column(nullable = false)
    @Comment("최저 온도 (섭씨)")
    val tempMin: Double,

    @Column(nullable = false)
    @Comment("최고 온도 (섭씨)")
    val tempMax: Double,

    // 대기 정보
    @Column(nullable = false)
    @Comment("습도 (%)")
    val humidity: Int,

    @Column(nullable = false)
    @Comment("기압 (hPa)")
    val pressure: Int,

    @Column
    @Comment("해수면 기압 (hPa)")
    val seaLevelPressure: Int? = null,

    @Column
    @Comment("지면 기압 (hPa)")
    val groundLevelPressure: Int? = null,

    // 바람 정보
    @Column(nullable = false)
    @Comment("풍속 (m/s)")
    val windSpeed: Double,

    @Column
    @Comment("풍향 (도)")
    val windDirection: Int? = null,

    @Column
    @Comment("돌풍 (m/s)")
    val windGust: Double? = null,

    // 날씨 상태
    @Column(nullable = false)
    @Comment("날씨 상태 코드 (Clear, Rain 등)")
    val weatherMain: String,

    @Column(nullable = false)
    @Comment("날씨 상세 설명")
    val weatherDescription: String,

    @Column(nullable = false)
    @Comment("날씨 아이콘 코드")
    val weatherIcon: String,

    // 가시거리
    @Column
    @Comment("가시거리 (미터)")
    val visibility: Int? = null,

    // 구름 정보
    @Column(nullable = false)
    @Comment("구름량 (%)")
    val cloudiness: Int,

    // 강수량 정보
    @Column
    @Comment("1시간 강수량 (mm)")
    val rain1h: Double? = null,

    @Column
    @Comment("3시간 강수량 (mm)")
    val rain3h: Double? = null,

    // 적설량 정보
    @Column
    @Comment("1시간 적설량 (mm)")
    val snow1h: Double? = null,

    @Column
    @Comment("3시간 적설량 (mm)")
    val snow3h: Double? = null,

    // UV 지수
    @Column
    @Comment("자외선 지수")
    val uvIndex: Double? = null,

    // 대기질 정보 (Air Pollution API)
    @Column
    @Comment("미세먼지 PM2.5 (μg/m³)")
    val pm25: Double? = null,

    @Column
    @Comment("미세먼지 PM10 (μg/m³)")
    val pm10: Double? = null,

    @Column
    @Comment("일산화탄소 CO (μg/m³)")
    val co: Double? = null,

    @Column
    @Comment("이산화질소 NO2 (μg/m³)")
    val no2: Double? = null,

    @Column
    @Comment("오존 O3 (μg/m³)")
    val ozone: Double? = null,

    @Column
    @Comment("아황산가스 SO2 (μg/m³)")
    val so2: Double? = null,

    // 일출/일몰 정보
    @Column
    @Comment("일출 시간 (Unix timestamp)")
    val sunrise: Long? = null,

    @Column
    @Comment("일몰 시간 (Unix timestamp)")
    val sunset: Long? = null,

    // API 데이터 시간
    @Column(nullable = false)
    @Comment("날씨 데이터 측정 시간 (Unix timestamp)")
    val dataTimestamp: Long

) : BaseTimeEntity() {
    companion object {
        fun builder(): WeatherDataBuilder {
            return WeatherDataBuilder()
        }
    }
}