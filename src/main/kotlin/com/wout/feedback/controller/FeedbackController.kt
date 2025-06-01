package com.wout.feedback.controller

import com.wout.common.response.ApiResponse
import com.wout.feedback.dto.request.FeedbackSubmitRequest
import com.wout.feedback.dto.response.FeedbackHistoryResponse
import com.wout.feedback.dto.response.FeedbackResponse
import com.wout.feedback.dto.response.FeedbackStatisticsResponse
import com.wout.feedback.service.FeedbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

/**
 * packageName    : com.wout.feedback.controller
 * fileName       : FeedbackController
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 관리 API 컨트롤러 MVP 버전
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성 (MVP 핵심 기능만)
 * 2025-06-01        MinKyu Park       가이드 v2.0 적용 (API First)
 */
@RestController
@RequestMapping("/api/feedback")
@Tag(name = "Feedback API", description = "피드백 수집 및 학습 시스템 API (MVP 버전)")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    @Operation(
        summary = "피드백 제출",
        description = "날씨 추천에 대한 피드백을 제출하고 즉시 학습을 적용합니다"
    )
    @PostMapping("/{deviceId}")
    fun submitFeedback(
        @Parameter(description = "디바이스 ID", required = true, example = "device-12345")
        @PathVariable deviceId: String,
        @Parameter(description = "피드백 정보", required = true)
        @Valid @RequestBody request: FeedbackSubmitRequest
    ): ApiResponse<FeedbackResponse> {
        val result = feedbackService.submitFeedback(deviceId, request)
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "피드백 히스토리 조회",
        description = "사용자의 피드백 히스토리를 페이징으로 조회합니다"
    )
    @GetMapping("/{deviceId}/history")
    fun getFeedbackHistory(
        @Parameter(description = "디바이스 ID", required = true, example = "device-12345")
        @PathVariable deviceId: String,
        @PageableDefault(
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ApiResponse<FeedbackHistoryResponse> {
        val result = feedbackService.getFeedbackHistory(deviceId, pageable)
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "피드백 통계 조회",
        description = "최근 30일간의 피드백 통계를 조회합니다"
    )
    @GetMapping("/{deviceId}/statistics")
    fun getFeedbackStatistics(
        @Parameter(description = "디바이스 ID", required = true, example = "device-12345")
        @PathVariable deviceId: String
    ): ApiResponse<FeedbackStatisticsResponse> {
        val result = feedbackService.getFeedbackStatistics(deviceId)
        return ApiResponse.success(result)
    }

    @Operation(
        summary = "오늘의 피드백 가능 여부",
        description = "오늘 더 피드백을 제출할 수 있는지 확인합니다 (일일 10개 제한)"
    )
    @GetMapping("/{deviceId}/can-submit-today")
    fun canSubmitFeedbackToday(
        @Parameter(description = "디바이스 ID", required = true, example = "device-12345")
        @PathVariable deviceId: String
    ): ApiResponse<Map<String, Any>> {
        val result = feedbackService.canSubmitFeedbackToday(deviceId)
        return ApiResponse.success(result)
    }
}