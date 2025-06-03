package com.wout.outfit.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.outfit.dto.response
 * fileName       : OutfitCategoryInfo
 * author         : MinKyu Park
 * date           : 2025-06-03
 * description    : 개별 카테고리 추천 정보
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-03        MinKyu Park       최초 생성
 */
@Schema(description = "개별 카테고리 추천 정보")
data class OutfitCategoryInfo(

    @Schema(description = "추천 아이템 목록", example = "[\"니트\", \"스웨터\", \"가디건\"]")
    val items: List<String>,

    @Schema(description = "추천 이유", example = "추위를 많이 타시니까 따뜻하게!")
    val reason: String
)