package com.wout.outfit.entity.enums

/**
 * packageName    : com.wout.outfit.entity.enums
 * fileName       : OuterCategory
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 외투 카테고리 열거형 (단순화)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       불필요한 메서드 제거, displayName만 유지
 */
enum class OuterCategory(val displayName: String) {
    PADDING("패딩"),
    COAT("코트"),
    JACKET("자켓"),
    LIGHT_JACKET("얇은 자켓"),
    CARDIGAN("가디건"),
    LIGHT_CARDIGAN("얇은 가디건"),
    WINDBREAKER("바람막이")
}