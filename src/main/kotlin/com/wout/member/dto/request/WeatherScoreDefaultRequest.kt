package com.wout.member.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * packageName    : com.wout.member.dto.request
 * fileName       : WeatherScoreDefaultRequest
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 기본 위치 날씨 점수 요청 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 */
@Schema(description = "기본 위치 날씨 점수 요청")
data class WeatherScoreDefaultRequest(

    @field:NotBlank(message = "디바이스 ID는 필수입니다")
    @Schema(description = "사용자 디바이스 ID", example = "device123456")
    val deviceId: String
)