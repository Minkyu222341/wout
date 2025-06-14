package com.wout.member.entity

import com.wout.common.entity.BaseTimeEntity
import com.wout.member.model.ElementWeights
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import kotlin.math.pow

/**
 * packageName    : com.wout.member.entity
 * fileName       : WeatherPreference
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 사용자 날씨 선호도 및 민감도 설정 엔티티 (더티체킹 패턴 적용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 * 2025-06-08        MinKyu Park       더티체킹 패턴으로 변경 (2단계)
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

    // === 1단계: 우선순위 선택 ===
    @Column(name = "priority_first", length = 20)
    @Comment("1순위 괴로운 날씨 요소")
    private var _priorityFirst: String? = null,

    @Column(name = "priority_second", length = 20)
    @Comment("2순위 괴로운 날씨 요소")
    private var _prioritySecond: String? = null,

    // === 2단계: 체감온도 기준점 ===
    @Column(name = "comfort_temperature", nullable = false)
    @Comment("긴팔을 입기 시작하는 온도 (10–30℃)")
    private var _comfortTemperature: Int = 20,

    // === 3·4단계: 피부·습도 반응 ===
    @Column(name = "skin_reaction", length = 10)
    private var _skinReaction: String? = null,

    @Column(name = "humidity_reaction", length = 10)
    private var _humidityReaction: String? = null,

    // === 5단계: 세부 가중치 (25–75 제한) ===
    @Column(name = "temperature_weight", nullable = false)
    private var _temperatureWeight: Int = 50,

    @Column(name = "humidity_weight", nullable = false)
    private var _humidityWeight: Int = 50,

    @Column(name = "wind_weight", nullable = false)
    private var _windWeight: Int = 50,

    @Column(name = "uv_weight", nullable = false)
    private var _uvWeight: Int = 50,

    @Column(name = "air_quality_weight", nullable = false)
    private var _airQualityWeight: Int = 50,

    // === 계산된 개인 보정값 ===
    @Column(name = "personal_temp_correction", nullable = false)
    private var _personalTempCorrection: Double = 0.0,

    @Column(name = "is_setup_completed", nullable = false)
    private var _isSetupCompleted: Boolean = false
) : BaseTimeEntity() {

    /* ===== Getter Properties (외부 접근용) ===== */
    val priorityFirst: String? get() = _priorityFirst
    val prioritySecond: String? get() = _prioritySecond
    val comfortTemperature: Int get() = _comfortTemperature
    val skinReaction: String? get() = _skinReaction
    val humidityReaction: String? get() = _humidityReaction
    val temperatureWeight: Int get() = _temperatureWeight
    val humidityWeight: Int get() = _humidityWeight
    val windWeight: Int get() = _windWeight
    val uvWeight: Int get() = _uvWeight
    val airQualityWeight: Int get() = _airQualityWeight
    val personalTempCorrection: Double get() = _personalTempCorrection
    val isSetupCompleted: Boolean get() = _isSetupCompleted

    /* ===== Companion & 상수 ===== */
    companion object {
        const val MIN_WEIGHT = 25
        const val MAX_WEIGHT = 75

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
            require(memberId > 0) { "Member ID는 양수여야 합니다" }
            require(comfortTemperature in 10..30) { "쾌적 온도는 10-30℃ 사이여야 합니다" }

            fun limit(w: Int) = w.coerceIn(MIN_WEIGHT, MAX_WEIGHT)
            val personalCorrection = (comfortTemperature - 20) * 0.5

            return WeatherPreference(
                memberId = memberId,
                _priorityFirst = priorityFirst,
                _prioritySecond = prioritySecond,
                _comfortTemperature = comfortTemperature,
                _skinReaction = skinReaction,
                _humidityReaction = humidityReaction,
                _temperatureWeight = limit(temperatureWeight),
                _humidityWeight = limit(humidityWeight),
                _windWeight = limit(windWeight),
                _uvWeight = limit(uvWeight),
                _airQualityWeight = limit(airQualityWeight),
                _personalTempCorrection = personalCorrection,
                _isSetupCompleted = true
            )
        }
    }

    protected constructor() : this(memberId = 0L)

    /* ===== ✅ 더티체킹을 위한 가변 업데이트 메서드 ===== */

    /**
     * ✅ 통합 업데이트 메서드 (더티체킹 방식)
     * null이 아닌 필드만 업데이트
     */
    fun updatePreferences(
        priorityFirst: String? = null,
        prioritySecond: String? = null,
        comfortTemperature: Int? = null,
        skinReaction: String? = null,
        humidityReaction: String? = null,
        temperatureWeight: Int? = null,
        humidityWeight: Int? = null,
        windWeight: Int? = null,
        uvWeight: Int? = null,
        airQualityWeight: Int? = null
    ) {
        // 1단계: 우선순위 업데이트
        priorityFirst?.let {
            validatePriority(it, "첫 번째")
            this._priorityFirst = it
        }
        prioritySecond?.let {
            validatePriority(it, "두 번째")
            this._prioritySecond = it
        }

        // 2단계: 체감온도 업데이트
        comfortTemperature?.let {
            require(it in 10..30) { "쾌적 온도는 10-30℃ 사이여야 합니다" }
            this._comfortTemperature = it
            this._personalTempCorrection = (it - 20) * 0.5
        }

        // 3-4단계: 반응 레벨 업데이트
        skinReaction?.let {
            validateReactionLevel(it, "피부 반응")
            this._skinReaction = it
        }
        humidityReaction?.let {
            validateReactionLevel(it, "습도 반응")
            this._humidityReaction = it
        }

        // 5단계: 가중치 업데이트
        temperatureWeight?.let {
            require(it in MIN_WEIGHT..MAX_WEIGHT) { "온도 가중치는 $MIN_WEIGHT-$MAX_WEIGHT 사이여야 합니다" }
            this._temperatureWeight = it
        }
        humidityWeight?.let {
            require(it in MIN_WEIGHT..MAX_WEIGHT) { "습도 가중치는 $MIN_WEIGHT-$MAX_WEIGHT 사이여야 합니다" }
            this._humidityWeight = it
        }
        windWeight?.let {
            require(it in MIN_WEIGHT..MAX_WEIGHT) { "바람 가중치는 $MIN_WEIGHT-$MAX_WEIGHT 사이여야 합니다" }
            this._windWeight = it
        }
        uvWeight?.let {
            require(it in MIN_WEIGHT..MAX_WEIGHT) { "자외선 가중치는 $MIN_WEIGHT-$MAX_WEIGHT 사이여야 합니다" }
            this._uvWeight = it
        }
        airQualityWeight?.let {
            require(it in MIN_WEIGHT..MAX_WEIGHT) { "대기질 가중치는 $MIN_WEIGHT-$MAX_WEIGHT 사이여야 합니다" }
            this._airQualityWeight = it
        }
    }

    /**
     * 설정 완료 상태 변경
     */
    fun markAsCompleted() {
        this._isSetupCompleted = true
    }

    fun markAsIncomplete() {
        this._isSetupCompleted = false
    }

    /* ===== ❌ 기존 update() 메서드 제거됨 ===== */
    // fun update(...): WeatherPreference { ... } // 제거!

    /* ===== ❌ copy() 메서드 제거됨 ===== */
    // private fun copy(...): WeatherPreference { ... } // 제거!

    /* ===== 기존 도메인 로직들 (변경 없음) ===== */

    fun calculateImprovedWeights(): ElementWeights = ElementWeights.fromRaw(
        temperatureWeight,
        humidityWeight,
        windWeight,
        uvWeight,
        airQualityWeight
    )

    fun getPriorityList(): List<String> = listOfNotNull(priorityFirst, prioritySecond)

    fun isPriorityElement(element: String): Boolean = element in getPriorityList()

    fun getPriorityPenaltyWeight(element: String): Double = when {
        priorityFirst == element -> 0.3
        prioritySecond == element -> 0.5
        else -> 1.0
    }

    fun isHighSensitivity(): Boolean {
        val cnt = listOf(
            skinReaction == "high",
            humidityReaction == "high",
            temperatureWeight >= 70,
            humidityWeight >= 70,
            uvWeight >= 70
        ).count { it }
        return cnt >= 3
    }

    fun isColdSensitive(): Boolean = comfortTemperature >= 22 || isPriorityElement("cold")
    fun isHeatSensitive(): Boolean = comfortTemperature <= 16 || isPriorityElement("heat")
    fun isHumiditySensitive(): Boolean = isPriorityElement("humidity") || humidityReaction == "high" || humidityWeight >= 70
    fun isWindSensitive(): Boolean = isPriorityElement("wind") || windWeight >= 70
    fun isUVSensitive(): Boolean = isPriorityElement("uv") || skinReaction == "high" || uvWeight >= 70
    fun isAirQualitySensitive(): Boolean = isPriorityElement("pollution") || airQualityWeight >= 70

    fun getPersonalityTraits(): List<String> = buildList {
        if (isColdSensitive()) add("추위를 많이 탐")
        if (isHeatSensitive()) add("더위를 많이 탐")
        if (isHumiditySensitive()) add("습도에 민감")
        if (isUVSensitive()) add("자외선 민감")
        if (isWindSensitive()) add("바람 싫어함")
        if (isAirQualitySensitive()) add("미세먼지 민감")
    }

    fun getTemperatureAdjustment(actual: Double): Double {
        var adj = 0.0
        if (isColdSensitive()) adj -= if (comfortTemperature >= 26) 3.0 else if (comfortTemperature >= 24) 2.0 else 1.0
        if (isHeatSensitive()) adj += if (comfortTemperature <= 14) 3.0 else if (comfortTemperature <= 16) 2.0 else 1.0
        return adj
    }

    fun calculateFeelsLikeTemperature(actual: Double, wind: Double, humidity: Double): Double {
        var feels = actual
        if (actual <= 10 && wind >= 1.34) {
            feels = 13.12 + 0.6215 * actual - 11.37 * wind.pow(0.16) + 0.3965 * actual * wind.pow(0.16)
        } else if (actual >= 27 && humidity >= 40) {
            feels = calculateHeatIndex(actual, humidity)
        }
        return feels + personalTempCorrection + getHumidityCorrection(humidity)
    }

    fun getWeightFor(element: String): Int = when (element) {
        "temperature" -> temperatureWeight
        "humidity" -> humidityWeight
        "wind" -> windWeight
        "uv" -> uvWeight
        "airQuality" -> airQualityWeight
        else -> 50
    }

    /* ===== Private 헬퍼 메서드들 ===== */

    private fun calculateHeatIndex(tc: Double, h: Double): Double {
        val tf = tc * 9 / 5 + 32
        val hi = -42.379 + 2.04901523 * tf + 10.14333127 * h - 0.22475541 * tf * h -
                0.00683783 * tf * tf - 0.05481717 * h * h + 0.00122874 * tf * tf * h +
                0.00085282 * tf * h * h - 0.00000199 * tf * tf * h * h
        return (hi - 32) * 5 / 9
    }

    private fun getHumidityCorrection(h: Double): Double {
        val base = when {
            h >= 85 -> 3.0
            h >= 75 -> 2.0
            h >= 65 -> 1.0
            h >= 40 -> 0.0
            h >= 30 -> -0.5
            else -> -1.0
        }
        val mult = when (humidityReaction) {
            "high" -> 1.5; "low" -> 0.5; else -> 1.0
        }
        return base * mult
    }

    private fun validatePriority(p: String?, name: String) {
        if (p != null && p !in setOf("heat","cold","humidity","wind","uv","pollution"))
            throw IllegalArgumentException("유효하지 않은 $name 우선순위: $p")
    }

    private fun validateReactionLevel(l: String?, name: String) {
        if (l != null && l !in setOf("high","medium","low"))
            throw IllegalArgumentException("유효하지 않은 $name 레벨: $l")
    }
}