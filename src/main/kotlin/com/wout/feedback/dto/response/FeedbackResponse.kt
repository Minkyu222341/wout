package com.wout.feedback.dto.response

import java.time.LocalDateTime

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : FeedbackResponse
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 피드백 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class FeedbackResponse(
    val id: Long,
    val memberId: Long,
    val weatherDataId: Long,
    val feedbackType: FeedbackTypeInfo,
    val actualTemperature: Double,
    val feelsLikeTemperature: Double,
    val weatherScore: Int,
    val adjustmentAmount: Double,
    val previousComfortTemp: Int,
    val updatedComfortTemp: Int,
    val comments: String?,
    val isConfirmed: Boolean,
    val reliabilityScore: Double,
    val learningWeight: Double,
    val temperatureDifference: Double,
    val createdAt: LocalDateTime
)