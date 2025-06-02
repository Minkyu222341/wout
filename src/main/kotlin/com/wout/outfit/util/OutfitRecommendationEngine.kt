package com.wout.outfit.util

import com.wout.member.entity.WeatherPreference
import com.wout.outfit.entity.enums.BottomCategory
import com.wout.outfit.entity.enums.OuterCategory
import com.wout.outfit.entity.enums.TopCategory
import com.wout.outfit.service.CategoryRecommendation
import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.outfit.util
 * fileName       : OutfitRecommendationEngine
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 알고리즘 엔진 (핵심 로직)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Component
class OutfitRecommendationEngine {

    companion object {
        // 온도 구간별 기본 추천 매핑
        private val TEMPERATURE_CATEGORY_MAP = mapOf(
            (-50.0..-10.0) to Triple(TopCategory.THICK_SWEATER, BottomCategory.THERMAL_PANTS, OuterCategory.PADDING),
            (-10.0..0.0) to Triple(TopCategory.THICK_SWEATER, BottomCategory.THERMAL_PANTS, OuterCategory.COAT),
            (0.0..5.0) to Triple(TopCategory.HOODIE_THICK, BottomCategory.THICK_PANTS, OuterCategory.COAT),
            (5.0..10.0) to Triple(TopCategory.SWEATER, BottomCategory.THICK_PANTS, OuterCategory.JACKET),
            (10.0..15.0) to Triple(TopCategory.HOODIE, BottomCategory.JEANS, OuterCategory.LIGHT_JACKET),
            (15.0..20.0) to Triple(TopCategory.LONG_SLEEVE, BottomCategory.JEANS, OuterCategory.CARDIGAN),
            (20.0..25.0) to Triple(TopCategory.T_SHIRT, BottomCategory.JEANS, null),
            (25.0..30.0) to Triple(TopCategory.T_SHIRT, BottomCategory.LIGHT_PANTS, null),
            (30.0..60.0) to Triple(TopCategory.SLEEVELESS, BottomCategory.SHORTS, null)
        )

        // 바람 보정 (풍속 5m/s 이상일 때)
        private const val WIND_ADJUSTMENT_THRESHOLD = 5.0
        private const val WIND_TEMPERATURE_PENALTY = 3.0

        // 습도 보정 (습도 80% 이상일 때)
        private const val HUMIDITY_ADJUSTMENT_THRESHOLD = 80.0
        private const val HUMIDITY_TEMPERATURE_PENALTY = 2.0
    }

    /**
     * 최적 카테고리 결정 (메인 알고리즘)
     */
    fun determineOptimalCategories(
        feelsLikeTemperature: Double,
        weatherData: WeatherData,
        weatherPreference: WeatherPreference
    ): CategoryRecommendation {
        // 1) 환경 보정된 체감온도 계산
        val adjustedTemperature = calculateEnvironmentAdjustedTemperature(
            feelsLikeTemperature, weatherData, weatherPreference
        )

        // 2) 기본 카테고리 매핑
        val baseCategories = getBaseCategoriesByTemperature(adjustedTemperature)

        // 3) 개인 민감도 기반 조정
        val personalizedCategories = applyPersonalSensitivityAdjustment(
            baseCategories, weatherPreference, adjustedTemperature
        )

        // 4) 특수 날씨 조건 보정
        val finalCategories = applySpecialWeatherAdjustment(
            personalizedCategories, weatherData, weatherPreference
        )

        return finalCategories
    }

    /**
     * 환경 요인 반영한 체감온도 조정
     */
    private fun calculateEnvironmentAdjustedTemperature(
        feelsLikeTemperature: Double,
        weatherData: WeatherData,
        preference: WeatherPreference
    ): Double {
        var adjustedTemp = feelsLikeTemperature

        // 바람 보정
        if (weatherData.windSpeed >= WIND_ADJUSTMENT_THRESHOLD) {
            val windSensitivity = preference.windWeight / 50.0  // 0.2 ~ 2.0 범위
            adjustedTemp -= WIND_TEMPERATURE_PENALTY * windSensitivity
        }

        // 습도 보정
        if (weatherData.humidity >= HUMIDITY_ADJUSTMENT_THRESHOLD) {
            val humiditySensitivity = preference.humidityWeight / 50.0
            adjustedTemp += HUMIDITY_TEMPERATURE_PENALTY * humiditySensitivity
        }

        // 자외선 보정 (25도 이상에서만)
        if (feelsLikeTemperature >= 25.0 && weatherData.uvIndex != null && weatherData.uvIndex!! >= 8) {
            val uvSensitivity = preference.uvWeight / 50.0
            adjustedTemp += 2.0 * uvSensitivity  // 자외선 차단을 위해 더 덮는 옷 필요
        }

        return adjustedTemp
    }

    /**
     * 온도 기준 기본 카테고리 매핑
     */
    private fun getBaseCategoriesByTemperature(temperature: Double): CategoryRecommendation {
        val categories = TEMPERATURE_CATEGORY_MAP.entries
            .firstOrNull { temperature in it.key }
            ?.value ?: Triple(TopCategory.T_SHIRT, BottomCategory.JEANS, null)

        return CategoryRecommendation(
            topCategory = categories.first,
            bottomCategory = categories.second,
            outerCategory = categories.third
        )
    }

    /**
     * 개인 민감도 기반 조정
     */
    private fun applyPersonalSensitivityAdjustment(
        baseCategories: CategoryRecommendation,
        preference: WeatherPreference,
        temperature: Double
    ): CategoryRecommendation {
        var adjustedTop = baseCategories.topCategory
        var adjustedBottom = baseCategories.bottomCategory
        var adjustedOuter = baseCategories.outerCategory

        // 추위 민감도 적용
        if (preference.isColdSensitive()) {
            adjustedTop = upgradeTopForCold(adjustedTop)
            adjustedBottom = upgradeBottomForCold(adjustedBottom)

            // 외투 추가 또는 업그레이드
            if (temperature <= 20 && adjustedOuter == null) {
                adjustedOuter = OuterCategory.LIGHT_CARDIGAN
            } else if (adjustedOuter != null) {
                adjustedOuter = upgradeOuterForCold(adjustedOuter)
            }
        }

        // 더위 민감도 적용
        if (preference.isHeatSensitive()) {
            adjustedTop = downgradeTopForHeat(adjustedTop)
            adjustedBottom = downgradeBottomForHeat(adjustedBottom)

            // 외투 제거 또는 다운그레이드
            if (temperature >= 20) {
                adjustedOuter = null
            } else if (adjustedOuter != null) {
                adjustedOuter = downgradeOuterForHeat(adjustedOuter)
            }
        }

        return CategoryRecommendation(
            topCategory = adjustedTop,
            bottomCategory = adjustedBottom,
            outerCategory = adjustedOuter
        )
    }

    /**
     * 특수 날씨 조건 보정
     */
    private fun applySpecialWeatherAdjustment(
        categories: CategoryRecommendation,
        weatherData: WeatherData,
        preference: WeatherPreference
    ): CategoryRecommendation {
        var adjustedTop = categories.topCategory
        var adjustedBottom = categories.bottomCategory
        var adjustedOuter = categories.outerCategory

        // 비 오는 날 보정
        if (weatherData.rain1h != null && weatherData.rain1h!! > 0) {
            adjustedOuter = when (adjustedOuter) {
                null -> OuterCategory.WINDBREAKER
                OuterCategory.LIGHT_CARDIGAN, OuterCategory.CARDIGAN -> OuterCategory.WINDBREAKER
                else -> adjustedOuter
            }
        }

        // 강한 바람 보정
        if (weatherData.windSpeed >= 7.0) {
            if (adjustedOuter == null && weatherData.temperature <= 25) {
                adjustedOuter = OuterCategory.WINDBREAKER
            }
        }

        // 미세먼지 보정 (가리는 옷 선호)
        if (weatherData.pm25 != null && weatherData.pm25!! >= 75 && preference.airQualityWeight >= 70) {
            // 긴팔 선호
            if (adjustedTop in listOf(TopCategory.T_SHIRT, TopCategory.SLEEVELESS)) {
                adjustedTop = TopCategory.LONG_SLEEVE
            }
        }

        return CategoryRecommendation(
            topCategory = adjustedTop,
            bottomCategory = adjustedBottom,
            outerCategory = adjustedOuter
        )
    }

    // ===== 카테고리 업그레이드/다운그레이드 헬퍼 메서드들 =====

    private fun upgradeTopForCold(current: TopCategory): TopCategory {
        return when (current) {
            TopCategory.SLEEVELESS -> TopCategory.T_SHIRT
            TopCategory.T_SHIRT -> TopCategory.LONG_SLEEVE
            TopCategory.LINEN_SHIRT -> TopCategory.LONG_SLEEVE
            TopCategory.LONG_SLEEVE -> TopCategory.LIGHT_SWEATER
            TopCategory.LIGHT_SWEATER -> TopCategory.SWEATER
            TopCategory.SWEATER -> TopCategory.HOODIE_THICK
            TopCategory.HOODIE -> TopCategory.HOODIE_THICK
            TopCategory.HOODIE_THICK -> TopCategory.THICK_SWEATER
            TopCategory.THICK_SWEATER -> current  // 더 이상 업그레이드 불가
        }
    }

    private fun downgradeTopForHeat(current: TopCategory): TopCategory {
        return when (current) {
            TopCategory.THICK_SWEATER -> TopCategory.SWEATER
            TopCategory.HOODIE_THICK -> TopCategory.HOODIE
            TopCategory.SWEATER -> TopCategory.LIGHT_SWEATER
            TopCategory.HOODIE -> TopCategory.LONG_SLEEVE
            TopCategory.LIGHT_SWEATER -> TopCategory.LONG_SLEEVE
            TopCategory.LONG_SLEEVE -> TopCategory.T_SHIRT
            TopCategory.T_SHIRT -> TopCategory.SLEEVELESS
            TopCategory.LINEN_SHIRT -> TopCategory.SLEEVELESS
            TopCategory.SLEEVELESS -> current  // 더 이상 다운그레이드 불가
        }
    }

    private fun upgradeBottomForCold(current: BottomCategory): BottomCategory {
        return when (current) {
            BottomCategory.SHORTS -> BottomCategory.LIGHT_PANTS
            BottomCategory.LIGHT_PANTS -> BottomCategory.JEANS
            BottomCategory.JEANS -> BottomCategory.THICK_PANTS
            BottomCategory.THICK_PANTS -> BottomCategory.THERMAL_PANTS
            BottomCategory.THERMAL_PANTS -> current
        }
    }

    private fun downgradeBottomForHeat(current: BottomCategory): BottomCategory {
        return when (current) {
            BottomCategory.THERMAL_PANTS -> BottomCategory.THICK_PANTS
            BottomCategory.THICK_PANTS -> BottomCategory.JEANS
            BottomCategory.JEANS -> BottomCategory.LIGHT_PANTS
            BottomCategory.LIGHT_PANTS -> BottomCategory.SHORTS
            BottomCategory.SHORTS -> current
        }
    }

    private fun upgradeOuterForCold(current: OuterCategory): OuterCategory {
        return when (current) {
            OuterCategory.LIGHT_CARDIGAN -> OuterCategory.CARDIGAN
            OuterCategory.CARDIGAN -> OuterCategory.LIGHT_JACKET
            OuterCategory.LIGHT_JACKET -> OuterCategory.JACKET
            OuterCategory.JACKET -> OuterCategory.COAT
            OuterCategory.COAT -> OuterCategory.PADDING
            OuterCategory.WINDBREAKER -> OuterCategory.LIGHT_JACKET
            OuterCategory.PADDING -> current
        }
    }

    private fun downgradeOuterForHeat(current: OuterCategory): OuterCategory? {
        return when (current) {
            OuterCategory.PADDING -> OuterCategory.COAT
            OuterCategory.COAT -> OuterCategory.JACKET
            OuterCategory.JACKET -> OuterCategory.LIGHT_JACKET
            OuterCategory.LIGHT_JACKET -> OuterCategory.CARDIGAN
            OuterCategory.CARDIGAN -> OuterCategory.LIGHT_CARDIGAN
            OuterCategory.LIGHT_CARDIGAN -> null  // 외투 제거
            OuterCategory.WINDBREAKER -> OuterCategory.LIGHT_CARDIGAN
        }
    }
}