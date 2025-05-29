package com.wout.member.entity

import com.wout.common.entity.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * packageName    : com.wout.member.entity
 * fileName       : WeatherPreference
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 사용자 날씨 선호도 및 민감도 설정 엔티티
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-05-29        MinKyu Park       체감온도 계산 공식 수정 및 개인화
 */
@Entity
class WeatherPreference private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("날씨 선호도 설정 고유 ID")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false, unique = true)
    @Comment("회원 ID (FK)")
    val memberId: Long,

    // === 1단계: 우선순위 (괴로운 날씨 2개 선택) ===
    @Column(name = "priority_first", length = 20)
    @Comment("1순위 괴로운 날씨 (heat/cold/humidity/wind/uv/pollution)")
    val priorityFirst: String? = null,

    @Column(name = "priority_second", length = 20)
    @Comment("2순위 괴로운 날씨 (heat/cold/humidity/wind/uv/pollution)")
    val prioritySecond: String? = null,

    // === 2단계: 체감온도 기준점 ===
    @Column(name = "comfort_temperature", nullable = false)
    @Comment("긴팔을 입기 시작하는 온도 (10-30도)")
    val comfortTemperature: Int = 20,

    // === 3단계: 피부 반응 (자외선+기온 민감도 추정) ===
    @Column(name = "skin_reaction", length = 10)
    @Comment("여름 외출 후 피부 반응 (high/medium/low)")
    val skinReaction: String? = null,

    // === 4단계: 습도 민감도 ===
    @Column(name = "humidity_reaction", length = 10)
    @Comment("습한 날씨 불편함 정도 (high/medium/low)")
    val humidityReaction: String? = null,

    // === 5단계: 세부 조정 (각 요소별 영향도) ===
    @Column(name = "temperature_weight", nullable = false)
    @Comment("기온 영향도 가중치 (1-100)")
    val temperatureWeight: Int = 50,

    @Column(name = "humidity_weight", nullable = false)
    @Comment("습도 영향도 가중치 (1-100)")
    val humidityWeight: Int = 50,

    @Column(name = "wind_weight", nullable = false)
    @Comment("바람 영향도 가중치 (1-100)")
    val windWeight: Int = 50,

    @Column(name = "uv_weight", nullable = false)
    @Comment("자외선 영향도 가중치 (1-100)")
    val uvWeight: Int = 50,

    @Column(name = "air_quality_weight", nullable = false)
    @Comment("대기질 영향도 가중치 (1-100)")
    val airQualityWeight: Int = 50,

    // === 계산된 개인 보정값들 ===
    @Column(name = "personal_temp_correction", nullable = false)
    @Comment("개인 온도 보정값 (comfort_temperature 기반 계산)")
    val personalTempCorrection: Double = 0.0,

    @Column(name = "is_setup_completed", nullable = false)
    @Comment("5단계 설정 완료 여부")
    val isSetupCompleted: Boolean = false
) : BaseTimeEntity() {

    // JPA용 기본 생성자
    protected constructor() : this(
        memberId = 0L
    )

    companion object {
        /**
         * 기본 설정으로 생성 (5단계 질문 전)
         */
        fun createDefault(memberId: Long): WeatherPreference {
            require(memberId > 0) { "Member ID는 양수여야 합니다." }

            return WeatherPreference(
                memberId = memberId,
                comfortTemperature = 20,
                temperatureWeight = 50,
                humidityWeight = 50,
                windWeight = 50,
                uvWeight = 50,
                airQualityWeight = 50,
                personalTempCorrection = 0.0,
                isSetupCompleted = false
            )
        }

        /**
         * 5단계 질문 완료 후 생성
         */
        fun createFromSetup(
            memberId: Long,
            priorityFirst: String?,
            prioritySecond: String?,
            comfortTemperature: Int,
            skinReaction: String?,
            humidityReaction: String?,
            temperatureWeight: Int = 50,
            humidityWeight: Int = 50,
            windWeight: Int = 50,
            uvWeight: Int = 50,
            airQualityWeight: Int = 50
        ): WeatherPreference {
            require(memberId > 0) { "Member ID는 양수여야 합니다." }
            require(comfortTemperature in 10..30) { "쾌적 온도는 10-30도 사이여야 합니다." }
            require(temperatureWeight in 1..100) { "온도 가중치는 1-100 사이여야 합니다." }
            require(humidityWeight in 1..100) { "습도 가중치는 1-100 사이여야 합니다." }
            require(windWeight in 1..100) { "바람 가중치는 1-100 사이여야 합니다." }
            require(uvWeight in 1..100) { "자외선 가중치는 1-100 사이여야 합니다." }
            require(airQualityWeight in 1..100) { "대기질 가중치는 1-100 사이여야 합니다." }

            // 개인 온도 보정값 계산: (사용자 긴팔온도 - 20) × 0.5
            val personalCorrection = (comfortTemperature - 20) * 0.5

            return WeatherPreference(
                memberId = memberId,
                priorityFirst = priorityFirst,
                prioritySecond = prioritySecond,
                comfortTemperature = comfortTemperature,
                skinReaction = skinReaction,
                humidityReaction = humidityReaction,
                temperatureWeight = temperatureWeight,
                humidityWeight = humidityWeight,
                windWeight = windWeight,
                uvWeight = uvWeight,
                airQualityWeight = airQualityWeight,
                personalTempCorrection = personalCorrection,
                isSetupCompleted = true
            )
        }
    }

    /**
     * 우선순위 항목들을 리스트로 반환
     */
    fun getPriorityList(): List<String> {
        return listOfNotNull(priorityFirst, prioritySecond)
    }

    /**
     * 특정 요소가 우선순위에 포함되는지 확인
     */
    fun hasPriority(weatherElement: String): Boolean {
        return priorityFirst == weatherElement || prioritySecond == weatherElement
    }

    /**
     * 우선순위에 따른 패널티 가중치 반환
     */
    fun getPriorityPenaltyWeight(weatherElement: String): Double {
        return when {
            priorityFirst == weatherElement -> 0.3  // 1순위: 70% 감점
            prioritySecond == weatherElement -> 0.5 // 2순위: 50% 감점
            else -> 1.0 // 패널티 없음
        }
    }

    /**
     * 개인별 체감온도 계산 (수정된 버전)
     */
    fun calculateFeelsLikeTemperature(
        actualTemp: Double,
        windSpeed: Double,
        humidity: Double
    ): Double {
        var feelsLikeTemp = actualTemp

        // 1. Wind Chill 계산 (10°C 이하에서만 적용)
        if (actualTemp <= 10.0 && windSpeed > 1.6) { // 풍속 1.6 km/h 이상에서만
            feelsLikeTemp = 13.12 + 0.6215 * actualTemp -
                    11.37 * Math.pow(windSpeed, 0.16) +
                    0.3965 * actualTemp * Math.pow(windSpeed, 0.16)  // ✅ 올바른 공식
        }
        // 2. Heat Index 계산 (27°C 이상에서만 적용)
        else if (actualTemp >= 27.0 && humidity >= 40.0) {
            // 간단한 Heat Index 공식 사용
            val tempF = actualTemp * 9.0 / 5.0 + 32.0  // 화씨 변환
            val heatIndexF = -42.379 + 2.04901523 * tempF + 10.14333127 * humidity -
                    0.22475541 * tempF * humidity - 0.00683783 * tempF * tempF -
                    0.05481717 * humidity * humidity + 0.00122874 * tempF * tempF * humidity +
                    0.00085282 * tempF * humidity * humidity - 0.00000199 * tempF * tempF * humidity * humidity
            feelsLikeTemp = (heatIndexF - 32.0) * 5.0 / 9.0  // 섭씨 변환
        }

        // 3. 습도 보정 (개인 민감도 반영)
        val humidityCorrection = getHumidityCorrection(humidity)

        // 4. 개인별 온도 보정 적용
        return feelsLikeTemp + this.personalTempCorrection + humidityCorrection
    }

    /**
     * 개인 습도 민감도에 따른 보정값 계산
     */
    private fun getHumidityCorrection(humidity: Double): Double {
        val baseCorrection = when {
            humidity >= 85 -> 3.0   // 매우 습함
            humidity >= 75 -> 2.0   // 습함
            humidity >= 65 -> 1.0   // 약간 습함
            humidity >= 40 -> 0.0   // 적당함
            humidity >= 30 -> -0.5  // 건조함
            else -> -1.0            // 매우 건조함
        }

        // 개인 습도 민감도 반영
        val sensitivityMultiplier = when (this.humidityReaction) {
            "high" -> 1.5    // 습도에 매우 민감 (50% 증폭)
            "medium" -> 1.0  // 보통 민감도
            "low" -> 0.5     // 습도에 둔감 (50% 감소)
            else -> 1.0      // 기본값
        }

        return baseCorrection * sensitivityMultiplier
    }
}