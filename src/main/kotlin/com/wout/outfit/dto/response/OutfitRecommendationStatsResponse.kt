package com.wout.outfit.dto.response

/**
 * packageName    : com.wout.outfit.dto.response
 * fileName       : OutfitRecommendationStatsResponse
 * author         : MinKyu Park
 * date           : 25. 6. 2.
 * description    : 추천 통계 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 2.        MinKyu Park       최초 생성
 */
data class OutfitRecommendationStatsResponse(
    val totalRecommendations: Long,
    val averageConfidenceScore: Double,
    val mostRecommendedTopCategory: String,
    val mostRecommendedBottomCategory: String,
    val highConfidenceCount: Long,
    val recentActivity: String
)