package com.wout.outfit.entity.enums

import com.wout.member.entity.WeatherPreference
import com.wout.weather.entity.WeatherData

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : WeatherCondition
 * author         : MinKyu Park
 * date           : 25. 6. 3.
 * description    : 날씨 상황별 조건 enum (아웃핏 추천용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 3.        MinKyu Park       최초 생성
 */
enum class WeatherCondition(
    val description: String,
    val priority: Int
) {
    EXTREME_COLD("극한 추위", 1),
    COLD_SENSITIVE("추위 민감형", 2),
    PERFECT_WEATHER("완벽한 날씨", 3),
    HUMIDITY_RESISTANT("습도 민감형", 4),
    HEAT_EXTREME("극한 더위", 5);

    companion object {
        /**
         * 온도와 사용자 특성을 기반으로 날씨 조건 결정
         */
        fun determineCondition(
            temperature: Double,
            preferences: WeatherPreference,
            weatherData: WeatherData
        ): WeatherCondition {
            return when {
                // 극한 추위: 5도 이하 + 추위 민감형 OR 0도 이하
                temperature <= 0 ||
                        (temperature <= 5 && preferences.isColdSensitive()) -> EXTREME_COLD

                // 추위 민감형: 추위를 많이 타는 사용자가 15도 이하
                preferences.isColdSensitive() && temperature <= 15 -> COLD_SENSITIVE

                // 극한 더위: 30도 이상 + 더위 민감형 OR 35도 이상
                temperature >= 35 ||
                        (temperature >= 30 && preferences.isHeatSensitive()) -> HEAT_EXTREME

                // 습도 민감형: 습도 민감 사용자가 습도 80% 이상
                preferences.isHumiditySensitive() && weatherData.humidity >= 80 -> HUMIDITY_RESISTANT

                // 나머지는 완벽한 날씨
                else -> PERFECT_WEATHER
            }
        }

        /**
         * 날씨 조건별 우선순위 정렬
         */
        fun sortByPriority(conditions: List<WeatherCondition>): List<WeatherCondition> {
            return conditions.sortedBy { it.priority }
        }

        /**
         * 특정 온도 범위의 기본 조건 반환
         */
        fun getDefaultConditionForTemperature(temperature: Double): WeatherCondition {
            return when {
                temperature <= 5 -> EXTREME_COLD
                temperature >= 30 -> HEAT_EXTREME
                else -> PERFECT_WEATHER
            }
        }
    }
}