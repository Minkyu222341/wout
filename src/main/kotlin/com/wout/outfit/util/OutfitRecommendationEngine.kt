package com.wout.outfit.util

import com.wout.member.entity.WeatherPreference
import com.wout.outfit.dto.response.OutfitCategories
import com.wout.outfit.dto.response.OutfitCategoryInfo
import com.wout.outfit.dto.response.OutfitRecommendationResponse
import com.wout.outfit.entity.enums.BottomCategory
import com.wout.outfit.entity.enums.OuterCategory
import com.wout.outfit.entity.enums.TopCategory
import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.util
 * fileName       : OutfitRecommendationEngine
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 알고리즘 엔진 (전면 개선: 온도 기반 자동 카테고리 선택)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       전면 개선: findBestMatch() 기반 자동 카테고리 선택
 */
@Component
class OutfitRecommendationEngine(
    private val outfitItemDatabase: OutfitItemDatabase
) {

    companion object {
        // 온도 임계값 상수
        private const val EXTREME_COLD_THRESHOLD = 5.0
        private const val COLD_WEATHER_THRESHOLD = 10.0
        private const val COOL_WEATHER_THRESHOLD = 15.0
        private const val PERFECT_WEATHER_THRESHOLD = 20.0
        private const val WARM_WEATHER_THRESHOLD = 25.0

        // 날씨 조건 상수
        private const val HIGH_HUMIDITY_THRESHOLD = 70
        private const val STRONG_WIND_THRESHOLD = 5.0
        private const val UV_CAUTION_THRESHOLD = 6.0
        private const val UV_DANGER_THRESHOLD = 8.0
        private const val PERFECT_WEATHER_SCORE_THRESHOLD = 85

        // 날씨 조건 타입
        private const val WEATHER_EXTREME_COLD = "extreme_cold"
        private const val WEATHER_COLD_SENSITIVE = "cold_sensitive"
        private const val WEATHER_WINTER_DAILY = "winter_daily"
        private const val WEATHER_COOL = "cool_weather"
        private const val WEATHER_PERFECT_CASUAL = "perfect_casual"
        private const val WEATHER_PERFECT_SEMI = "perfect_semi"
        private const val WEATHER_WARM = "warm_weather"
        private const val WEATHER_HUMIDITY_RESISTANT = "humidity_resistant"
        private const val WEATHER_HEAT_EXTREME = "heat_extreme"
        private const val WEATHER_HEAT_SENSITIVE = "heat_sensitive"
    }

    /**
     * 🔧 전면 개선: 온도 기반 자동 카테고리 선택으로 다중 추천 생성
     */
    fun generateMultipleRecommendations(
        weatherData: WeatherData,
        preference: WeatherPreference,
        personalScore: Int?
    ): List<OutfitRecommendationResponse> {

        val feelsLike = preference.calculateFeelsLikeTemperature(
            weatherData.temperature,
            weatherData.windSpeed,
            weatherData.humidity.toDouble()
        )

        val recommendations = mutableListOf<OutfitRecommendationResponse>()

        when {
            feelsLike <= EXTREME_COLD_THRESHOLD -> {
                recommendations.add(createWeatherBasedRecommendation(
                    name = "한겨울 완전방한 스타일",
                    weatherCondition = WEATHER_EXTREME_COLD,
                    weatherData = weatherData,
                    preference = preference,
                    feelsLike = feelsLike,
                    personalScore = personalScore
                ))

                if (preference.isColdSensitive()) {
                    recommendations.add(createWeatherBasedRecommendation(
                        name = "추위 민감형 레이어드",
                        weatherCondition = WEATHER_COLD_SENSITIVE,
                        weatherData = weatherData,
                        preference = preference,
                        feelsLike = feelsLike,
                        personalScore = personalScore
                    ))
                }
            }
            feelsLike <= COLD_WEATHER_THRESHOLD -> {
                recommendations.add(createWeatherBasedRecommendation(
                    name = "겨울 데일리 스타일",
                    weatherCondition = WEATHER_WINTER_DAILY,
                    weatherData = weatherData,
                    preference = preference,
                    feelsLike = feelsLike
                ))
            }
            feelsLike <= COOL_WEATHER_THRESHOLD -> {
                recommendations.add(createWeatherBasedRecommendation(
                    name = "가을/봄 쾌적 스타일",
                    weatherCondition = WEATHER_COOL,
                    weatherData = weatherData,
                    preference = preference,
                    feelsLike = feelsLike
                ))
            }
            feelsLike <= PERFECT_WEATHER_THRESHOLD -> {
                recommendations.add(createWeatherBasedRecommendation(
                    name = "완벽한 날씨 캐주얼",
                    weatherCondition = WEATHER_PERFECT_CASUAL,
                    weatherData = weatherData,
                    preference = preference,
                    feelsLike = feelsLike,
                    personalScore = personalScore
                ))
                recommendations.add(createWeatherBasedRecommendation(
                    name = "완벽한 날씨 세미정장",
                    weatherCondition = WEATHER_PERFECT_SEMI,
                    weatherData = weatherData,
                    preference = preference,
                    feelsLike = feelsLike
                ))
            }
            feelsLike <= WARM_WEATHER_THRESHOLD -> {
                recommendations.add(createWeatherBasedRecommendation(
                    name = "초여름 시원 스타일",
                    weatherCondition = WEATHER_WARM,
                    weatherData = weatherData,
                    preference = preference,
                    feelsLike = feelsLike
                ))

                if (weatherData.humidity > HIGH_HUMIDITY_THRESHOLD && preference.isHumiditySensitive()) {
                    recommendations.add(createWeatherBasedRecommendation(
                        name = "습도 민감형 드라이 스타일",
                        weatherCondition = WEATHER_HUMIDITY_RESISTANT,
                        weatherData = weatherData,
                        preference = preference,
                        feelsLike = feelsLike
                    ))
                }
            }
            else -> {
                recommendations.add(createWeatherBasedRecommendation(
                    name = "한여름 극시원 스타일",
                    weatherCondition = WEATHER_HEAT_EXTREME,
                    weatherData = weatherData,
                    preference = preference,
                    feelsLike = feelsLike
                ))

                if (preference.isHeatSensitive()) {
                    recommendations.add(createWeatherBasedRecommendation(
                        name = "더위 민감형 극한 쿨링",
                        weatherCondition = WEATHER_HEAT_SENSITIVE,
                        weatherData = weatherData,
                        preference = preference,
                        feelsLike = feelsLike
                    ))
                }
            }
        }

        return recommendations
    }

    /**
     * 🆕 통합된 날씨 기반 추천 생성 메서드 (온도 기반 자동 카테고리 선택)
     */
    private fun createWeatherBasedRecommendation(
        name: String,
        weatherCondition: String,
        weatherData: WeatherData,
        preference: WeatherPreference,
        feelsLike: Double,
        personalScore: Int? = null
    ): OutfitRecommendationResponse {

        // 🎯 온도 기반 자동 카테고리 선택
        val optimalCategories = selectOptimalCategories(feelsLike, weatherData, preference)

        // 🎯 OutfitItemDatabase를 활용한 아이템 조회
        val topItems = getTopItemsForCondition(optimalCategories.first, weatherCondition, preference, feelsLike)
        val bottomItems = getBottomItemsForCondition(optimalCategories.second, weatherCondition, preference, feelsLike)
        val outerItems = getOuterItemsForCondition(optimalCategories.third, weatherCondition, weatherData, preference, feelsLike)
        val accessoryItems = outfitItemDatabase.getAccessoryItemsForWeather(weatherCondition, weatherData, preference, feelsLike)

        // 🎯 날씨 조건별 맞춤 메시지 생성
        val reasonData = generateReasonData(weatherCondition, feelsLike, weatherData)
        val personalTip = generatePersonalTip(preference, weatherCondition, personalScore)

        return OutfitRecommendationResponse(
            id = generateRecommendationId(),
            memberId = preference.memberId,
            name = name,
            categories = OutfitCategories(
                top = OutfitCategoryInfo(
                    items = topItems,
                    reason = reasonData.topReason
                ),
                bottom = OutfitCategoryInfo(
                    items = bottomItems,
                    reason = reasonData.bottomReason
                ),
                outer = OutfitCategoryInfo(
                    items = outerItems,
                    reason = reasonData.outerReason
                ),
                accessories = OutfitCategoryInfo(
                    items = accessoryItems,
                    reason = reasonData.accessoryReason
                )
            ),
            recommendationReason = reasonData.mainReason,
            personalTip = personalTip,
            summary = generateSummary(topItems, bottomItems, outerItems),
            createdAt = LocalDateTime.now(),
            topCategory = optimalCategories.first,
            bottomCategory = optimalCategories.second,
            outerCategory = optimalCategories.third
        )
    }

    /**
     * 🎯 온도와 날씨 조건 기반 최적 카테고리 선택
     */
    private fun selectOptimalCategories(
        feelsLike: Double,
        weatherData: WeatherData,
        preference: WeatherPreference
    ): Triple<TopCategory, BottomCategory, OuterCategory?> {

        // 기본 온도 기반 카테고리 선택
        val baseTopCategory = TopCategory.findBestMatch(feelsLike) ?: TopCategory.LONG_SLEEVE
        val baseBottomCategory = BottomCategory.findBestMatch(feelsLike) ?: BottomCategory.JEANS
        val baseOuterCategory = OuterCategory.findBestMatchWithWind(feelsLike, weatherData.windSpeed)

        // 개인 민감도에 따른 조정
        val adjustedTop = adjustTopCategoryForSensitivity(baseTopCategory, feelsLike, preference)
        val adjustedBottom = adjustBottomCategoryForSensitivity(baseBottomCategory, feelsLike, preference)
        val adjustedOuter = adjustOuterCategoryForSensitivity(baseOuterCategory, feelsLike, preference)

        return Triple(adjustedTop, adjustedBottom, adjustedOuter)
    }

    /**
     * 🔧 개인 민감도에 따른 상의 카테고리 조정
     */
    private fun adjustTopCategoryForSensitivity(
        baseCategory: TopCategory,
        feelsLike: Double,
        preference: WeatherPreference
    ): TopCategory {
        return when {
            preference.isColdSensitive() && feelsLike <= PERFECT_WEATHER_THRESHOLD -> {
                // 추위 민감형: 더 따뜻한 카테고리로 업그레이드
                when (baseCategory) {
                    TopCategory.T_SHIRT -> TopCategory.LONG_SLEEVE
                    TopCategory.LONG_SLEEVE -> TopCategory.LIGHT_SWEATER
                    TopCategory.LIGHT_SWEATER -> TopCategory.SWEATER
                    TopCategory.HOODIE -> TopCategory.HOODIE_THICK
                    else -> baseCategory
                }
            }
            preference.isHeatSensitive() && feelsLike >= COOL_WEATHER_THRESHOLD -> {
                // 더위 민감형: 더 시원한 카테고리로 다운그레이드
                when (baseCategory) {
                    TopCategory.LONG_SLEEVE -> TopCategory.T_SHIRT
                    TopCategory.T_SHIRT -> TopCategory.SLEEVELESS
                    TopCategory.LIGHT_SWEATER -> TopCategory.LONG_SLEEVE
                    TopCategory.SWEATER -> TopCategory.LIGHT_SWEATER
                    else -> baseCategory
                }
            }
            else -> baseCategory
        }
    }

    /**
     * 🔧 개인 민감도에 따른 하의 카테고리 조정
     */
    private fun adjustBottomCategoryForSensitivity(
        baseCategory: BottomCategory,
        feelsLike: Double,
        preference: WeatherPreference
    ): BottomCategory {
        return when {
            preference.isColdSensitive() && feelsLike <= PERFECT_WEATHER_THRESHOLD -> {
                when (baseCategory) {
                    BottomCategory.SHORTS -> BottomCategory.LIGHT_PANTS
                    BottomCategory.LIGHT_PANTS -> BottomCategory.JEANS
                    BottomCategory.JEANS -> BottomCategory.THICK_PANTS
                    else -> baseCategory
                }
            }
            preference.isHeatSensitive() && feelsLike >= COOL_WEATHER_THRESHOLD -> {
                when (baseCategory) {
                    BottomCategory.THICK_PANTS -> BottomCategory.JEANS
                    BottomCategory.JEANS -> BottomCategory.LIGHT_PANTS
                    BottomCategory.LIGHT_PANTS -> BottomCategory.SHORTS
                    else -> baseCategory
                }
            }
            else -> baseCategory
        }
    }

    /**
     * 🔧 개인 민감도에 따른 외투 카테고리 조정
     */
    private fun adjustOuterCategoryForSensitivity(
        baseCategory: OuterCategory?,
        feelsLike: Double,
        preference: WeatherPreference
    ): OuterCategory? {
        return when {
            preference.isColdSensitive() && feelsLike <= WARM_WEATHER_THRESHOLD -> {
                // 추위 민감형: 외투 추가 또는 업그레이드
                baseCategory ?: OuterCategory.LIGHT_CARDIGAN
            }
            preference.isHeatSensitive() && feelsLike >= PERFECT_WEATHER_THRESHOLD -> {
                // 더위 민감형: 외투 제거 또는 다운그레이드
                null
            }
            else -> baseCategory
        }
    }

    /**
     * 🎯 날씨 조건별 상의 아이템 조회
     */
    private fun getTopItemsForCondition(
        category: TopCategory,
        weatherCondition: String,
        preference: WeatherPreference,
        feelsLike: Double
    ): List<String> {
        return if (outfitItemDatabase::class.java.methods.any { it.name == "getTopItemsForWeather" }) {
            // OutfitItemDatabase에 특화 메서드가 있으면 사용
            outfitItemDatabase.getTopItemsForWeather(category, weatherCondition, preference, feelsLike)
        } else {
            // 없으면 기본 메서드 사용
            outfitItemDatabase.getTopItems(category, feelsLike, preference)
        }
    }

    /**
     * 🎯 날씨 조건별 하의 아이템 조회
     */
    private fun getBottomItemsForCondition(
        category: BottomCategory,
        weatherCondition: String,
        preference: WeatherPreference,
        feelsLike: Double
    ): List<String> {
        return if (outfitItemDatabase::class.java.methods.any { it.name == "getBottomItemsForWeather" }) {
            outfitItemDatabase.getBottomItemsForWeather(category, weatherCondition, preference, feelsLike)
        } else {
            outfitItemDatabase.getBottomItems(category, feelsLike, preference)
        }
    }

    /**
     * 🎯 날씨 조건별 외투 아이템 조회
     */
    private fun getOuterItemsForCondition(
        category: OuterCategory?,
        weatherCondition: String,
        weatherData: WeatherData,
        preference: WeatherPreference,
        feelsLike: Double
    ): List<String> {
        return if (category == null) {
            emptyList()
        } else if (outfitItemDatabase::class.java.methods.any { it.name == "getOuterItemsForWeather" }) {
            outfitItemDatabase.getOuterItemsForWeather(category, weatherCondition, weatherData, preference, feelsLike)
        } else {
            outfitItemDatabase.getOuterItems(category, feelsLike, preference)
        }
    }

    /**
     * 🎯 날씨 조건별 추천 이유 데이터 생성
     */
    private fun generateReasonData(
        weatherCondition: String,
        feelsLike: Double,
        weatherData: WeatherData
    ): ReasonData {
        return when (weatherCondition) {
            WEATHER_EXTREME_COLD -> ReasonData(
                topReason = "극한 추위 대비 두꺼운 상의 필수",
                bottomReason = "보온성 최우선, 두꺼운 소재 필수",
                outerReason = if (weatherData.windSpeed >= STRONG_WIND_THRESHOLD) "강한 바람으로 인해 방풍 기능 필수" else "바깥 활동 시 필수 아우터",
                accessoryReason = "노출 부위 최소화 필요",
                mainReason = "극한 추위(체감 ${feelsLike.toInt()}°C)로 인해 최대한 보온에 집중한 스타일링이 필요합니다"
            )
            WEATHER_COLD_SENSITIVE -> ReasonData(
                topReason = "추위 많이 타시니 레이어드 필수",
                bottomReason = "속옷부터 보온에 신경써야 해요",
                outerReason = "최고 보온성 아우터",
                accessoryReason = "소품으로 보온 효과 극대화",
                mainReason = "추위 민감도가 높아 레이어드와 보온 소품을 적극 활용한 스타일링"
            )
            WEATHER_PERFECT_CASUAL -> ReasonData(
                topReason = "가장 쾌적한 온도, 얇은 긴팔이 최적",
                bottomReason = "가벼운 소재의 긴바지로 적당한 보온성",
                outerReason = "선택사항 (실내외 온도차 대비)",
                accessoryReason = generateAccessoryReason(weatherData),
                mainReason = "이상적인 날씨(체감 ${feelsLike.toInt()}°C)로 가장 편안하고 쾌적한 옷차림을 즐길 수 있어요"
            )
            WEATHER_HUMIDITY_RESISTANT -> ReasonData(
                topReason = "습함을 싫어하시니 빠른 건조 소재로",
                bottomReason = "습기 배출 잘 되는 소재 중심",
                outerReason = "최소한의 겉옷, 통풍 중시",
                accessoryReason = "습도 대응 전용 아이템",
                mainReason = "높은 습도(${weatherData.humidity}%)에 대응하여 빠른 건조와 통풍을 우선시한 스타일링"
            )
            WEATHER_HEAT_EXTREME -> ReasonData(
                topReason = "최대한 시원하게, 소매 최소화",
                bottomReason = "다리 시원함 우선, 짧은 길이",
                outerReason = "자외선 차단용으로만 필요시",
                accessoryReason = generateHeatAccessoryReason(weatherData),
                mainReason = "극한 더위(체감 ${feelsLike.toInt()}°C)에 대응하여 체온 조절과 자외선 차단에 집중한 스타일링"
            )
            else -> ReasonData(
                topReason = "체감온도에 적합한 상의 선택",
                bottomReason = "편안하고 활동하기 좋은 하의",
                outerReason = "날씨 조건에 맞는 외투",
                accessoryReason = "날씨에 맞는 소품 추천",
                mainReason = "체감온도 ${feelsLike.toInt()}°C에 적합한 기본 추천"
            )
        }
    }

    /**
     * 🎯 개인화 팁 생성
     */
    private fun generatePersonalTip(
        preference: WeatherPreference,
        weatherCondition: String,
        personalScore: Int? = null
    ): String? {
        val primaryTrait = preference.getPrimaryTrait()

        return when (weatherCondition) {
            WEATHER_EXTREME_COLD -> when {
                preference.isColdSensitive() -> "평소 추위를 많이 타시니 한 겹 더 입는 걸 추천해요!"
                else -> "극한 추위이니 체온 유지에 신경써주세요"
            }
            WEATHER_HEAT_EXTREME -> when {
                preference.isHeatSensitive() -> "더위 많이 타시는 편이라 에어컨 있는 곳 이동 시 얇은 겉옷 챙기세요!"
                else -> "자외선이 강하니 실내 활동을 권장해요"
            }
            WEATHER_PERFECT_CASUAL -> {
                if (personalScore != null && personalScore >= PERFECT_WEATHER_SCORE_THRESHOLD) {
                    "완벽한 날씨네요! 원하는 스타일로 자유롭게 입으세요 😊"
                } else primaryTrait
            }
            WEATHER_HUMIDITY_RESISTANT -> "습한 날씨를 싫어하시니 실내 위주로 활동하는 게 좋겠어요!"
            else -> primaryTrait
        }
    }

    /**
     * 🔧 자외선 조건 기반 소품 이유 생성
     */
    private fun generateAccessoryReason(weatherData: WeatherData): String {
        return weatherData.uvIndex?.let { uvIndex ->
            if (uvIndex >= UV_CAUTION_THRESHOLD) "자외선 차단 필수" else "햇빛 차단용"
        } ?: "햇빛 차단용"
    }

    /**
     * 🔧 더위 조건 기반 소품 이유 생성
     */
    private fun generateHeatAccessoryReason(weatherData: WeatherData): String {
        return weatherData.uvIndex?.let { uvIndex ->
            if (uvIndex >= UV_DANGER_THRESHOLD) "극강 자외선 대응 필수템" else "강한 더위 대응 필수템"
        } ?: "강한 더위 대응 필수템"
    }

    /**
     * 🎯 요약 메시지 생성
     */
    private fun generateSummary(
        topItems: List<String>,
        bottomItems: List<String>,
        outerItems: List<String>
    ): String {
        val topItem = topItems.firstOrNull() ?: "상의"
        val bottomItem = bottomItems.firstOrNull() ?: "하의"
        val outerItem = outerItems.firstOrNull()

        return if (outerItem != null) {
            "$topItem + $bottomItem + $outerItem"
        } else {
            "$topItem + $bottomItem"
        }
    }

    /**
     * 🎯 추천 ID 생성
     */
    private fun generateRecommendationId(): String {
        return "rec_${System.currentTimeMillis()}"
    }

    /**
     * 🎯 추천 이유 데이터 클래스
     */
    private data class ReasonData(
        val topReason: String,
        val bottomReason: String,
        val outerReason: String,
        val accessoryReason: String,
        val mainReason: String
    )
}