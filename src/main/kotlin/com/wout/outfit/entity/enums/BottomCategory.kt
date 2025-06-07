package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : BottomCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 하의 카테고리 열거형 (단순화)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       불필요한 메서드 제거, displayName만 유지
 */
enum class BottomCategory(val displayName: String) {
    THERMAL_PANTS("기모 바지"),
    THICK_PANTS("두꺼운 바지"),
    JEANS("청바지"),
    LIGHT_PANTS("얇은 바지"),
    SHORTS("반바지")
}