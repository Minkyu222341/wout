package com.wout.outfit.controller

import com.wout.common.response.ApiResponse
import com.wout.outfit.dto.request.InstantRecommendationRequest
import com.wout.outfit.dto.request.OutfitRecommendationRequest
import com.wout.outfit.dto.request.SatisfactionFeedbackRequest
import com.wout.outfit.dto.response.OutfitRecommendationResponse
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
 * description    : 아웃핏 추천 API 컨트롤러 (MVP 버전)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성 (MVP 핵심 기능만)
 */
@RestController
@RequestMapping("/api/outfit")
@Tag(name = "Outfit Recommendation API", description = "개인화된 아웃핏 추천 시스템 API")
@Validated
class OutfitController(
    private val outfitRecommendationService: OutfitRecommendationService
) {

    @Operation(
        summary = "날씨 데이터 기반 아웃핏 추천",
        description = "특정 날씨 데이터를 기반으로 사용자에게 개인화된 아웃핏을 추천합니다. " +
                "이미 해당 날씨에 대한 추천이 있다면 기존 추천을 반환합니다."
    )
    @PostMapping("/recommend")
    fun generateOutfitRecommendation(
        @Valid @RequestBody request: OutfitRecommendationRequest
    ): ApiResponse<OutfitRecommendationResponse> {
        val result = outfitRecommendationService.generatePersonalizedOutfitRecommendation(
            deviceId = request.deviceId,
            weatherDataId = request.weatherDataId
        )
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "위치 기반 즉시 아웃핏 추천",
        description = "사용자의 현재 위치를 기반으로 즉시 아웃핏을 추천합니다. " +
                "가장 가까운 날씨 데이터를 찾아서 추천을 생성합니다."
    )
    @PostMapping("/recommend/instant")
    fun generateInstantRecommendation(
        @Valid @RequestBody request: InstantRecommendationRequest
    ): ApiResponse<OutfitRecommendationResponse> {
        val result = outfitRecommendationService.generateInstantRecommendation(
            deviceId = request.deviceId,
            latitude = request.latitude,
            longitude = request.longitude
        )
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "추천 만족도 피드백",
        description = "아웃핏 추천에 대한 사용자 만족도를 수집합니다. " +
                "기존 피드백 시스템과 연동하여 추천 알고리즘 개선에 활용됩니다."
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