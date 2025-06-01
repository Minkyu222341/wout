package com.wout.feedback.dto.response

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : TemperatureAnalysis
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 온도 분석 정보 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-01        MinKyu Park       최초 생성
 */
data class TemperatureAnalysis(
    val averageTemperatureDifference: Double,
    val coldBias: Double,
    val hotBias: Double,
    val optimalTemperatureRange: String
)