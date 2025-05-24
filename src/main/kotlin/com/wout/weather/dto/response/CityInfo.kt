package com.wout.weather.dto.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.weather.dto.response
 * fileName       : CityInfo
 * author         : MinKyu Park
 * date           : 25. 5. 25.
 * description    : 지원 도시 정보 응답 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 25.        MinKyu Park       최초 생성
 */
@Schema(description = "지원 도시 정보")
data class CityInfo(

    @Schema(description = "도시명", example = "서울")
    val cityName: String,

    @Schema(description = "위도", example = "37.5665")
    val latitude: Double,

    @Schema(description = "경도", example = "126.9780")
    val longitude: Double
)