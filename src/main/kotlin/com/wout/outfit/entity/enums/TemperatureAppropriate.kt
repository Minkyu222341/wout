package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : TemperatureAppropriate
 * author         : MinKyu Park
 * date           : 2025-06-03
 * description    : 온도 적합성 판단을 위한 공통 인터페이스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-03        MinKyu Park       최초 생성 (코드 리뷰 반영)
 */
interface TemperatureAppropriate {
    val displayName: String
    val tempRange: ClosedFloatingPointRange<Double>

    /**
     * 해당 온도에 적합한 카테고리인지 확인 (Double 정밀도 유지)
     */
    fun isAppropriateFor(temperature: Double): Boolean {
        return temperature in tempRange
    }
}