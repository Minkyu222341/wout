package com.wout.outfit.mapper

import com.wout.outfit.dto.response.OutfitCategories
import com.wout.outfit.dto.response.OutfitCategoryInfo
import com.wout.outfit.dto.response.OutfitRecommendationResponse
import com.wout.outfit.dto.response.OutfitRecommendationSummary
import com.wout.outfit.entity.OutfitRecommendation
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.outfit.mapper
 * fileName       : OutfitRecommendationMapper
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 엔티티 ↔ DTO 변환 매퍼 (수정된 버전)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       DTO 구조에 맞게 매퍼 수정
 */
@Component
class OutfitRecommendationMapper {

    /**
     * 엔티티 → 상세 응답 DTO 변환
     */
    fun toResponse(entity: OutfitRecommendation): OutfitRecommendationResponse {
        return OutfitRecommendationResponse(
            id = "rec_${entity.id}",  // ✅ String 형태로 ID 변환
            memberId = entity.memberId,  // ✅ Long 타입 그대로
            name = generateRecommendationName(entity),  // ✅ 추천 스타일명 생성
            categories = createOutfitCategories(entity),  // ✅ OutfitCategories 생성
            recommendationReason = entity.recommendationReason,
            personalTip = entity.personalTip,
            summary = entity.generateSummaryMessage(),  // ✅ 요약 메시지
            createdAt = entity.createdAt,
            topCategory = entity.topCategory,  // ✅ enum 그대로
            bottomCategory = entity.bottomCategory,  // ✅ enum 그대로
            outerCategory = entity.outerCategory  // ✅ enum 그대로 (nullable)
        )
    }

    /**
     * 엔티티 → 요약 응답 DTO 변환 (히스토리용)
     */
    fun toSummary(entity: OutfitRecommendation): OutfitRecommendationSummary {
        return OutfitRecommendationSummary(
            id = entity.id,  // ✅ Long 타입 그대로
            weatherScore = entity.weatherScore,
            temperature = entity.temperature,  // ✅ temperature 필드명 그대로
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

    // ===== 헬퍼 메서드들 =====

    /**
     * 추천 스타일명 생성
     */
    private fun generateRecommendationName(entity: OutfitRecommendation): String {
        val seasonalCategory = entity.getSeasonalCategory()
        val layeringComplexity = entity.getLayeringComplexity()

        return when {
            layeringComplexity >= 4 -> "$seasonalCategory 완전레이어드 스타일"
            layeringComplexity >= 3 -> "$seasonalCategory 멀티레이어 스타일"
            entity.hasOuterwearRecommendation() -> "$seasonalCategory 기본 스타일"
            else -> "$seasonalCategory 심플 스타일"
        }
    }

    /**
     * OutfitCategories DTO 생성
     */
    private fun createOutfitCategories(entity: OutfitRecommendation): OutfitCategories {
        return OutfitCategories(
            top = OutfitCategoryInfo(
                items = entity.getTopItemsList(),
                reason = "온도에 적합한 상의 추천"
            ),
            bottom = OutfitCategoryInfo(
                items = entity.getBottomItemsList(),
                reason = "편안하고 실용적인 하의"
            ),
            outer = OutfitCategoryInfo(
                items = entity.getOuterItemsList(),
                reason = if (entity.hasOuterwearRecommendation()) "바람과 온도 변화 대비" else "외투 불필요"
            ),
            accessories = OutfitCategoryInfo(
                items = entity.getAccessoryItemsList(),
                reason = if (entity.hasAccessoryRecommendation()) "스타일 완성과 실용성" else "소품 불필요"
            )
        )
    }
}