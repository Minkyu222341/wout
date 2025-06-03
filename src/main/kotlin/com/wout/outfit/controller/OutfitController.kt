package com.wout.outfit.controller

import com.wout.common.response.ApiResponse
import com.wout.outfit.dto.request.OutfitRecommendationRequest
import com.wout.outfit.dto.request.SatisfactionFeedbackRequest
import com.wout.outfit.dto.response.OutfitRecommendationResponse
import com.wout.outfit.dto.response.OutfitRecommendationSummary
import com.wout.outfit.service.OutfitRecommendationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * packageName    : com.wout.outfit.controller
 * fileName       : OutfitController
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 API 컨트롤러 (다중 추천 지원)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성 (MVP 핵심 기능만)
 * 2025-06-03        MinKyu Park       다중 추천 지원 + 히스토리 조회 추가
 */
@RestController
@RequestMapping("/api/outfit")
@Tag(name = "Outfit Recommendation API", description = "개인화된 아웃핏 추천 시스템 API")
@Validated
class OutfitController(
    private val outfitRecommendationService: OutfitRecommendationService
) {

    @Operation(
        summary = "🆕 다중 아웃핏 추천 생성",
        description = "날씨 데이터를 기반으로 스타일별 다중 아웃핏 추천을 생성합니다. " +
                "(예: 캐주얼, 세미정장, 민감형 등) 1시간 내 동일한 요청이 있으면 기존 추천을 반환합니다."
    )
    @PostMapping("/recommendations")
    fun generateMultipleOutfitRecommendations(
        @Valid @RequestBody request: OutfitRecommendationRequest
    ): ApiResponse<List<OutfitRecommendationResponse>> {
        val results = outfitRecommendationService.generatePersonalizedOutfitRecommendations(
            deviceId = request.deviceId,
            weatherDataId = request.weatherDataId
        )
        return ApiResponse.success(results)
    }

    @Operation(
        summary = "단일 아웃핏 추천 조회 (호환성)",
        description = "기존 API와의 호환성을 위한 단일 추천 조회입니다. " +
                "다중 추천 중 첫 번째 추천을 반환합니다."
    )
    @PostMapping("/recommend")
    fun generateSingleOutfitRecommendation(
        @Valid @RequestBody request: OutfitRecommendationRequest
    ): ApiResponse<OutfitRecommendationResponse> {
        val results = outfitRecommendationService.generatePersonalizedOutfitRecommendations(
            deviceId = request.deviceId,
            weatherDataId = request.weatherDataId
        )
        val firstRecommendation = results.firstOrNull()
            ?: throw IllegalStateException("추천 생성에 실패했습니다")

        return ApiResponse.success(firstRecommendation)
    }

    @Operation(
        summary = "🆕 추천 히스토리 조회",
        description = "사용자의 최근 아웃핏 추천 히스토리를 조회합니다. " +
                "기본 10개까지 조회되며, limit 파라미터로 조회 개수를 조정할 수 있습니다."
    )
    @GetMapping("/{deviceId}/history")
    fun getRecommendationHistory(
        @Parameter(description = "디바이스 ID", required = true)
        @PathVariable deviceId: String,
        @Parameter(description = "조회할 추천 개수 (기본: 10개)")
        @RequestParam(defaultValue = "10") @Positive limit: Int
    ): ApiResponse<List<OutfitRecommendationSummary>> {
        val results = outfitRecommendationService.getRecommendationHistory(deviceId, limit)
        return ApiResponse.success(results)
    }

    @Operation(
        summary = "추천 만족도 피드백",
        description = "아웃핏 추천에 대한 사용자 만족도를 수집합니다. " +
                "향후 피드백 시스템과 연동하여 추천 알고리즘 개선에 활용됩니다."
    )
    @PostMapping("/{recommendationId}/feedback")
    fun submitSatisfactionFeedback(
        @Parameter(description = "추천 ID", required = true)
        @PathVariable @Positive(message = "추천 ID는 양수여야 합니다") recommendationId: Long,

        @Valid @RequestBody request: SatisfactionFeedbackRequest
    ): ApiResponse<OutfitRecommendationResponse> {
        val result = outfitRecommendationService.updateRecommendationSatisfaction(
            recommendationId = recommendationId,
            satisfactionScore = request.satisfactionScore,
            feedback = request.feedback
        )
        return ApiResponse.success(result)
    }
}