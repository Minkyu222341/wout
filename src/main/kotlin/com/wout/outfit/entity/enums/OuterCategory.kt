package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : OuterCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 외투 카테고리 열거형
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
enum class OuterCategory(val displayName: String, val tempRange: IntRange) {
    LIGHT_CARDIGAN("얇은 가디건", 15..22),
    CARDIGAN("가디건", 12..20),
    LIGHT_JACKET("얇은 자켓", 10..18),
    JACKET("자켓", 5..15),
    COAT("코트", 0..12),
    PADDING("패딩", -10..8),
    WINDBREAKER("바람막이", 8..20);

    /**
     * 해당 온도에 적합한 카테고리인지 확인
     */
    fun isAppropriateFor(temperature: Double): Boolean {
        return temperature.toInt() in tempRange
    }
}