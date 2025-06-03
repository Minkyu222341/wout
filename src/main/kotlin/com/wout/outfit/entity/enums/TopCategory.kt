package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : TopCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 상의 카테고리 열거형 (단순화)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       불필요한 메서드 제거, displayName만 유지
 */
enum class TopCategory(val displayName: String) {
    THICK_SWEATER("두꺼운 니트"),
    HOODIE_THICK("두꺼운 후드티"),
    SWEATER("니트"),
    HOODIE("후드티"),
    LIGHT_SWEATER("얇은 니트"),
    LONG_SLEEVE("긴팔"),
    T_SHIRT("반팔"),
    LINEN_SHIRT("린넨 셔츠"),
    SLEEVELESS("나시")
}