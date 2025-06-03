package com.wout.outfit.util

import com.wout.member.entity.WeatherPreference
import com.wout.outfit.entity.enums.BottomCategory
import com.wout.outfit.entity.enums.OuterCategory
import com.wout.outfit.entity.enums.TopCategory
import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.outfit.util
 * fileName       : OutfitItemDatabase
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 아이템 데이터베이스 (카테고리별 실제 의류 아이템 관리)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       OutfitRecommendationEngine 연동 강화
 */
@Component
class OutfitItemDatabase {

    /**
     * 상의 아이템 조회
     */
    fun getTopItems(
        category: TopCategory,
        temperature: Double,
        preferences: WeatherPreference
    ): List<String> {
        val baseItems = when (category) {
            TopCategory.SLEEVELESS -> listOf("민소매", "끈나시", "민소매 티셔츠")
            TopCategory.T_SHIRT -> listOf("반팔 티셔츠", "면 티셔츠", "폴로 셔츠")
            TopCategory.LINEN_SHIRT -> listOf("린넨 셔츠", "시어서커 셔츠", "통풍 셔츠")
            TopCategory.LONG_SLEEVE -> listOf("긴팔 티셔츠", "면 긴팔", "헨리넥")
            TopCategory.LIGHT_SWEATER -> listOf("얇은 니트", "가벼운 스웨터", "면 가디건")
            TopCategory.SWEATER -> listOf("니트", "스웨터", "울 니트")
            TopCategory.HOODIE -> listOf("후드티", "맨투맨", "기모 후드")
            TopCategory.HOODIE_THICK -> listOf("두꺼운 후드티", "기모 맨투맨", "플리스")
            TopCategory.THICK_SWEATER -> listOf("두꺼운 니트", "울 스웨터", "목폴라")
        }

        return applyPersonalPreferences(baseItems, category, preferences, temperature)
    }

    /**
     * 하의 아이템 조회
     */
    fun getBottomItems(
        category: BottomCategory,
        temperature: Double,
        preferences: WeatherPreference
    ): List<String> {
        val baseItems = when (category) {
            BottomCategory.SHORTS -> listOf("반바지", "숏 팬츠", "린넨 반바지")
            BottomCategory.LIGHT_PANTS -> listOf("얇은 면바지", "린넨 바지", "치노 팬츠")
            BottomCategory.JEANS -> listOf("청바지", "데님 팬츠", "스키니 진")
            BottomCategory.THICK_PANTS -> listOf("두꺼운 면바지", "코듀로이 팬츠", "울 바지")
            BottomCategory.THERMAL_PANTS -> listOf("기모 바지", "겨울 팬츠", "기모 청바지")
        }

        return applyComfortOptions(baseItems, preferences, temperature)
    }

    /**
     * 외투 아이템 조회
     */
    fun getOuterItems(
        category: OuterCategory,
        temperature: Double,
        preferences: WeatherPreference
    ): List<String> {
        val baseItems = when (category) {
            OuterCategory.LIGHT_CARDIGAN -> listOf("얇은 가디건", "가벼운 니트")
            OuterCategory.CARDIGAN -> listOf("가디건", "니트 가디건", "버튼 가디건")
            OuterCategory.LIGHT_JACKET -> listOf("얇은 자켓", "봄 자켓", "가벼운 점퍼")
            OuterCategory.JACKET -> listOf("자켓", "재킷", "블레이저")
            OuterCategory.COAT -> listOf("코트", "트렌치코트", "울코트")
            OuterCategory.PADDING -> listOf("패딩", "다운 재킷", "겨울 점퍼")
            OuterCategory.WINDBREAKER -> listOf("바람막이", "윈드브레이커", "레인코트")
        }

        return addWeatherSpecificOptions(baseItems, category, preferences, temperature)
    }

    /**
     * 🆕 특정 날씨 상황에 맞는 상의 아이템 조회 (추천 엔진 연동용)
     */
    fun getTopItemsForWeather(
        category: TopCategory,
        weatherCondition: String,
        preferences: WeatherPreference,
        temperature: Double
    ): List<String> {
        val baseItems = getTopItems(category, temperature, preferences).toMutableList()

        return when (weatherCondition) {
            "extreme_cold" -> {
                // 극한 추위용 특화 아이템
                when (category) {
                    TopCategory.THICK_SWEATER -> listOf("두꺼운 니트", "목폴라", "기모 후드티")
                    TopCategory.HOODIE_THICK -> listOf("기모 후드티", "플리스", "두꺼운 맨투맨")
                    else -> baseItems
                }
            }
            "cold_sensitive" -> {
                // 추위 민감형용 레이어드 아이템
                baseItems.map {
                    when {
                        it.contains("니트") -> "히트텍 + $it"
                        it.contains("후드") -> "목폴라 + $it"
                        else -> it
                    }
                }
            }
            "humidity_resistant" -> {
                // 습도 민감형용 속건 아이템
                listOf("속건 반팔", "메시 티셔츠", "린넨 셔츠")
            }
            "heat_extreme" -> {
                // 극한 더위용 쿨링 아이템
                listOf("민소매", "쿨링 반팔", "얇은 나시")
            }
            else -> baseItems
        }.take(3) // 추천용으로 3개까지만
    }

    /**
     * 🆕 특정 날씨 상황에 맞는 하의 아이템 조회 (추천 엔진 연동용)
     */
    fun getBottomItemsForWeather(
        category: BottomCategory,
        weatherCondition: String,
        preferences: WeatherPreference,
        temperature: Double
    ): List<String> {
        val baseItems = getBottomItems(category, temperature, preferences).toMutableList()

        return when (weatherCondition) {
            "extreme_cold" -> {
                when (category) {
                    BottomCategory.THERMAL_PANTS -> listOf("기모 청바지", "패딩 바지", "털안감 슬랙스")
                    else -> baseItems
                }
            }
            "cold_sensitive" -> {
                baseItems.map { "히트텍 레깅스 + $it" }
            }
            "humidity_resistant" -> {
                listOf("속건 7부 팬츠", "린넨 바지", "쿨맥스 레깅스")
            }
            "heat_extreme" -> {
                listOf("반바지", "쿨링 쇼츠", "짧은 원피스")
            }
            else -> baseItems
        }.take(3)
    }

    /**
     * 🆕 특정 날씨 상황에 맞는 외투 아이템 조회 (추천 엔진 연동용)
     */
    fun getOuterItemsForWeather(
        category: OuterCategory?,
        weatherCondition: String,
        weatherData: WeatherData,
        preferences: WeatherPreference,
        temperature: Double
    ): List<String> {
        if (category == null) return emptyList()

        val baseItems = getOuterItems(category, temperature, preferences).toMutableList()

        return when (weatherCondition) {
            "extreme_cold" -> {
                when (category) {
                    OuterCategory.PADDING -> {
                        if (weatherData.windSpeed >= 5.0) {
                            listOf("롱패딩", "방풍 패딩", "두꺼운 코트")
                        } else {
                            listOf("롱패딩", "무스탕", "두꺼운 코트")
                        }
                    }
                    else -> baseItems
                }
            }
            "cold_sensitive" -> {
                when (category) {
                    OuterCategory.PADDING -> listOf("두꺼운 패딩", "퍼 코트", "구스다운")
                    else -> baseItems
                }
            }
            "humidity_resistant" -> {
                listOf("통풍 자켓")
            }
            "heat_extreme" -> {
                listOf("자외선 차단복")
            }
            else -> baseItems
        }.take(3)
    }

    /**
     * 🆕 특정 날씨 상황에 맞는 소품 아이템 조회 (추천 엔진 연동용)
     */
    fun getAccessoryItemsForWeather(
        weatherCondition: String,
        weatherData: WeatherData,
        preferences: WeatherPreference,
        temperature: Double
    ): List<String> {
        val accessories = mutableListOf<String>()

        when (weatherCondition) {
            "extreme_cold" -> {
                accessories.addAll(listOf("목도리", "장갑", "모자"))
                if (weatherData.windSpeed >= 5.0) {
                    accessories.add("방풍 마스크")
                } else {
                    accessories.add("마스크")
                }
            }
            "cold_sensitive" -> {
                accessories.addAll(listOf("털모자", "터치장갑", "목도리", "핫팩"))
            }
            "perfect_weather" -> {
                if (weatherData.uvIndex != null && weatherData.uvIndex!! >= 6.0) {
                    accessories.addAll(listOf("선글라스", "모자"))
                } else {
                    accessories.add("선글라스")
                }
            }
            "humidity_resistant" -> {
                accessories.addAll(listOf("메시 모자", "쿨타월"))
            }
            "heat_extreme" -> {
                accessories.addAll(listOf("넓은 모자", "선글라스"))
                if (weatherData.uvIndex != null && weatherData.uvIndex!! >= 8.0) {
                    accessories.addAll(listOf("쿨토시", "휴대용 선풍기", "자외선 차단 크림"))
                } else {
                    accessories.addAll(listOf("쿨토시", "휴대용 선풍기"))
                }
            }
            else -> {
                // 기본 소품 추천
                accessories.addAll(getAccessoryItems(temperature, weatherData, preferences))
            }
        }

        return accessories.distinct().take(4) // 최대 4개까지
    }

    /**
     * 소품 아이템 조회 (기본 메서드 유지)
     */
    fun getAccessoryItems(
        temperature: Double,
        weatherData: WeatherData,
        preferences: WeatherPreference
    ): List<String> {
        val accessories = mutableListOf<String>()

        // 온도 기반 소품
        when {
            temperature <= 5 -> accessories.addAll(listOf("목도리", "장갑", "모자", "귀마개"))
            temperature <= 15 -> accessories.addAll(listOf("가벼운 목도리", "얇은 장갑"))
            temperature >= 25 -> accessories.addAll(listOf("모자", "선글라스"))
        }

        // 자외선 보호 소품
        if (weatherData.uvIndex != null && weatherData.uvIndex!! >= 7.0) {
            accessories.addAll(listOf("챙 넓은 모자", "선글라스", "팔토시"))

            // 자외선 민감 사용자 추가 아이템
            if (preferences.uvWeight >= 70) {
                accessories.addAll(listOf("자외선 차단 스카프", "UV 차단 장갑"))
            }
        }

        // 미세먼지 보호
        if (weatherData.pm25 != null && weatherData.pm25!! >= 75) {
            accessories.add("마스크")

            // 공기질 민감 사용자 추가 아이템
            if (preferences.airQualityWeight >= 70) {
                accessories.addAll(listOf("KF94 마스크", "공기정화 목걸이"))
            }
        }

        // 비 대비
        if (weatherData.rain1h != null && weatherData.rain1h!! > 0) {
            accessories.addAll(listOf("우산", "방수 신발"))

            // 강한 비일 경우 추가 아이템
            if (weatherData.rain1h!! > 5.0) {
                accessories.addAll(listOf("장우산", "레인부츠", "방수 가방"))
            }
        }

        // 바람 대비
        if (weatherData.windSpeed >= 7.0) {
            accessories.addAll(listOf("바람막이 모자", "스카프"))
        }

        return accessories.distinct().take(5) // 최대 5개까지만
    }

    // ===== Private Helper Methods =====

    /**
     * 개인 선호도 반영 아이템 조정
     */
    private fun applyPersonalPreferences(
        baseItems: List<String>,
        category: TopCategory,
        preferences: WeatherPreference,
        temperature: Double
    ): List<String> {
        val adjustedItems = baseItems.toMutableList()

        // 추위 민감 사용자 추가 옵션
        if (preferences.isColdSensitive()) {
            when (category) {
                TopCategory.T_SHIRT -> adjustedItems.add("기모 반팔")
                TopCategory.LONG_SLEEVE -> adjustedItems.add("기모 긴팔")
                TopCategory.SWEATER -> adjustedItems.add("터틀넥 니트")
                TopCategory.HOODIE -> adjustedItems.add("안감 기모 후드")
                else -> {}
            }
        }

        // 더위 민감 사용자 추가 옵션
        if (preferences.isHeatSensitive()) {
            when (category) {
                TopCategory.T_SHIRT -> adjustedItems.addAll(listOf("메쉬 티셔츠", "쿨링 소재"))
                TopCategory.LONG_SLEEVE -> adjustedItems.add("얇은 긴팔")
                TopCategory.LIGHT_SWEATER -> adjustedItems.add("망사 니트")
                else -> {}
            }
        }

        // 습도 민감 사용자 추가 옵션
        if (preferences.isHumiditySensitive()) {
            adjustedItems.addAll(getHumidityFriendlyOptions(category))
        }

        return adjustedItems.distinct().take(4) // 최대 4개까지
    }

    /**
     * 편안함 옵션 추가
     */
    private fun applyComfortOptions(
        baseItems: List<String>,
        preferences: WeatherPreference,
        temperature: Double
    ): List<String> {
        val adjustedItems = baseItems.toMutableList()

        // 고민감도 사용자용 편안함 아이템
        if (preferences.isHighSensitivity()) {
            adjustedItems.addAll(listOf("편안한 핏", "스트레치 소재"))
        }

        // 추위 민감도 높을 때
        if (preferences.isColdSensitive() && temperature <= 15) {
            adjustedItems.addAll(listOf("기모 안감", "보온 소재"))
        }

        return adjustedItems.distinct().take(4)
    }

    /**
     * 날씨별 특수 옵션 추가
     */
    private fun addWeatherSpecificOptions(
        baseItems: List<String>,
        category: OuterCategory,
        preferences: WeatherPreference,
        temperature: Double
    ): List<String> {
        val adjustedItems = baseItems.toMutableList()

        // 바람막이 기능 강화
        if (category == OuterCategory.WINDBREAKER) {
            adjustedItems.addAll(listOf("방수 기능", "통기성 소재"))
        }

        // 패딩 세분화
        if (category == OuterCategory.PADDING) {
            when {
                temperature <= -10 -> adjustedItems.addAll(listOf("극한기 패딩", "구스다운"))
                temperature <= 0 -> adjustedItems.addAll(listOf("롱패딩", "방풍 패딩"))
                else -> adjustedItems.addAll(listOf("라이트 패딩", "숏패딩"))
            }
        }

        return adjustedItems.distinct().take(4)
    }

    /**
     * 습도 친화적 옵션 조회
     */
    private fun getHumidityFriendlyOptions(category: TopCategory): List<String> {
        return when (category) {
            TopCategory.T_SHIRT -> listOf("속건 티셔츠", "통풍 소재")
            TopCategory.LONG_SLEEVE -> listOf("속건 긴팔", "메쉬 소재")
            TopCategory.LIGHT_SWEATER -> listOf("통풍 니트", "린넨 혼방")
            else -> listOf("통풍 소재")
        }
    }
}