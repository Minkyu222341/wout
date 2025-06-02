package com.wout.outfit.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * packageName    : com.wout.outfit.dto.response
 * fileName       : OutfitRecommendationResponse
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 결과 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Schema(description = "아웃핏 추천 결과")
data class OutfitRecommendationResponse(

    @Schema(description = "추천 ID", example = "1")
    val id: Long,

    @Schema(description = "회원 ID", example = "1")
    val memberId: Long,

    @Schema(description = "날씨 점수", example = "75")
    val weatherScore: Int,

    @Schema(description = "실제 기온", example = "18.5")
    val temperature: Double,

    @Schema(description = "개인화된 체감온도", example = "15.2")
    val feelsLikeTemperature: Double,

    @Schema(description = "상의 카테고리", example = "니트")
    val topCategory: String,

    @Schema(description = "상의 아이템 목록", example = "[\"니트\", \"스웨터\", \"가디건\"]")
    val topItems: List<String>,

    @Schema(description = "하의 카테고리", example = "청바지")
    val bottomCategory: String,

    @Schema(description = "하의 아이템 목록", example = "[\"청바지\", \"면바지\", \"치노팬츠\"]")
    val bottomItems: List<String>,

    @Schema(description = "외투 카테고리", example = "자켓")
    val outerCategory: String?,

    @Schema(description = "외투 아이템 목록", example = "[\"자켓\", \"가디건\", \"코트\"]")
    val outerItems: List<String>?,

    @Schema(description = "소품 아이템 목록", example = "[\"목도리\", \"장갑\", \"모자\"]")
    val accessoryItems: List<String>?,

    @Schema(description = "추천 근거", example = "쌀쌀한 날씨라 따뜻하게 입는 게 좋아요. 바람이 강해서 체감온도가 더 낮게 느껴질 수 있어요.")
    val recommendationReason: String,

    @Schema(description = "개인 맞춤 팁", example = "평소 추위를 많이 타시니까 한 겹 더 입는 걸 추천해요")
    val personalTip: String?,

    @Schema(description = "추천 신뢰도 점수 (0-100)", example = "85")
    val confidenceScore: Int,

    @Schema(description = "추천 요약 메시지", example = "니트, 스웨터 + 청바지 + 자켓")
    val summaryMessage: String,

    @Schema(description = "추천 생성 시간", example = "2025-06-02T10:30:00")
    val createdAt: LocalDateTime
)