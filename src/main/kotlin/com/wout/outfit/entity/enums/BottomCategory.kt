package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : BottomCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 하의 카테고리 열거형 (정밀도 개선 + 공통 인터페이스)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       Double 정밀도 유지 + TemperatureAppropriate 인터페이스 적용
 */
enum class BottomCategory(
    override val displayName: String,
    override val tempRange: ClosedFloatingPointRange<Double>
) : TemperatureAppropriate {

    THERMAL_PANTS("기모 바지", -10.0..10.0),
    THICK_PANTS("두꺼운 바지", 8.0..15.0),
    JEANS("청바지", 12.0..22.0),
    LIGHT_PANTS("얇은 바지", 20.0..26.0),
    SHORTS("반바지", 24.0..40.0);

    companion object {
        /**
         * 온도에 가장 적합한 카테고리 반환 (겹치는 구간에서는 우선순위 적용)
         */
        fun findBestMatch(temperature: Double): BottomCategory? {
            val exactMatches = BottomCategory.entries.filter { it.isAppropriateFor(temperature) }

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