package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : TopCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 상의 카테고리 열거형
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
enum class TopCategory(val displayName: String, val tempRange: IntRange) {
    SLEEVELESS("나시", 28..40),
    T_SHIRT("반팔", 22..30),
    LINEN_SHIRT("린넨 셔츠", 25..35),
    LONG_SLEEVE("긴팔", 15..25),
    LIGHT_SWEATER("얇은 니트", 12..22),
    SWEATER("니트", 8..18),
    HOODIE("후드티", 10..20),
    HOODIE_THICK("두꺼운 후드티", 5..15),
    THICK_SWEATER("두꺼운 니트", 0..12);

    /**
     * 해당 온도에 적합한 카테고리인지 확인
     */
    fun isAppropriateFor(temperature: Double): Boolean {
        return temperature.toInt() in tempRange
    }
}