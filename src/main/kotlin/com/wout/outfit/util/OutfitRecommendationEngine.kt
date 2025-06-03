package com.wout.outfit.util

import com.wout.member.entity.WeatherPreference
import com.wout.member.util.WeatherGrade
import com.wout.member.util.WeatherScoreCalculator
import com.wout.member.util.WeatherScoreResult
import com.wout.outfit.entity.OutfitRecommendation
import com.wout.outfit.entity.enums.BottomCategory
import com.wout.outfit.entity.enums.OuterCategory
import com.wout.outfit.entity.enums.TopCategory
import com.wout.outfit.entity.enums.WeatherCondition
import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.outfit.util
 * fileName       : OutfitRecommendationEngine
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 날씨와 사용자 선호도를 기반으로 한 아웃핏 추천 엔진
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       WeatherCondition Enum 적용
 * 2025-06-03        MinKyu Park       NPE 방지, UUID 적용, Reflection 제거
 * 2025-06-03        MinKyu Park       기존 WeatherScoreCalculator 활용
 */
@Component
class OutfitRecommendationEngine(
    private val outfitItemDatabase: OutfitItemDatabase,
    private val weatherScoreCalculator: WeatherScoreCalculator
) {

    /**
     * 메인 추천 함수: 날씨와 사용자 선호도를 기반으로 완전한 아웃핏 추천
     */
    fun generateOutfitRecommendation(
        weatherData: WeatherData,
        preferences: WeatherPreference,
        memberId: Long
    ): OutfitRecommendation {

        val personalFeelsLike = preferences.calculateFeelsLikeTemperature(
            weatherData.temperature,
            weatherData.windSpeed,
            weatherData.humidity.toDouble()
        )

        // WeatherCondition Enum 사용
        val weatherCondition = WeatherCondition.determineCondition(
            personalFeelsLike,
            preferences,
            weatherData
        )

        val topCategory = determineTopCategory(personalFeelsLike, preferences)
        val bottomCategory = determineBottomCategory(personalFeelsLike, preferences)
        val outerCategory = determineOuterCategory(personalFeelsLike, weatherData, preferences)

        // 직접 메서드 호출 (Reflection 제거)
        val topRecommendations = outfitItemDatabase.getTopItemsForWeather(
            topCategory, weatherCondition, preferences, personalFeelsLike
        )

        val bottomRecommendations = outfitItemDatabase.getBottomItemsForWeather(
            bottomCategory, weatherCondition, preferences, personalFeelsLike
        )

        val outerRecommendations = if (outerCategory != null) {
            outfitItemDatabase.getOuterItemsForWeather(
                outerCategory, weatherCondition, weatherData, preferences, personalFeelsLike
            )
        } else {
            emptyList()
        }

        val accessoryRecommendations = outfitItemDatabase.getAccessoryItemsForWeather(
            weatherCondition, weatherData, preferences, personalFeelsLike
        )

        val personalizedMessage = generatePersonalizedMessage(
            weatherCondition, personalFeelsLike, preferences, weatherData
        )

        // 기존 WeatherScoreCalculator 사용
        val scoreResult = weatherScoreCalculator.calculateTotalScore(
            temperature = weatherData.temperature,
            humidity = weatherData.humidity.toDouble(),
            windSpeed = weatherData.windSpeed,
            uvIndex = weatherData.uvIndex ?: 0.0,
            pm25 = weatherData.pm25 ?: 0.0,
            pm10 = weatherData.pm10 ?: 0.0,
            weatherPreference = preferences
        )

        // 기존 로직 기반 개인 맞춤 팁 생성
        val personalTip = generatePersonalTipFromScore(scoreResult, weatherCondition, preferences)

        return OutfitRecommendation.create(
            memberId = memberId,
            weatherDataId = weatherData.id!!,
            temperature = weatherData.temperature,
            feelsLikeTemperature = personalFeelsLike,
            weatherScore = scoreResult.totalScore.toInt(),
            topCategory = topCategory,
            topItems = topRecommendations,
            bottomCategory = bottomCategory,
            bottomItems = bottomRecommendations,
            outerCategory = outerCategory,
            outerItems = outerRecommendations,
            accessoryItems = accessoryRecommendations,
            recommendationReason = personalizedMessage,
            personalTip = personalTip
        )
    }

    // ===== 카테고리 결정 로직 =====

    /**
     * 상의 카테고리 결정
     */
    private fun determineTopCategory(
        feelsLikeTemperature: Double,
        preferences: WeatherPreference
    ): TopCategory {
        return when {
            feelsLikeTemperature >= 30 -> {
                if (preferences.isHeatSensitive()) TopCategory.SLEEVELESS else TopCategory.T_SHIRT
            }

            feelsLikeTemperature >= 25 -> TopCategory.T_SHIRT
            feelsLikeTemperature >= 22 -> {
                if (preferences.isHumiditySensitive()) TopCategory.LINEN_SHIRT else TopCategory.T_SHIRT
            }

            feelsLikeTemperature >= 20 -> TopCategory.LONG_SLEEVE
            feelsLikeTemperature >= 17 -> {
                if (preferences.isColdSensitive()) TopCategory.SWEATER else TopCategory.LIGHT_SWEATER
            }

            feelsLikeTemperature >= 12 -> TopCategory.SWEATER
            feelsLikeTemperature >= 9 -> {
                if (preferences.isColdSensitive()) TopCategory.THICK_SWEATER else TopCategory.HOODIE
            }

            feelsLikeTemperature >= 5 -> TopCategory.HOODIE_THICK
            else -> TopCategory.THICK_SWEATER
        }
    }

    /**
     * 하의 카테고리 결정
     */
    private fun determineBottomCategory(
        feelsLikeTemperature: Double,
        preferences: WeatherPreference
    ): BottomCategory {
        return when {
            feelsLikeTemperature >= 28 -> BottomCategory.SHORTS
            feelsLikeTemperature >= 20 -> {
                if (preferences.isHumiditySensitive()) BottomCategory.LIGHT_PANTS else BottomCategory.JEANS
            }

            feelsLikeTemperature >= 15 -> BottomCategory.JEANS
            feelsLikeTemperature >= 10 -> {
                if (preferences.isColdSensitive()) BottomCategory.THERMAL_PANTS else BottomCategory.THICK_PANTS
            }

            else -> BottomCategory.THERMAL_PANTS
        }
    }

    /**
     * 외투 카테고리 결정
     */
    private fun determineOuterCategory(
        feelsLikeTemperature: Double,
        weatherData: WeatherData,
        preferences: WeatherPreference
    ): OuterCategory? {
        // Safe call 사용으로 NPE 방지
        val hasRain = weatherData.rain1h?.let { it > 0 } ?: false
        val hasStrongWind = weatherData.windSpeed >= 7.0

        return when {
            feelsLikeTemperature >= 27 -> {
                when {
                    hasRain -> OuterCategory.WINDBREAKER
                    else -> null // 외투 불필요
                }
            }

            feelsLikeTemperature >= 22 -> {
                when {
                    hasRain -> OuterCategory.WINDBREAKER
                    hasStrongWind -> OuterCategory.LIGHT_JACKET
                    preferences.isColdSensitive() -> OuterCategory.LIGHT_CARDIGAN
                    else -> null
                }
            }

            feelsLikeTemperature >= 17 -> {
                when {
                    hasRain -> OuterCategory.WINDBREAKER
                    hasStrongWind -> OuterCategory.JACKET
                    else -> OuterCategory.CARDIGAN
                }
            }

            feelsLikeTemperature >= 12 -> {
                when {
                    hasRain -> OuterCategory.WINDBREAKER
                    hasStrongWind -> OuterCategory.COAT
                    else -> OuterCategory.LIGHT_JACKET
                }
            }

            feelsLikeTemperature >= 5 -> {
                when {
                    hasRain -> OuterCategory.WINDBREAKER
                    hasStrongWind || preferences.isColdSensitive() -> OuterCategory.PADDING
                    else -> OuterCategory.COAT
                }
            }

            else -> OuterCategory.PADDING
        }
    }

    // ===== 개인화 메시지 생성 =====

    /**
     * WeatherCondition Enum 사용한 개인화 메시지 생성
     */
    private fun generatePersonalizedMessage(
        weatherCondition: WeatherCondition,
        feelsLikeTemperature: Double,
        preferences: WeatherPreference,
        weatherData: WeatherData
    ): String {
        val personalityType = determinePersonalityType(preferences)

        return when (weatherCondition) {
            WeatherCondition.EXTREME_COLD -> generateExtremeColdMessage(personalityType, feelsLikeTemperature)
            WeatherCondition.COLD_SENSITIVE -> generateColdSensitiveMessage(personalityType, feelsLikeTemperature)
            WeatherCondition.PERFECT_WEATHER -> generatePerfectWeatherMessage(personalityType)
            WeatherCondition.HUMIDITY_RESISTANT -> generateHumidityMessage(
                personalityType,
                weatherData.humidity.toDouble()
            )

            WeatherCondition.HEAT_EXTREME -> generateHeatExtremeMessage(personalityType, feelsLikeTemperature)
        }
    }

    private fun generateExtremeColdMessage(personalityType: String, temperature: Double): String {
        return when (personalityType) {
            "추위민감형" -> "🥶 ${temperature.toInt()}°C! 평소 추위 많이 타시는데 오늘은 정말 춥네요. 레이어드 착용 필수입니다!"
            "더위민감형" -> "🥶 ${temperature.toInt()}°C. 더위 많이 타시지만 오늘은 두꺼운 옷 꼭 챙기세요!"
            "습도민감형" -> "🥶 ${temperature.toInt()}°C. 습도는 낮지만 기온이 너무 낮아 보온이 최우선입니다!"
            else -> "🥶 ${temperature.toInt()}°C. 매우 추운 날씨입니다. 완전 무장하고 나가세요!"
        }
    }

    private fun generateColdSensitiveMessage(personalityType: String, temperature: Double): String {
        return when (personalityType) {
            "추위민감형" -> "😰 ${temperature.toInt()}°C. 평소 추위 많이 타시는 편이라 한 겹 더 입는 걸 추천해요!"
            "더위민감형" -> "😊 ${temperature.toInt()}°C. 더위 많이 타시는 분께는 적당한 온도예요!"
            else -> "😐 ${temperature.toInt()}°C. 약간 쌀쌀한 날씨입니다."
        }
    }

    private fun generatePerfectWeatherMessage(personalityType: String): String {
        return when (personalityType) {
            "추위민감형" -> "😊 완벽한 날씨네요! 추위 많이 타시는 분도 편안하게 외출하실 수 있어요!"
            "더위민감형" -> "😌 완벽한 날씨입니다! 더위 많이 타시는 분께 딱 맞는 온도예요!"
            "습도민감형" -> "😊 습도도 적당하고 완벽한 날씨입니다!"
            else -> "😊 완벽한 날씨네요! 원하는 스타일로 자유롭게 입으세요!"
        }
    }

    private fun generateHumidityMessage(personalityType: String, humidity: Double): String {
        return when (personalityType) {
            "습도민감형" -> "😰 습도 ${humidity.toInt()}%. 습함을 특히 싫어하시는데 오늘은 통풍 잘 되는 옷 위주로 입으세요!"
            else -> "😐 습도 ${humidity.toInt()}%. 약간 눅눅한 날씨입니다."
        }
    }

    private fun generateHeatExtremeMessage(personalityType: String, temperature: Double): String {
        return when (personalityType) {
            "더위민감형" -> "🔥 ${temperature.toInt()}°C! 더위 많이 타시는데 오늘은 정말 더워요. 시원한 곳 위주로 이동하세요!"
            "추위민감형" -> "🔥 ${temperature.toInt()}°C. 평소 추위 많이 타시지만 오늘은 더위 조심하세요!"
            else -> "🔥 ${temperature.toInt()}°C. 매우 더운 날씨입니다. 시원하게 입고 수분 보충 잊지 마세요!"
        }
    }

    // ===== Helper Methods =====

    private fun determinePersonalityType(preferences: WeatherPreference): String {
        return when {
            preferences.isColdSensitive() -> "추위민감형"
            preferences.isHeatSensitive() -> "더위민감형"
            preferences.isHumiditySensitive() -> "습도민감형"
            else -> "일반형"
        }
    }

    // ===== 개인 맞춤 팁 생성 (기존 점수 기반) =====

    /**
     * 기존 WeatherScoreResult 기반 개인 맞춤 팁 생성
     */
    private fun generatePersonalTipFromScore(
        scoreResult: WeatherScoreResult,
        weatherCondition: WeatherCondition,
        preferences: WeatherPreference
    ): String? {
        val tips = mutableListOf<String>()

        // 점수 등급별 기본 팁
        when (scoreResult.grade) {
            WeatherGrade.TERRIBLE -> {
                tips.add("오늘은 외출을 최소화하는 것이 좋겠어요")
            }

            WeatherGrade.POOR -> {
                tips.add("외출 시 충분한 준비를 하고 나가세요")
            }

            WeatherGrade.FAIR -> {
                tips.add("적당한 날씨지만 개인 특성에 맞게 준비하세요")
            }

            WeatherGrade.GOOD -> {
                tips.add("좋은 날씨네요! 편안하게 외출하실 수 있어요")
            }

            WeatherGrade.PERFECT -> {
                tips.add("완벽한 날씨입니다! 원하는 활동을 마음껏 즐기세요")
            }
        }

        // 개인 특성별 추가 팁
        val personalTraits = preferences.getPersonalityTraits()
        if (personalTraits.isNotEmpty()) {
            val primaryTrait = personalTraits.first()
            when {
                primaryTrait.contains("추위") && scoreResult.elementScores.temperature < 70 -> {
                    tips.add("평소 추위를 많이 타시니 보온에 신경쓰세요")
                }

                primaryTrait.contains("더위") && scoreResult.elementScores.temperature < 70 -> {
                    tips.add("더위를 많이 타시는 편이니 시원함을 유지하세요")
                }

                primaryTrait.contains("습함") && scoreResult.elementScores.humidity < 70 -> {
                    tips.add("습도에 민감하시니 통풍이 잘 되는 옷을 선택하세요")
                }
            }
        }

        return tips.firstOrNull()
    }
}