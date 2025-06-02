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
            TopCategory.SLEEVELESS -> listOf("나시", "끈나시", "민소매 티셔츠")
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
            BottomCategory.THERMAL_PANTS -> listOf("기모 바지", "겨울 팬츠", "내복 바지")
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
     * 소품 아이템 조회
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
        if (weatherData.uvIndex != null && weatherData.uvIndex!! >= 7) {
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
        if (preferences.humidityWeight >= 70) {
            adjustedItems.addAll(getHumidityFriendlyOptions(category))
        }

        return adjustedItems.take(4) // 최대 4개까지
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

        return adjustedItems.take(4)
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

    /**
     * 계절별 추천 아이템 (향후 확장용)
     */
    private fun getSeasonalRecommendations(temperature: Double): List<String> {
        return when {
            temperature <= 5 -> listOf("겨울 필수템", "보온 우선")
            temperature <= 15 -> listOf("환절기 아이템", "레이어드")
            temperature <= 25 -> listOf("봄가을 추천", "가벼운 소재")
            else -> listOf("여름 필수", "시원한 소재")
        }
    }

    /**
     * 스타일별 아이템 추천 (향후 확장용)
     */
    private fun getStyleRecommendations(
        category: TopCategory,
        preferences: WeatherPreference
    ): List<String> {
        // 향후 사용자 스타일 선호도가 추가되면 활용
        return when (category) {
            TopCategory.T_SHIRT -> listOf("캐주얼", "베이직")
            TopCategory.SWEATER -> listOf("클래식", "모던")
            TopCategory.HOODIE -> listOf("스포티", "스트릿")
            else -> listOf("베이직")
        }
    }
}