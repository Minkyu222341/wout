package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : BottomCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 하의 카테고리 열거형
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
enum class BottomCategory(val displayName: String, val tempRange: IntRange) {
    SHORTS("반바지", 22..40),
    LIGHT_PANTS("얇은 바지", 18..28),
    JEANS("청바지", 10..25),
    THICK_PANTS("두꺼운 바지", 5..18),
    THERMAL_PANTS("기모 바지", 0..15);

    /**
     * 해당 온도에 적합한 카테고리인지 확인
     */
    fun isAppropriateFor(temperature: Double): Boolean {
        return temperature.toInt() in tempRange
    }
}