package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : TopCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 상의 카테고리 열거형 (정밀도 개선 + 공통 인터페이스)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       Double 정밀도 유지 + TemperatureAppropriate 인터페이스 적용
 */
enum class TopCategory(
    override val displayName: String,
    override val tempRange: ClosedFloatingPointRange<Double>
) : TemperatureAppropriate {

    THICK_SWEATER("두꺼운 니트", -10.0..8.0),
    HOODIE_THICK("두꺼운 후드티", 5.0..12.0),
    SWEATER("니트", 8.0..15.0),
    HOODIE("후드티", 10.0..18.0),
    LIGHT_SWEATER("얇은 니트", 15.0..20.0),
    LONG_SLEEVE("긴팔", 18.0..23.0),
    T_SHIRT("반팔", 22.0..28.0),
    LINEN_SHIRT("린넨 셔츠", 25.0..32.0),
    SLEEVELESS("나시", 28.0..40.0);

    companion object {
        /**
         * 온도에 가장 적합한 카테고리 반환 (겹치는 구간에서는 우선순위 적용)
         */
        fun findBestMatch(temperature: Double): TopCategory? {
            // 정확히 매칭되는 카테고리 우선 반환
            val exactMatches = TopCategory.entries.filter { it.isAppropriateFor(temperature) }

            return when {
                exactMatches.isEmpty() -> null
                exactMatches.size == 1 -> exactMatches.first()
                else -> {
                    // 겹치는 구간에서는 온도에 더 가까운 중앙값을 가진 카테고리 선택
                    exactMatches.minByOrNull {
                        kotlin.math.abs(temperature - (it.tempRange.start + it.tempRange.endInclusive) / 2.0)
                    }
                }
            }
        }
    }
}