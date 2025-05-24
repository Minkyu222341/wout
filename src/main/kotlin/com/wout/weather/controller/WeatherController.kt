package com.wout.weather.controller

import com.wout.common.response.ApiResponse
import com.wout.weather.dto.response.CityInfo
import com.wout.weather.dto.response.WeatherResponse
import com.wout.weather.dto.response.WeatherSummary
import com.wout.weather.entity.enums.KoreanMajorCity
import com.wout.weather.service.WeatherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
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
 * 25. 5. 25.        MinKyu Park       유효성 검증 추가, DTO 타입 안전성 개선
 */
@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "날씨 API", description = "날씨 정보 조회 관련 API")
@Validated
class WeatherController(
    private val weatherService: WeatherService
) {

    @Operation(
        summary = "위치 기반 현재 날씨 조회",
        description = "사용자의 위도/경도를 기반으로 가장 가까운 주요 도시의 최신 날씨 정보를 조회합니다."
    )
    @GetMapping
    fun getCurrentWeatherByLocation(
        @Parameter(description = "위도 (-90 ~ 90)", example = "37.5665", required = true)
        @RequestParam
        @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
        lat: Double,

        @Parameter(description = "경도 (-180 ~ 180)", example = "126.9780", required = true)
        @RequestParam
        @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
        lon: Double
    ): ResponseEntity<ApiResponse<WeatherResponse>> {
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
    ): ResponseEntity<ApiResponse<WeatherResponse>> {
        val weatherData = weatherService.getWeatherByCity(cityName)
        return ResponseEntity.ok(ApiResponse.success(weatherData))
    }

    @Operation(
        summary = "전국 주요 도시 날씨 조회",
        description = "23개 주요 도시의 현재 날씨 정보를 한번에 조회합니다."
    )
    @GetMapping("/cities")
    fun getAllMajorCitiesWeather(): ResponseEntity<ApiResponse<List<WeatherResponse>>> {
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
    ): ResponseEntity<ApiResponse<List<WeatherResponse>>> {
        val weatherHistory = weatherService.getWeatherHistory(cityName)
        return ResponseEntity.ok(ApiResponse.success(weatherHistory))
    }

    @Operation(
        summary = "전국 날씨 현황 요약",
        description = "전국 주요 도시의 평균/최고/최저 온도, 습도 등 요약 정보를 조회합니다."
    )
    @GetMapping("/summary")
    fun getWeatherSummary(): ResponseEntity<ApiResponse<WeatherSummary>> {
        val summary = weatherService.getWeatherSummary()
        return ResponseEntity.ok(ApiResponse.success(summary))
    }

    @Operation(
        summary = "지원하는 주요 도시 목록 조회",
        description = "날씨 정보를 제공하는 23개 주요 도시 목록을 조회합니다."
    )
    @GetMapping("/cities/supported")
    fun getSupportedCities(): ResponseEntity<ApiResponse<List<CityInfo>>> {
        val cities = KoreanMajorCity.entries.map { city ->
            CityInfo(
                cityName = city.cityName,
                latitude = city.latitude,
                longitude = city.longitude
            )
        }
        return ResponseEntity.ok(ApiResponse.success(cities))
    }
}