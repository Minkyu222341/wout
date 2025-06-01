package com.wout.weather.controller

import com.wout.common.response.ApiResponse
import com.wout.weather.dto.request.WeatherRequest
import com.wout.weather.dto.response.CityInfo
import com.wout.weather.dto.response.WeatherResponse
import com.wout.weather.dto.response.WeatherSummary
import com.wout.weather.entity.enums.KoreanMajorCity
import com.wout.weather.service.WeatherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
 * 25. 5. 31.        MinKyu Park       ResponseEntity 제거, ApiResponse 통일
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
        @Valid request: WeatherRequest
    ): ApiResponse<WeatherResponse> {
        val weatherData = weatherService.getWeatherByLocation(request.latitude, request.longitude)
        return ApiResponse.success(weatherData)
    }

    @Operation(
        summary = "도시별 현재 날씨 조회",
        description = "특정 도시명으로 해당 도시의 최신 날씨 정보를 조회합니다."
    )
    @GetMapping("/cities/{cityName}")
    fun getWeatherByCity(
        @Parameter(description = "도시명", example = "서울", required = true)
        @PathVariable cityName: String
    ): ApiResponse<WeatherResponse> {
        val weatherData = weatherService.getWeatherByCity(cityName)
        return ApiResponse.success(weatherData)
    }

    @Operation(
        summary = "전국 주요 도시 날씨 조회",
        description = "23개 주요 도시의 현재 날씨 정보를 한번에 조회합니다."
    )
    @GetMapping("/cities")
    fun getAllMajorCitiesWeather(): ApiResponse<List<WeatherResponse>> {
        val weatherDataList = weatherService.getAllMajorCitiesWeather()
        return ApiResponse.success(weatherDataList)
    }

    @Operation(
        summary = "도시별 날씨 히스토리 조회",
        description = "특정 도시의 최근 24시간 날씨 변화 히스토리를 조회합니다."
    )
    @GetMapping("/cities/{cityName}/history")
    fun getWeatherHistory(
        @Parameter(description = "도시명", example = "서울", required = true)
        @PathVariable cityName: String
    ): ApiResponse<List<WeatherResponse>> {
        val weatherHistory = weatherService.getWeatherHistory(cityName)
        return ApiResponse.success(weatherHistory)
    }

    @Operation(
        summary = "전국 날씨 현황 요약",
        description = "전국 주요 도시의 평균/최고/최저 온도, 습도 등 요약 정보를 조회합니다."
    )
    @GetMapping("/summary")
    fun getWeatherSummary(): ApiResponse<WeatherSummary> {
        val summary = weatherService.getWeatherSummary()
        return ApiResponse.success(summary)
    }

    @Operation(
        summary = "지원하는 주요 도시 목록 조회",
        description = "날씨 정보를 제공하는 23개 주요 도시 목록을 조회합니다."
    )
    @GetMapping("/cities/supported")
    fun getSupportedCities(): ApiResponse<List<CityInfo>> {
        val cities = KoreanMajorCity.entries.map { city ->
            CityInfo(
                cityName = city.cityName,
                latitude = city.latitude,
                longitude = city.longitude
            )
        }
        return ApiResponse.success(cities)
    }
}