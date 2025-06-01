package com.wout.member.entity

import com.wout.common.entity.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import kotlin.math.pow

/**
 * packageName    : com.wout.member.entity
 * fileName       : WeatherPreference
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 사용자 날씨 선호도 및 민감도 설정 엔티티 (개발 가이드 준수)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       개발 가이드에 맞게 수정 (update 메서드 추가)
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
    private var _priorityFirst: String? = null,

    @Column(name = "priority_second", length = 20)
    @Comment("2순위 괴로운 날씨 (heat/cold/humidity/wind/uv/pollution)")
    private var _prioritySecond: String? = null,

    // === 2단계: 체감온도 기준점 ===
    @Column(name = "comfort_temperature", nullable = false)
    @Comment("긴팔을 입기 시작하는 온도 (10-30도)")
    private var _comfortTemperature: Int = 20,

    // === 3단계: 피부 반응 (자외선+기온 민감도 추정) ===
    @Column(name = "skin_reaction", length = 10)
    @Comment("여름 외출 후 피부 반응 (high/medium/low)")
    private var _skinReaction: String? = null,

    // === 4단계: 습도 민감도 ===
    @Column(name = "humidity_reaction", length = 10)
    @Comment("습한 날씨 불편함 정도 (high/medium/low)")
    private var _humidityReaction: String? = null,

    // === 5단계: 세부 조정 (각 요소별 영향도) ===
    @Column(name = "temperature_weight", nullable = false)
    @Comment("기온 영향도 가중치 (1-100)")
    private var _temperatureWeight: Int = 50,

    @Column(name = "humidity_weight", nullable = false)
    @Comment("습도 영향도 가중치 (1-100)")
    private var _humidityWeight: Int = 50,

    @Column(name = "wind_weight", nullable = false)
    @Comment("바람 영향도 가중치 (1-100)")
    private var _windWeight: Int = 50,

    @Column(name = "uv_weight", nullable = false)
    @Comment("자외선 영향도 가중치 (1-100)")
    private var _uvWeight: Int = 50,

    @Column(name = "air_quality_weight", nullable = false)
    @Comment("대기질 영향도 가중치 (1-100)")
    private var _airQualityWeight: Int = 50,

    // === 계산된 개인 보정값들 ===
    @Column(name = "personal_temp_correction", nullable = false)
    @Comment("개인 온도 보정값 (comfort_temperature 기반 계산)")
    private var _personalTempCorrection: Double = 0.0,

    @Column(name = "is_setup_completed", nullable = false)
    @Comment("5단계 설정 완료 여부")
    private var _isSetupCompleted: Boolean = false
) : BaseTimeEntity() {

    // 읽기 전용 프로퍼티
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

    protected constructor() : this(memberId = 0L)

    companion object {
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
            require(memberId > 0) { "Member ID는 양수여야 합니다" }
            require(comfortTemperature in 10..30) { "쾌적 온도는 10-30도 사이여야 합니다" }

            // 가중치 유효성 검증
            validateWeight(temperatureWeight, "온도")
            validateWeight(humidityWeight, "습도")
            validateWeight(windWeight, "바람")
            validateWeight(uvWeight, "자외선")
            validateWeight(airQualityWeight, "대기질")

            // 개인 온도 보정값 계산: (사용자 긴팔온도 - 20) × 0.5
            val personalCorrection = (comfortTemperature - 20) * 0.5

            return WeatherPreference(
                memberId = memberId,
                _priorityFirst = priorityFirst,
                _prioritySecond = prioritySecond,
                _comfortTemperature = comfortTemperature,
                _skinReaction = skinReaction,
                _humidityReaction = humidityReaction,
                _temperatureWeight = temperatureWeight,
                _humidityWeight = humidityWeight,
                _windWeight = windWeight,
                _uvWeight = uvWeight,
                _airQualityWeight = airQualityWeight,
                _personalTempCorrection = personalCorrection,
                _isSetupCompleted = true
            )
        }

        /**
         * WeatherPreferenceSetupRequest로부터 생성
         */
        fun from(memberId: Long, request: com.wout.member.dto.request.WeatherPreferenceSetupRequest): WeatherPreference {
            return createFromSetup(
                memberId = memberId,
                priorityFirst = request.priorityFirst,
                prioritySecond = request.prioritySecond,
                comfortTemperature = request.comfortTemperature,
                skinReaction = request.skinReaction,
                humidityReaction = request.humidityReaction,
                temperatureWeight = request.temperatureWeight,
                humidityWeight = request.humidityWeight,
                windWeight = request.windWeight,
                uvWeight = request.uvWeight,
                airQualityWeight = request.airQualityWeight
            )
        }

        /**
         * 가중치 유효성 검증
         */
        private fun validateWeight(weight: Int, name: String) {
            require(weight in 1..100) { "${name} 가중치는 1-100 사이여야 합니다" }
        }
    }

    // ===== 도메인 로직 (상태 변경) =====

    /**
     * 부분 업데이트 (5단계 설정 완료 후)
     */
    fun update(
        comfortTemperature: Int? = null,
        temperatureWeight: Int? = null,
        humidityWeight: Int? = null,
        windWeight: Int? = null,
        uvWeight: Int? = null,
        airQualityWeight: Int? = null
    ): WeatherPreference {
        val newComfortTemp = comfortTemperature ?: this._comfortTemperature
        val newTempWeight = temperatureWeight ?: this._temperatureWeight
        val newHumidityWeight = humidityWeight ?: this._humidityWeight
        val newWindWeight = windWeight ?: this._windWeight
        val newUvWeight = uvWeight ?: this._uvWeight
        val newAirQualityWeight = airQualityWeight ?: this._airQualityWeight

        // 유효성 검증
        if (comfortTemperature != null) {
            require(newComfortTemp in 10..30) { "쾌적 온도는 10-30도 사이여야 합니다" }
        }

        validateWeight(newTempWeight, "온도")
        validateWeight(newHumidityWeight, "습도")
        validateWeight(newWindWeight, "바람")
        validateWeight(newUvWeight, "자외선")
        validateWeight(newAirQualityWeight, "대기질")

        // 개인 온도 보정값 재계산
        val newPersonalCorrection = (newComfortTemp - 20) * 0.5

        return copy(
            _comfortTemperature = newComfortTemp,
            _temperatureWeight = newTempWeight,
            _humidityWeight = newHumidityWeight,
            _windWeight = newWindWeight,
            _uvWeight = newUvWeight,
            _airQualityWeight = newAirQualityWeight,
            _personalTempCorrection = newPersonalCorrection
        )
    }

    /**
     * 우선순위 업데이트
     */
    fun updatePriorities(priorityFirst: String?, prioritySecond: String?): WeatherPreference {
        validatePriority(priorityFirst, "1순위")
        validatePriority(prioritySecond, "2순위")

        if (priorityFirst != null && prioritySecond != null) {
            require(priorityFirst != prioritySecond) { "1순위와 2순위는 달라야 합니다" }
        }

        return copy(
            _priorityFirst = priorityFirst,
            _prioritySecond = prioritySecond
        )
    }

    /**
     * 반응 정보 업데이트
     */
    fun updateReactions(skinReaction: String?, humidityReaction: String?): WeatherPreference {
        validateReactionLevel(skinReaction, "피부 반응")
        validateReactionLevel(humidityReaction, "습도 반응")

        return copy(
            _skinReaction = skinReaction,
            _humidityReaction = humidityReaction
        )
    }

    // ===== 질의 메서드 =====

    /**
     * 우선순위 항목들을 리스트로 반환
     */
    fun getPriorityList(): List<String> {
        return listOfNotNull(_priorityFirst, _prioritySecond)
    }

    /**
     * 특정 날씨 요소가 우선순위에 포함되어 있는지 확인
     */
    fun isPriorityElement(weatherElement: String): Boolean {
        return weatherElement in getPriorityList()
    }

    /**
     * 우선순위에 따른 패널티 가중치 반환
     */
    fun getPriorityPenaltyWeight(weatherElement: String): Double {
        return when {
            _priorityFirst == weatherElement -> 0.3  // 1순위: 70% 감점
            _prioritySecond == weatherElement -> 0.5 // 2순위: 50% 감점
            else -> 1.0 // 패널티 없음
        }
    }

    /**
     * 민감도가 높은 사용자인지 확인
     */
    fun isHighSensitivity(): Boolean {
        val highSensitivityCount = listOf(
            _skinReaction == "high",
            _humidityReaction == "high",
            _temperatureWeight >= 80,
            _humidityWeight >= 80,
            _uvWeight >= 80
        ).count { it }

        return highSensitivityCount >= 3
    }

    /**
     * 추위를 많이 타는 타입인지 확인
     */
    fun isColdSensitive(): Boolean {
        return _comfortTemperature >= 22 || isPriorityElement("cold")
    }

    /**
     * 더위를 많이 타는 타입인지 확인
     */
    fun isHeatSensitive(): Boolean {
        return _comfortTemperature <= 16 || isPriorityElement("heat")
    }

    // ===== 날씨 계산 로직 (도메인 로직) =====

    /**
     * 개인별 체감온도 계산
     */
    fun calculateFeelsLikeTemperature(
        actualTemp: Double,
        windSpeed: Double,
        humidity: Double
    ): Double {
        var feelsLikeTemp = actualTemp

        // 1. Wind Chill 계산 (10°C 이하에서만 적용)
        if (actualTemp <= 10.0 && windSpeed > 1.6) {
            feelsLikeTemp = 13.12 + 0.6215 * actualTemp -
                    11.37 * windSpeed.pow(0.16) +
                    0.3965 * actualTemp * windSpeed.pow(0.16)
        }
        // 2. Heat Index 계산 (27°C 이상에서만 적용)
        else if (actualTemp >= 27.0 && humidity >= 40.0) {
            feelsLikeTemp = calculateHeatIndex(actualTemp, humidity)
        }

        // 3. 습도 보정 적용
        val humidityCorrection = getHumidityCorrection(humidity)

        // 4. 개인별 온도 보정 적용
        return feelsLikeTemp + this._personalTempCorrection + humidityCorrection
    }

    /**
     * 날씨 요소별 가중치 반환
     */
    fun getWeightFor(weatherElement: String): Int {
        return when (weatherElement) {
            "temperature" -> _temperatureWeight
            "humidity" -> _humidityWeight
            "wind" -> _windWeight
            "uv" -> _uvWeight
            "airQuality" -> _airQualityWeight
            else -> 50 // 기본값
        }
    }

    // ===== Private 헬퍼 메서드들 =====

    private fun calculateHeatIndex(tempC: Double, humidity: Double): Double {
        val tempF = tempC * 9.0 / 5.0 + 32.0
        val heatIndexF = -42.379 + 2.04901523 * tempF + 10.14333127 * humidity -
                0.22475541 * tempF * humidity - 0.00683783 * tempF * tempF -
                0.05481717 * humidity * humidity + 0.00122874 * tempF * tempF * humidity +
                0.00085282 * tempF * humidity * humidity - 0.00000199 * tempF * tempF * humidity * humidity
        return (heatIndexF - 32.0) * 5.0 / 9.0
    }

    private fun getHumidityCorrection(humidity: Double): Double {
        val baseCorrection = when {
            humidity >= 85 -> 3.0
            humidity >= 75 -> 2.0
            humidity >= 65 -> 1.0
            humidity >= 40 -> 0.0
            humidity >= 30 -> -0.5
            else -> -1.0
        }

        val sensitivityMultiplier = when (this._humidityReaction) {
            "high" -> 1.5
            "medium" -> 1.0
            "low" -> 0.5
            else -> 1.0
        }

        return baseCorrection * sensitivityMultiplier
    }

    private fun validatePriority(priority: String?, name: String) {
        if (priority != null) {
            val validPriorities = setOf("heat", "cold", "humidity", "wind", "uv", "pollution")
            require(priority in validPriorities) { "유효하지 않은 ${name} 우선순위입니다: $priority" }
        }
    }

    private fun validateReactionLevel(level: String?, name: String) {
        if (level != null) {
            val validLevels = setOf("high", "medium", "low")
            require(level in validLevels) { "유효하지 않은 ${name} 레벨입니다: $level" }
        }
    }

    private fun validateWeight(weight: Int, name: String) {
        require(weight in 1..100) { "${name} 가중치는 1-100 사이여야 합니다" }
    }

    // ===== 불변성 보장을 위한 copy 메서드 =====

    private fun copy(
        _priorityFirst: String? = this._priorityFirst,
        _prioritySecond: String? = this._prioritySecond,
        _comfortTemperature: Int = this._comfortTemperature,
        _skinReaction: String? = this._skinReaction,
        _humidityReaction: String? = this._humidityReaction,
        _temperatureWeight: Int = this._temperatureWeight,
        _humidityWeight: Int = this._humidityWeight,
        _windWeight: Int = this._windWeight,
        _uvWeight: Int = this._uvWeight,
        _airQualityWeight: Int = this._airQualityWeight,
        _personalTempCorrection: Double = this._personalTempCorrection,
        _isSetupCompleted: Boolean = this._isSetupCompleted
    ): WeatherPreference {
        return WeatherPreference(
            id = this.id,
            memberId = this.memberId,
            _priorityFirst = _priorityFirst,
            _prioritySecond = _prioritySecond,
            _comfortTemperature = _comfortTemperature,
            _skinReaction = _skinReaction,
            _humidityReaction = _humidityReaction,
            _temperatureWeight = _temperatureWeight,
            _humidityWeight = _humidityWeight,
            _windWeight = _windWeight,
            _uvWeight = _uvWeight,
            _airQualityWeight = _airQualityWeight,
            _personalTempCorrection = _personalTempCorrection,
            _isSetupCompleted = _isSetupCompleted
        )
    }
}