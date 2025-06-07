package com.wout.outfit.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.outfit.dto.response
 * fileName       : OutfitCategories
 * author         : MinKyu Park
 * date           : 2025-06-03
 * description    : 카테고리별 아웃핏 추천 상세정보
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-03        MinKyu Park       최초 생성
 */
@Schema(description = "카테고리별 아웃핏 추천 상세정보")
data class OutfitCategories(

    @Schema(description = "상의 추천 정보")
    val top: OutfitCategoryInfo,

    @Schema(description = "하의 추천 정보")
    val bottom: OutfitCategoryInfo,

    @Schema(description = "외투 추천 정보")
    val outer: OutfitCategoryInfo,

    @Schema(description = "소품 추천 정보")
    val accessories: OutfitCategoryInfo
)