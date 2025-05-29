package com.wout.member.util

import com.wout.member.entity.WeatherPreference
import org.springframework.stereotype.Component
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow

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
 * 2025-05-29        MinKyu Park       Magic Number 상수화 (유지보수성 향상)
 */
@Component
class WeatherScoreCalculator {

    companion object {
        // 기온 계산 상수
        private const val OPTIMAL_TEMPERATURE = 22.0  // 최적 온도 (섭씨)
        private const val TEMPERATURE_SIGMA = 8.0     // 온도 분포 표준편차

        // 습도 계산 상수
        private const val HUMIDITY_OPTIMAL_MIN = 40.0  // 최적 습도 최소값
        private const val HUMIDITY_OPTIMAL_MAX = 60.0  // 최적 습도 최대값

        // 바람 계산 상수
        private const val WIND_OPTIMAL_MIN = 2.0  // 최적 풍속 최소값 (m/s)
        private const val WIND_OPTIMAL_MAX = 3.0  // 최적 풍속 최대값 (m/s)

        // 우선순위 패널티 임계값
        private const val HEAT_THRESHOLD = 30.0      // 더위 기준 온도
        private const val COLD_THRESHOLD = 5.0       // 추위 기준 온도
        private const val HUMIDITY_THRESHOLD = 75.0  // 습함 기준 습도
        private const val WIND_THRESHOLD = 6.0       // 강풍 기준
        private const val UV_THRESHOLD = 8.0         // 자외선 위험 기준
        private const val PM25_THRESHOLD = 75.0      // PM2.5 나쁨 기준
        private const val PM10_THRESHOLD = 150.0     // PM10 나쁨 기준

        // 점수 등급 경계값
        private const val PERFECT_SCORE_THRESHOLD = 90.0  // 완벽한 날씨
        private const val GOOD_SCORE_THRESHOLD = 70.0     // 좋은 날씨
        private const val FAIR_SCORE_THRESHOLD = 50.0     // 보통 날씨
        private const val POOR_SCORE_THRESHOLD = 30.0     // 아쉬운 날씨

        // 개인 가중치 기준값
        private const val DEFAULT_WEIGHT_BASE = 50.0      // 기본 가중치 기준값

        // 대기질 계산 비율
        private const val PM25_WEIGHT_RATIO = 0.7  // PM2.5 비중
        private const val PM10_WEIGHT_RATIO = 0.3  // PM10 비중
    }

    /**
     * 전체 날씨 점수 계산
     */
    fun calculateTotalScore(
        temperature: Double,
        humidity: Double,
        windSpeed: Double,
        uvIndex: Double,
        pm25: Double,
        pm10: Double,
        weatherPreference: WeatherPreference
    ): WeatherScoreResult {

        // 1단계: 요소별 기본 점수 계산
        val temperatureScore = calculateTemperatureScore(temperature)
        val humidityScore = calculateHumidityScore(humidity)
        val windScore = calculateWindScore(windSpeed)
        val uvScore = calculateUvScore(uvIndex)
        val airQualityScore = calculateAirQualityScore(pm25, pm10)

        val baseScores = ElementScores(
            temperature = temperatureScore,
            humidity = humidityScore,
            wind = windScore,
            uv = uvScore,
            airQuality = airQualityScore
        )

        // 2단계: 개인 가중치 적용
        val weightedScores = applyPersonalWeights(baseScores, weatherPreference)

        // 3단계: 우선순위 패널티 적용
        val finalScores = applyPriorityPenalties(
            weightedScores,
            weatherPreference,
            temperature, humidity, windSpeed, uvIndex, pm25, pm10
        )

        // 4단계: 최종 점수 계산 (가중평균)
        val totalScore = calculateWeightedAverage(finalScores, weatherPreference)

        return WeatherScoreResult(
            totalScore = totalScore,
            grade = getWeatherGrade(totalScore),
            elementScores = baseScores,
            weightedScores = finalScores,
            appliedWeights = getAppliedWeights(weatherPreference)
        )
    }

    /**
     * 1단계: 기온 점수 계산 (22도 기준 가우시안 분포)
     */
    private fun calculateTemperatureScore(temperature: Double): Double {
        // 가우시안 함수: e^(-(x-μ)²/(2σ²))
        val score = exp(-((temperature - OPTIMAL_TEMPERATURE).pow(2)) / (2 * TEMPERATURE_SIGMA.pow(2))) * 100

        return score.coerceIn(0.0, 100.0)
    }

    /**
     * 1단계: 습도 점수 계산 (40-60% 최적 구간)
     */
    private fun calculateHumidityScore(humidity: Double): Double {
        return when {
            humidity in HUMIDITY_OPTIMAL_MIN..HUMIDITY_OPTIMAL_MAX -> 100.0  // 최적 구간
            humidity in 30.0..HUMIDITY_OPTIMAL_MIN || humidity in HUMIDITY_OPTIMAL_MAX..70.0 -> {
                // 선형 감소
                val distance = if (humidity < HUMIDITY_OPTIMAL_MIN) HUMIDITY_OPTIMAL_MIN - humidity else humidity - HUMIDITY_OPTIMAL_MAX
                100 - (distance * 3)  // 1%당 3점 감소
            }
            humidity in 20.0..30.0 || humidity in 70.0..80.0 -> {
                val distance = if (humidity < 30) 30 - humidity else humidity - 70
                70 - (distance * 2)  // 추가 감소
            }
            else -> {
                // 극값 처리
                max(0.0, 50 - abs(humidity - 50) * 2)
            }
        }.coerceIn(0.0, 100.0)
    }

    /**
     * 1단계: 바람 점수 계산 (2-3m/s 최적)
     */
    private fun calculateWindScore(windSpeed: Double): Double {
        return when {
            windSpeed in WIND_OPTIMAL_MIN..WIND_OPTIMAL_MAX -> 100.0  // 최적
            windSpeed in 1.0..WIND_OPTIMAL_MIN || windSpeed in WIND_OPTIMAL_MAX..4.0 -> 85.0  // 좋음
            windSpeed in 0.5..1.0 || windSpeed in 4.0..WIND_THRESHOLD -> 70.0  // 보통
            windSpeed < 0.5 -> 60.0  // 무풍 (답답함)
            windSpeed in WIND_THRESHOLD..10.0 -> 50.0  // 강풍
            else -> 20.0  // 매우 강풍
        }.coerceIn(0.0, 100.0)
    }

    /**
     * 1단계: 자외선 점수 계산 (낮을수록 좋음)
     */
    private fun calculateUvScore(uvIndex: Double): Double {
        return when {
            uvIndex <= 2 -> 100.0     // 낮음
            uvIndex <= 5 -> 80.0      // 보통
            uvIndex <= 7 -> 60.0      // 높음
            uvIndex <= 10 -> 40.0     // 매우 높음
            else -> 20.0              // 위험
        }.coerceIn(0.0, 100.0)
    }

    /**
     * 1단계: 대기질 점수 계산
     */
    private fun calculateAirQualityScore(pm25: Double, pm10: Double): Double {
        // PM2.5 기준 점수
        val pm25Score = when {
            pm25 <= 15 -> 100.0      // 좋음
            pm25 <= 35 -> 80.0       // 보통
            pm25 <= PM25_THRESHOLD -> 60.0       // 나쁨
            else -> 30.0             // 매우 나쁨
        }

        // PM10 기준 점수
        val pm10Score = when {
            pm10 <= 30 -> 100.0      // 좋음
            pm10 <= 80 -> 80.0       // 보통
            pm10 <= PM10_THRESHOLD -> 60.0      // 나쁨
            else -> 30.0             // 매우 나쁨
        }

        // PM2.5가 더 중요하므로 7:3 비율
        return (pm25Score * PM25_WEIGHT_RATIO + pm10Score * PM10_WEIGHT_RATIO).coerceIn(0.0, 100.0)
    }

    /**
     * 2단계: 개인 가중치 적용
     */
    private fun applyPersonalWeights(
        baseScores: ElementScores,
        preference: WeatherPreference
    ): ElementScores {
        return ElementScores(
            temperature = baseScores.temperature * (preference.temperatureWeight / DEFAULT_WEIGHT_BASE),
            humidity = baseScores.humidity * (preference.humidityWeight / DEFAULT_WEIGHT_BASE),
            wind = baseScores.wind * (preference.windWeight / DEFAULT_WEIGHT_BASE),
            uv = baseScores.uv * (preference.uvWeight / DEFAULT_WEIGHT_BASE),
            airQuality = baseScores.airQuality * (preference.airQualityWeight / DEFAULT_WEIGHT_BASE)
        )
    }

    /**
     * 3단계: 우선순위 패널티 적용
     */
    private fun applyPriorityPenalties(
        scores: ElementScores,
        preference: WeatherPreference,
        temperature: Double,
        humidity: Double,
        windSpeed: Double,
        uvIndex: Double,
        pm25: Double,
        pm10: Double
    ): ElementScores {
        var penalizedScores = scores.copy()

        // 우선순위 요소들에 대한 패널티 적용
        preference.getPriorityList().forEach { priority ->
            val penaltyWeight = preference.getPriorityPenaltyWeight(priority)

            when (priority) {
                "heat" -> {
                    if (temperature >= HEAT_THRESHOLD) {
                        penalizedScores = penalizedScores.copy(
                            temperature = penalizedScores.temperature * penaltyWeight
                        )
                    }
                }
                "cold" -> {
                    if (temperature <= COLD_THRESHOLD) {
                        penalizedScores = penalizedScores.copy(
                            temperature = penalizedScores.temperature * penaltyWeight
                        )
                    }
                }
                "humidity" -> {
                    if (humidity >= HUMIDITY_THRESHOLD) {  // 장마철 수준
                        penalizedScores = penalizedScores.copy(
                            humidity = penalizedScores.humidity * penaltyWeight
                        )
                    }
                }
                "wind" -> {
                    if (windSpeed >= WIND_THRESHOLD) {  // 강풍
                        penalizedScores = penalizedScores.copy(
                            wind = penalizedScores.wind * penaltyWeight
                        )
                    }
                }
                "uv" -> {
                    if (uvIndex >= UV_THRESHOLD) {  // 매우 높음
                        penalizedScores = penalizedScores.copy(
                            uv = penalizedScores.uv * penaltyWeight
                        )
                    }
                }
                "pollution" -> {
                    if (pm25 >= PM25_THRESHOLD || pm10 >= PM10_THRESHOLD) {  // 나쁨 수준
                        penalizedScores = penalizedScores.copy(
                            airQuality = penalizedScores.airQuality * penaltyWeight
                        )
                    }
                }
            }
        }

        return penalizedScores
    }

    /**
     * 4단계: 가중평균으로 최종 점수 계산
     */
    private fun calculateWeightedAverage(
        scores: ElementScores,
        preference: WeatherPreference
    ): Double {
        val weights = getAppliedWeights(preference)
        val totalWeight = weights.values.sum()

        if (totalWeight == 0.0) return 0.0

        val weightedSum = scores.temperature * weights["temperature"]!! +
                scores.humidity * weights["humidity"]!! +
                scores.wind * weights["wind"]!! +
                scores.uv * weights["uv"]!! +
                scores.airQuality * weights["airQuality"]!!

        return (weightedSum / totalWeight).coerceIn(0.0, 100.0)
    }

    /**
     * 적용된 가중치 맵 생성
     */
    private fun getAppliedWeights(preference: WeatherPreference): Map<String, Double> {
        return mapOf(
            "temperature" to preference.temperatureWeight.toDouble(),
            "humidity" to preference.humidityWeight.toDouble(),
            "wind" to preference.windWeight.toDouble(),
            "uv" to preference.uvWeight.toDouble(),
            "airQuality" to preference.airQualityWeight.toDouble()
        )
    }

    /**
     * 점수에 따른 등급 계산
     */
    private fun getWeatherGrade(score: Double): WeatherGrade {
        return when {
            score >= PERFECT_SCORE_THRESHOLD -> WeatherGrade.PERFECT
            score >= GOOD_SCORE_THRESHOLD -> WeatherGrade.GOOD
            score >= FAIR_SCORE_THRESHOLD -> WeatherGrade.FAIR
            score >= POOR_SCORE_THRESHOLD -> WeatherGrade.POOR
            else -> WeatherGrade.TERRIBLE
        }
    }
}

/**
 * 요소별 점수 데이터 클래스
 */
data class ElementScores(
    val temperature: Double,
    val humidity: Double,
    val wind: Double,
    val uv: Double,
    val airQuality: Double
)

/**
 * 날씨 점수 계산 결과
 */
data class WeatherScoreResult(
    val totalScore: Double,
    val grade: WeatherGrade,
    val elementScores: ElementScores,
    val weightedScores: ElementScores,
    val appliedWeights: Map<String, Double>
)

/**
 * 날씨 등급 enum
 */
enum class WeatherGrade(
    val emoji: String,
    val description: String
) {
    PERFECT("😊", "완벽한 날씨"),
    GOOD("😌", "좋은 날씨"),
    FAIR("😐", "보통 날씨"),
    POOR("😰", "아쉬운 날씨"),
    TERRIBLE("😵", "힘든 날씨")
}