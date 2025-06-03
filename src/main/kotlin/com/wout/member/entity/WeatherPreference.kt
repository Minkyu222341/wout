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
 * description    : 사용자 날씨 선호도 및 민감도 설정 엔티티 (언더바 제거, QueryDSL 최적화)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       개발 가이드에 맞게 수정 (update 메서드 추가)
 * 2025-06-03        MinKyu Park       언더바 제거, 불변성 강화, QueryDSL 친화적으로 개선
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

        private fun validateLocation(latitude: Double?, longitude: Double?) {
            if (latitude != null || longitude != null) {
                require(latitude != null && longitude != null) {
                    "위도와 경도는 함께 설정되어야 합니다"
                }
                require(latitude in -90.0..90.0) { "위도는 -90~90 범위여야 합니다" }
                require(longitude in -180.0..180.0) { "경도는 -180~180 범위여야 합니다" }
            }
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
        val newComfortTemp = comfortTemperature ?: this.comfortTemperature
        val newTempWeight = temperatureWeight ?: this.temperatureWeight
        val newHumidityWeight = humidityWeight ?: this.humidityWeight
        val newWindWeight = windWeight ?: this.windWeight
        val newUvWeight = uvWeight ?: this.uvWeight
        val newAirQualityWeight = airQualityWeight ?: this.airQualityWeight

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
            comfortTemperature = newComfortTemp,
            temperatureWeight = newTempWeight,
            humidityWeight = newHumidityWeight,
            windWeight = newWindWeight,
            uvWeight = newUvWeight,
            airQualityWeight = newAirQualityWeight,
            personalTempCorrection = newPersonalCorrection
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
            priorityFirst = priorityFirst,
            prioritySecond = prioritySecond
        )
    }

    /**
     * 반응 정보 업데이트
     */
    fun updateReactions(skinReaction: String?, humidityReaction: String?): WeatherPreference {
        validateReactionLevel(skinReaction, "피부 반응")
        validateReactionLevel(humidityReaction, "습도 반응")

        return copy(
            skinReaction = skinReaction,
            humidityReaction = humidityReaction
        )
    }

    // ===== 질의 메서드 =====

    /**
     * 우선순위 항목들을 리스트로 반환
     */
    fun getPriorityList(): List<String> {
        return listOfNotNull(priorityFirst, prioritySecond)
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
            priorityFirst == weatherElement -> 0.3  // 1순위: 70% 감점
            prioritySecond == weatherElement -> 0.5 // 2순위: 50% 감점
            else -> 1.0 // 패널티 없음
        }
    }

    /**
     * 민감도가 높은 사용자인지 확인
     */
    fun isHighSensitivity(): Boolean {
        val highSensitivityCount = listOf(
            skinReaction == "high",
            humidityReaction == "high",
            temperatureWeight >= 80,
            humidityWeight >= 80,
            uvWeight >= 80
        ).count { it }

        return highSensitivityCount >= 3
    }

    /**
     * 추위를 많이 타는 타입인지 확인
     */
    fun isColdSensitive(): Boolean {
        return comfortTemperature >= 22 || isPriorityElement("cold")
    }

    /**
     * 더위를 많이 타는 타입인지 확인
     */
    fun isHeatSensitive(): Boolean {
        return comfortTemperature <= 16 || isPriorityElement("heat")
    }

    // 🆕 아웃핏 추천을 위한 민감도 판단 메서드들

    /**
     * 습도에 민감한지 확인 (아웃핏 추천용)
     */
    fun isHumiditySensitive(): Boolean {
        return isPriorityElement("humidity") || humidityReaction == "high" || humidityWeight >= 70
    }

    /**
     * 바람에 민감한지 확인 (아웃핏 추천용)
     */
    fun isWindSensitive(): Boolean {
        return isPriorityElement("wind") || windWeight >= 70
    }

    /**
     * 자외선에 민감한지 확인 (아웃핏 추천용)
     */
    fun isUVSensitive(): Boolean {
        return isPriorityElement("uv") || skinReaction == "high" || uvWeight >= 70
    }

    /**
     * 대기질에 민감한지 확인 (아웃핏 추천용)
     */
    fun isAirQualitySensitive(): Boolean {
        return isPriorityElement("pollution") || airQualityWeight >= 70
    }

    /**
     * 사용자 타입 특성 요약 (아웃핏 추천 메시지용)
     */
    fun getPersonalityTraits(): List<String> {
        val traits = mutableListOf<String>()

        if (isColdSensitive()) traits.add("추위를 많이 타는 편")
        if (isHeatSensitive()) traits.add("더위를 많이 타는 편")
        if (isHumiditySensitive()) traits.add("습함을 특히 싫어하는 편")
        if (isUVSensitive()) traits.add("자외선에 예민한 편")
        if (isWindSensitive()) traits.add("바람을 싫어하는 편")
        if (isAirQualitySensitive()) traits.add("미세먼지에 민감한 편")

        return traits
    }

    /**
     * 개인화 메시지 생성을 위한 주요 특성 반환
     */
    fun getPrimaryTrait(): String? {
        return when {
            isColdSensitive() && comfortTemperature >= 24 -> "극도로 추위를 타시는데"
            isColdSensitive() -> "추위를 많이 타시는데"
            isHeatSensitive() && comfortTemperature <= 14 -> "극도로 더위를 타시는데"
            isHeatSensitive() -> "더위를 많이 타시는데"
            isHumiditySensitive() && humidityReaction == "high" -> "습함을 특히 싫어하시는데"
            isUVSensitive() && skinReaction == "high" -> "자외선에 매우 예민하셔서"
            isWindSensitive() && windWeight >= 80 -> "바람을 특히 싫어하시는데"
            else -> null
        }
    }

    /**
     * 온도 구간별 민감도 보정값 계산
     * 개인 특성에 따라 온도 구간을 조정하여 더 정확한 카테고리 선택
     */
    fun getTemperatureAdjustment(actualTemp: Double): Double {
        var adjustment = 0.0

        // 추위 민감형: 체감온도를 더 낮게 느끼도록 조정
        if (isColdSensitive()) {
            adjustment -= when {
                comfortTemperature >= 26 -> 3.0  // 매우 추위 많이 탐
                comfortTemperature >= 24 -> 2.0  // 추위 많이 탐
                else -> 1.0                       // 조금 추위 탐
            }
        }

        // 더위 민감형: 체감온도를 더 높게 느끼도록 조정
        if (isHeatSensitive()) {
            adjustment += when {
                comfortTemperature <= 14 -> 3.0  // 매우 더위 많이 탐
                comfortTemperature <= 16 -> 2.0  // 더위 많이 탐
                else -> 1.0                       // 조금 더위 탐
            }
        }

        return adjustment
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
        if (actualTemp <= 10.0 && windSpeed >= 1.34) {
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
        return feelsLikeTemp + this.personalTempCorrection + humidityCorrection
    }

    /**
     * 날씨 요소별 가중치 반환
     */
    fun getWeightFor(weatherElement: String): Int {
        return when (weatherElement) {
            "temperature" -> temperatureWeight
            "humidity" -> humidityWeight
            "wind" -> windWeight
            "uv" -> uvWeight
            "airQuality" -> airQualityWeight
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

        val sensitivityMultiplier = when (this.humidityReaction) {
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
            require(priority in validPriorities) { "유효하지 않은 $name 우선순위입니다: $priority" }
        }
    }

    private fun validateReactionLevel(level: String?, name: String) {
        if (level != null) {
            val validLevels = setOf("high", "medium", "low")
            require(level in validLevels) { "유효하지 않은 $name 레벨입니다: $level" }
        }
    }

    private fun validateWeight(weight: Int, name: String) {
        require(weight in 1..100) { "${name} 가중치는 1-100 사이여야 합니다" }
    }

    // ===== 불변성 보장을 위한 copy 메서드 =====

    private fun copy(
        priorityFirst: String? = this.priorityFirst,
        prioritySecond: String? = this.prioritySecond,
        comfortTemperature: Int = this.comfortTemperature,
        skinReaction: String? = this.skinReaction,
        humidityReaction: String? = this.humidityReaction,
        temperatureWeight: Int = this.temperatureWeight,
        humidityWeight: Int = this.humidityWeight,
        windWeight: Int = this.windWeight,
        uvWeight: Int = this.uvWeight,
        airQualityWeight: Int = this.airQualityWeight,
        personalTempCorrection: Double = this.personalTempCorrection,
        isSetupCompleted: Boolean = this.isSetupCompleted
    ): WeatherPreference {
        return WeatherPreference(
            id = this.id,
            memberId = this.memberId,
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
            personalTempCorrection = personalTempCorrection,
            isSetupCompleted = isSetupCompleted
        )
    }
}