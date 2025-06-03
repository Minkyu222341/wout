package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : OuterCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 외투 카테고리 열거형 (온도 범위 겹침 해결 + 정밀도 개선)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       온도 범위 겹침 해결 + Double 정밀도 + 우선순위 로직
 */
enum class OuterCategory(
    override val displayName: String,
    override val tempRange: ClosedFloatingPointRange<Double>,
    val priority: Int  // 겹치는 구간에서의 우선순위 (낮을수록 우선)
) : TemperatureAppropriate {

    PADDING("패딩", -10.0..5.0, 1),           // 극한 추위 최우선
    COAT("코트", 3.0..10.0, 2),               // 겨울 정장
    JACKET("자켓", 8.0..15.0, 3),             // 봄/가을 기본
    LIGHT_JACKET("얇은 자켓", 12.0..18.0, 4), // 선선한 날씨
    CARDIGAN("가디건", 15.0..20.0, 5),        // 실내외 온도차
    LIGHT_CARDIGAN("얇은 가디건", 18.0..22.0, 6), // 약간 쌀쌀
    WINDBREAKER("바람막이", 10.0..25.0, 7);   // 바람 대응 (특수 조건)

    companion object {
        /**
         * 온도에 가장 적합한 카테고리 반환 (우선순위 기반)
         */
        fun findBestMatch(temperature: Double): OuterCategory? {
            val exactMatches = OuterCategory.entries.filter { it.isAppropriateFor(temperature) }

            return when {
                exactMatches.isEmpty() -> null
                exactMatches.size == 1 -> exactMatches.first()
                else -> {
                    // 겹치는 구간에서는 우선순위가 높은(숫자가 낮은) 카테고리 선택
                    exactMatches.minByOrNull { it.priority }
                }
            }
        }

        /**
         * 바람 조건을 고려한 카테고리 선택
         */
        fun findBestMatchWithWind(temperature: Double, windSpeed: Double): OuterCategory? {
            return if (windSpeed >= 5.0 && WINDBREAKER.isAppropriateFor(temperature)) {
                WINDBREAKER  // 강한 바람 시 바람막이 우선
            } else {
                findBestMatch(temperature)
            }
        }
    }
}