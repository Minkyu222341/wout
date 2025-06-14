package com.wout.member.util

import com.wout.member.entity.WeatherPreference
import com.wout.member.model.ElementWeights
import org.springframework.stereotype.Component
import kotlin.math.*

/**
 * packageName    : com.wout.member.util
 * fileName       : WeatherScoreCalculator
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 날씨 점수 계산 전용 유틸리티 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-05-29        MinKyu Park       Magic Number 상수화
 * 2025-06-08        MinKyu Park       디버깅 로그 추가
 * 2025-06-08        MinKyu Park       √보정·25~75 가중치 리팩터링
 */


/**
 * 날씨 점수 계산 전용 유틸
 *  - 1단계: 요소별 기본 점수
 *  - 2단계: 개인 가중치(25~75) + √보정
 *  - 3단계: 우선순위 패널티
 *  - 4단계: 가중 평균 → 0~100 cap
 */
@Component
class WeatherScoreCalculator {

    /* ---------- 상수 영역 ---------- */

    private companion object {
        /* 요소별 최적·임계값 */
        const val OPTIMAL_TEMPERATURE = 22.0
        const val TEMPERATURE_SIGMA   = 8.0
        const val HUMIDITY_OPTIMAL_MIN = 40.0
        const val HUMIDITY_OPTIMAL_MAX = 60.0
        const val WIND_OPTIMAL_MIN = 2.0
        const val WIND_OPTIMAL_MAX = 3.0

        /* 패널티 임계값 */
        const val HEAT_THRESHOLD = 30.0
        const val COLD_THRESHOLD = 5.0
        const val HUMIDITY_THRESHOLD = 75.0
        const val WIND_THRESHOLD = 6.0
        const val UV_THRESHOLD   = 8.0
        const val PM25_THRESHOLD = 75.0
        const val PM10_THRESHOLD = 150.0

        /* 대기질 가중 */
        const val PM25_WEIGHT_RATIO = 0.7
        const val PM10_WEIGHT_RATIO = 0.3
    }

    /* ---------- Public API ---------- */

    fun calculateTotalScore(
        temperature: Double,
        humidity: Double,
        windSpeed: Double,
        uvIndex: Double,
        pm25: Double,
        pm10: Double,
        preference: WeatherPreference
    ): WeatherScoreResult {

        /* 1단계: 요소별 기본 점수 */
        val base = ElementScores(
            temperature = calculateTemperatureScore(temperature),
            humidity    = calculateHumidityScore(humidity),
            wind        = calculateWindScore(windSpeed),
            uv          = calculateUvScore(uvIndex),
            airQuality  = calculateAirQualityScore(pm25, pm10)
        )

        /* 2단계: 개선 가중치 + √보정 */
        val weighted = applyImprovedWeights(base, preference)

        /* 3단계: 우선순위 패널티 */
        val penalized = applyPriorityPenalties(
            weighted, preference,
            temperature, humidity, windSpeed, uvIndex, pm25, pm10
        )

        /* 4단계: 가중 평균 */
        val totalScore = calculateWeightedAverage(penalized, preference)

        return WeatherScoreResult(
            totalScore = totalScore,
            grade = getWeatherGrade(totalScore),
            elementScores  = base,
            weightedScores = penalized,
            appliedWeights = preference.calculateImprovedWeights().toDoubleMap()
        )
    }

    /* ---------- 1단계: 요소별 기본 점수 ---------- */

    private fun calculateTemperatureScore(t: Double) =
        (exp(-((t - OPTIMAL_TEMPERATURE).pow(2)) / (2 * TEMPERATURE_SIGMA.pow(2))) * 100)
            .coerceIn(0.0, 100.0)

    private fun calculateHumidityScore(h: Double): Double = when {
        h in HUMIDITY_OPTIMAL_MIN..HUMIDITY_OPTIMAL_MAX -> 100.0
        h in 30.0..HUMIDITY_OPTIMAL_MIN || h in HUMIDITY_OPTIMAL_MAX..70.0 -> {
            val d = if (h < HUMIDITY_OPTIMAL_MIN) HUMIDITY_OPTIMAL_MIN - h else h - HUMIDITY_OPTIMAL_MAX
            100 - d * 3
        }
        h in 20.0..30.0 || h in 70.0..80.0 -> {
            val d = if (h < 30) 30 - h else h - 70
            70 - d * 2
        }
        else -> max(0.0, 50 - abs(h - 50) * 2)
    }.coerceIn(0.0, 100.0)

    private fun calculateWindScore(w: Double): Double = when {
        w in WIND_OPTIMAL_MIN..WIND_OPTIMAL_MAX -> 100.0
        w in 1.0..WIND_OPTIMAL_MIN || w in WIND_OPTIMAL_MAX..4.0 -> 85.0
        w in 0.5..1.0 || w in 4.0..WIND_THRESHOLD -> 70.0
        w < 0.5 -> 60.0
        w in WIND_THRESHOLD..10.0 -> 50.0
        else -> 20.0
    }

    private fun calculateUvScore(uv: Double): Double = when {
        uv <= 2 -> 100.0
        uv <= 5 -> 80.0
        uv <= 7 -> 60.0
        uv <= 10 -> 40.0
        else -> 20.0
    }

    private fun calculateAirQualityScore(pm25: Double, pm10: Double): Double {
        val pm25Score = when {
            pm25 <= 15 -> 100.0
            pm25 <= 35 -> 80.0
            pm25 <= PM25_THRESHOLD -> 60.0
            else -> 30.0
        }
        val pm10Score = when {
            pm10 <= 30 -> 100.0
            pm10 <= 80 -> 80.0
            pm10 <= PM10_THRESHOLD -> 60.0
            else -> 30.0
        }
        return (pm25Score * PM25_WEIGHT_RATIO + pm10Score * PM10_WEIGHT_RATIO)
            .coerceIn(0.0, 100.0)
    }

    /* ---------- 2단계: 개선 가중치 + √보정 ---------- */

    private fun applyImprovedWeights(
        base: ElementScores,
        pref: WeatherPreference
    ): ElementScores {
        val w: ElementWeights = pref.calculateImprovedWeights()

        fun adjust(score: Double, weight: Int): Double =
            (score * sqrt(weight.toDouble() / 50.0)).coerceIn(0.0, 100.0)

        return ElementScores(
            temperature = adjust(base.temperature, w.temperature),
            humidity    = adjust(base.humidity,    w.humidity),
            wind        = adjust(base.wind,        w.wind),
            uv          = adjust(base.uv,          w.uv),
            airQuality  = adjust(base.airQuality,  w.airQuality)
        )
    }

    /* ---------- 3단계: 우선순위 패널티 ---------- */

    private fun applyPriorityPenalties(
        s: ElementScores,
        p: WeatherPreference,
        t: Double, h: Double, w: Double, uv: Double, pm25: Double, pm10: Double
    ): ElementScores {
        var sc = s
        p.getPriorityList().forEach { pr ->
            val factor = p.getPriorityPenaltyWeight(pr)
            sc = when (pr) {
                "heat"  -> if (t >= HEAT_THRESHOLD) sc.copy(temperature = sc.temperature * factor) else sc
                "cold"  -> if (t <= COLD_THRESHOLD) sc.copy(temperature = sc.temperature * factor) else sc
                "humidity" -> if (h >= HUMIDITY_THRESHOLD) sc.copy(humidity = sc.humidity * factor) else sc
                "wind"  -> if (w >= WIND_THRESHOLD) sc.copy(wind = sc.wind * factor) else sc
                "uv"    -> if (uv >= UV_THRESHOLD)  sc.copy(uv = sc.uv * factor) else sc
                "pollution" ->
                    if (pm25 >= PM25_THRESHOLD || pm10 >= PM10_THRESHOLD)
                        sc.copy(airQuality = sc.airQuality * factor) else sc
                else -> sc
            }
        }
        return sc
    }

    /* ---------- 4단계: 가중 평균 ---------- */

    private fun calculateWeightedAverage(
        s: ElementScores,
        pref: WeatherPreference
    ): Double {
        val w = pref.calculateImprovedWeights()
        val total = (w.temperature + w.humidity + w.wind + w.uv + w.airQuality).toDouble()
        val sum =
            s.temperature * w.temperature +
                    s.humidity    * w.humidity +
                    s.wind        * w.wind +
                    s.uv          * w.uv +
                    s.airQuality  * w.airQuality
        return (sum / total).coerceIn(0.0, 100.0)
    }

    /* ---------- 보조 ---------- */

    private fun getWeatherGrade(score: Double): WeatherGrade = when {
        score >= 90 -> WeatherGrade.PERFECT
        score >= 70 -> WeatherGrade.GOOD
        score >= 50 -> WeatherGrade.FAIR
        score >= 30 -> WeatherGrade.POOR
        else        -> WeatherGrade.TERRIBLE
    }
}

/* ===== 데이터 클래스 및 enum ===== */

data class ElementScores(
    val temperature: Double,
    val humidity: Double,
    val wind: Double,
    val uv: Double,
    val airQuality: Double
)

data class WeatherScoreResult(
    val totalScore: Double,
    val grade: WeatherGrade,
    val elementScores: ElementScores,
    val weightedScores: ElementScores,
    val appliedWeights: Map<String, Double>
)

enum class WeatherGrade(val emoji: String, val description: String) {
    PERFECT("😊", "완벽한 날씨"),
    GOOD("😌", "좋은 날씨"),
    FAIR("😐", "보통 날씨"),
    POOR("😰", "아쉬운 날씨"),
    TERRIBLE("😵", "힘든 날씨")
}

/* ===== ElementWeights → Map 변환 헬퍼 ===== */

private fun ElementWeights.toDoubleMap(): Map<String, Double> = mapOf(
    "temperature" to temperature.toDouble(),
    "humidity"    to humidity.toDouble(),
    "wind"        to wind.toDouble(),
    "uv"          to uv.toDouble(),
    "airQuality"  to airQuality.toDouble()
)
