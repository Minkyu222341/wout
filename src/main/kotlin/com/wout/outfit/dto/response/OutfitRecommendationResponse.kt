package com.wout.outfit.dto.response

import com.wout.outfit.entity.enums.BottomCategory
import com.wout.outfit.entity.enums.OuterCategory
import com.wout.outfit.entity.enums.TopCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.dto.response
 * fileName       : OutfitRecommendationResponse
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 결과 응답 DTO (프론트엔드 구조 호환)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       프론트엔드 다중 추천 구조로 확장
 */
@Schema(description = "아웃핏 추천 결과")
data class OutfitRecommendationResponse(

    @Schema(description = "추천 ID", example = "rec_1701234567890")
    val id: String,

    @Schema(description = "회원 ID", example = "1")
    val memberId: Long,

    @Schema(description = "추천 스타일명", example = "한겨울 완전방한 스타일")
    val name: String,

    @Schema(description = "카테고리별 추천 상세정보")
    val categories: OutfitCategories,

    @Schema(description = "추천 근거", example = "쌀쌀한 날씨라 따뜻하게 입는 게 좋아요")
    val recommendationReason: String,

    @Schema(description = "개인 맞춤 팁", example = "평소 추위를 많이 타시니까 한 겹 더 입는 걸 추천해요")
    val personalTip: String?,

    @Schema(description = "추천 요약", example = "니트 + 청바지 + 자켓")
    val summary: String,

    @Schema(description = "추천 생성 시간", example = "2025-06-02T10:30:00")
    val createdAt: LocalDateTime,

    @Schema(description = "상의 카테고리", example = "SWEATER")
    val topCategory: TopCategory,

    @Schema(description = "하의 카테고리", example = "JEANS")
    val bottomCategory: BottomCategory,

    @Schema(description = "외투 카테고리", example = "JACKET")
    val outerCategory: OuterCategory?
)