package com.wout.weather.controller

import com.wout.common.response.ApiResponse
import com.wout.weather.dto.response.WeatherResponseDto
import com.wout.weather.service.WeatherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
@Tag(name = "날씨 API", description = "날씨 정보 조회 관련 API")
class WeatherController(
    private val weatherService: WeatherService
) {

    @Operation(
        summary = "위치 기반 현재 날씨 조회",
        description = "사용자의 위도/경도를 기반으로 가장 가까운 주요 도시의 최신 날씨 정보를 조회합니다."
    )
    @GetMapping
    fun getCurrentWeatherByLocation(
        @Parameter(description = "위도", example = "37.5665", required = true)
        @RequestParam lat: Double,

        @Parameter(description = "경도", example = "126.9780", required = true)
        @RequestParam lon: Double
    ): ResponseEntity<ApiResponse<WeatherResponseDto>> {
        val weatherData = weatherService.getWeatherByLocation(lat, lon)
        return ResponseEntity.ok(ApiResponse.success(weatherData))
    }

    @Operation(
        summary = "도시별 현재 날씨 조회",
        description = "특정 도시명으로 해당 도시의 최신 날씨 정보를 조회합니다."
    )
    @GetMapping("/cities/{cityName}")
    fun getWeatherByCity(
        @Parameter(description = "도시명", example = "서울", required = true)
        @PathVariable cityName: String
    ): ResponseEntity<ApiResponse<WeatherResponseDto>> {
        val weatherData = weatherService.getWeatherByCity(cityName)
        return ResponseEntity.ok(ApiResponse.success(weatherData))
    }

    @Operation(
        summary = "전국 주요 도시 날씨 조회",
        description = "23개 주요 도시의 현재 날씨 정보를 한번에 조회합니다."
    )
    @GetMapping("/cities")
    fun getAllMajorCitiesWeather(): ResponseEntity<ApiResponse<List<WeatherResponseDto>>> {
        val weatherDataList = weatherService.getAllMajorCitiesWeather()
        return ResponseEntity.ok(ApiResponse.success(weatherDataList))
    }

    @Operation(
        summary = "도시별 날씨 히스토리 조회",
        description = "특정 도시의 최근 24시간 날씨 변화 히스토리를 조회합니다."
    )
    @GetMapping("/cities/{cityName}/history")
    fun getWeatherHistory(
        @Parameter(description = "도시명", example = "서울", required = true)
        @PathVariable cityName: String
    ): ResponseEntity<ApiResponse<List<WeatherResponseDto>>> {
        val weatherHistory = weatherService.getWeatherHistory(cityName)
        return ResponseEntity.ok(ApiResponse.success(weatherHistory))
    }

    @Operation(
        summary = "전국 날씨 현황 요약",
        description = "전국 주요 도시의 평균/최고/최저 온도, 습도 등 요약 정보를 조회합니다."
    )
    @GetMapping("/summary")
    fun getWeatherSummary(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val summary = weatherService.getWeatherSummary()
        return ResponseEntity.ok(ApiResponse.success(summary))
    }

    @Operation(
        summary = "지원하는 주요 도시 목록 조회",
        description = "날씨 정보를 제공하는 23개 주요 도시 목록을 조회합니다."
    )
    @GetMapping("/cities/supported")
    fun getSupportedCities(): ResponseEntity<ApiResponse<List<Map<String, Any>>>> {
        val cities = com.wout.weather.entity.KoreanMajorCity.entries.map { city ->
            mapOf(
                "cityName" to city.cityName,
                "latitude" to city.latitude,
                "longitude" to city.longitude
            )
        }
        return ResponseEntity.ok(ApiResponse.success(cities))
    }
}