package com.wout.outfit.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.dto.response
 * fileName       : OutfitRecommendationSummary
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 요약 응답 DTO (히스토리용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Schema(description = "아웃핏 추천 요약")
data class OutfitRecommendationSummary(

    @Schema(description = "추천 ID", example = "1")
    val id: Long,

    @Schema(description = "날씨 점수", example = "75")
    val weatherScore: Int,

    @Schema(description = "실제 기온", example = "18.5")
    val temperature: Double,

    @Schema(description = "추천 요약 메시지", example = "니트 + 청바지 + 자켓")
    val summaryMessage: String,

    @Schema(description = "추천 신뢰도 점수", example = "85")
    val confidenceScore: Int,

    @Schema(description = "추천 생성 시간", example = "2025-06-02T10:30:00")
    val createdAt: LocalDateTime
)