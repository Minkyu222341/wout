package com.wout.outfit.mapper

import com.wout.outfit.dto.response.OutfitRecommendationResponse
import com.wout.outfit.dto.response.OutfitRecommendationSummary
import com.wout.outfit.entity.OutfitRecommendation
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.outfit.mapper
 * fileName       : OutfitRecommendationMapper
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 엔티티 ↔ DTO 변환 매퍼
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 */
@Component
class OutfitRecommendationMapper {

    /**
     * 엔티티 → 상세 응답 DTO 변환
     */
    fun toResponse(entity: OutfitRecommendation): OutfitRecommendationResponse {
        return OutfitRecommendationResponse(
            id = entity.id,
            memberId = entity.memberId,
            weatherScore = entity.weatherScore,
            temperature = entity.temperature,
            feelsLikeTemperature = entity.feelsLikeTemperature,
            topCategory = entity.topCategory.displayName,
            topItems = entity.getTopItemsList(),
            bottomCategory = entity.bottomCategory.displayName,
            bottomItems = entity.getBottomItemsList(),
            outerCategory = entity.outerCategory?.displayName,
            outerItems = entity.getOuterItemsList(),
            accessoryItems = entity.getAccessoryItemsList(),
            recommendationReason = entity.recommendationReason,
            personalTip = entity.personalTip,
            confidenceScore = entity.confidenceScore,
            summaryMessage = entity.generateSummaryMessage(),
            createdAt = entity.createdAt
        )
    }

    /**
     * 엔티티 → 요약 응답 DTO 변환 (히스토리용)
     */
    fun toSummary(entity: OutfitRecommendation): OutfitRecommendationSummary {
        return OutfitRecommendationSummary(
            id = entity.id,
            weatherScore = entity.weatherScore,
            temperature = entity.temperature,
            summaryMessage = entity.generateSummaryMessage(),
            confidenceScore = entity.confidenceScore,
            createdAt = entity.createdAt
        )
    }

    /**
     * 엔티티 리스트 → 상세 응답 리스트 변환
     */
    fun toResponseList(entities: List<OutfitRecommendation>): List<OutfitRecommendationResponse> {
        return entities.map { toResponse(it) }
    }

    /**
     * 엔티티 리스트 → 요약 응답 리스트 변환
     */
    fun toSummaryList(entities: List<OutfitRecommendation>): List<OutfitRecommendationSummary> {
        return entities.map { toSummary(it) }
    }
}