package com.wout.weather.controller

import com.wout.common.response.ApiResponse
import com.wout.weather.dto.response.WeatherResponseDto
import com.wout.weather.service.WeatherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse

/**
 * packageName    : com.wout.weather.controller
 * fileName       : WeatherController
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : 날씨 정보 API 컨트롤러
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "날씨", description = "날씨 정보 관련 API")
class WeatherController(
    private val weatherService: WeatherService
) {
    /**
     * 현재 위치 기반 날씨 정보 조회
     */
    @Operation(
        summary = "현재 날씨 조회",
        description = "위도/경도 기반으로 현재 날씨 정보를 조회합니다."
    )
    @ApiResponses(value = [
        SwaggerResponse(
            responseCode = "200",
            description = "날씨 조회 성공",
            content = [Content(schema = Schema(implementation = WeatherResponseDto::class))]
        ),
        SwaggerResponse(
            responseCode = "404",
            description = "날씨 데이터를 찾을 수 없음"
        ),
        SwaggerResponse(
            responseCode = "500",
            description = "날씨 API 호출 오류"
        )
    ])
    @GetMapping
    fun getCurrentWeather(
        @Parameter(description = "위도", required = true, example = "37.5665")
        @RequestParam latitude: Double,

        @Parameter(description = "경도", required = true, example = "126.9780")
        @RequestParam longitude: Double
    ): ResponseEntity<ApiResponse<WeatherResponseDto>> {
        val weatherData = weatherService.getCurrentWeather(latitude, longitude)
        return ResponseEntity.ok(ApiResponse.success(weatherData))
    }

}