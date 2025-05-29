package com.wout.member.service

import com.wout.common.exception.ApiException
import com.wout.common.exception.ErrorCode.*
import com.wout.member.dto.response.ElementScoreDetails
import com.wout.member.dto.response.LocationInfo
import com.wout.member.dto.response.WeatherInfo
import com.wout.member.dto.response.WeatherScoreResponse
import com.wout.member.entity.WeatherPreference
import com.wout.member.repository.MemberRepository
import com.wout.member.repository.WeatherPreferenceRepository
import com.wout.member.util.WeatherScoreCalculator
import com.wout.member.util.WeatherScoreResult
import com.wout.weather.entity.WeatherData
import com.wout.weather.service.WeatherService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * packageName    : com.wout.member.service
 * fileName       : WeatherScoreService
 * author         : MinKyu Park
 * date           : 2025-05-27
 * description    : 개인화된 날씨 점수 계산 비즈니스 로직
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-05-27        MinKyu Park       LocationInfo import 추가
 * 2025-05-29        MinKyu Park       WeatherScoreResponse 생성 로직 공통화
 */
@Service
@Transactional(readOnly = true)
class WeatherScoreService(
    private val weatherScoreCalculator: WeatherScoreCalculator,
    private val memberRepository: MemberRepository,
    private val weatherPreferenceRepository: WeatherPreferenceRepository,
    private val weatherService: WeatherService
) {

    /**
     * 개인화된 날씨 점수 계산 (위도/경도 기반)
     */
    fun getPersonalizedWeatherScore(
        deviceId: String,
        latitude: Double,
        longitude: Double
    ): WeatherScoreResponse {
        if (deviceId.isBlank()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }

        // 위도/경도 유효성 검증
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            throw ApiException(INVALID_INPUT_VALUE)
        }

        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(MEMBER_NOT_FOUND) }

        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)
            .orElseThrow { ApiException(SENSITIVITY_PROFILE_NOT_FOUND) }

        val weatherData = try {
            weatherService.getCurrentWeatherData(latitude, longitude)
        } catch (e: Exception) {
            throw ApiException(WEATHER_DATA_NOT_FOUND)
        }

        // 개인화된 점수 계산
        val scoreResult = calculatePersonalizedScore(weatherData, weatherPreference)

        // 개인화된 메시지 생성
        val personalizedMessage = generatePersonalizedMessage(scoreResult, weatherPreference)

        // ✅ 공통 메서드로 응답 생성
        return buildWeatherScoreResponse(
            scoreResult = scoreResult,
            personalizedMessage = personalizedMessage,
            weatherData = weatherData,
            weatherPreference = weatherPreference,
            latitude = latitude,
            longitude = longitude
        )
    }

    /**
     * 도시명 기반 날씨 점수 계산
     */
    fun getPersonalizedWeatherScoreByCity(
        deviceId: String,
        cityName: String
    ): WeatherScoreResponse {
        if (deviceId.isBlank() || cityName.isBlank()) {
            throw ApiException(INVALID_INPUT_VALUE)
        }

        // 1. 사용자 조회
        val member = memberRepository.findByDeviceId(deviceId)
            .orElseThrow { ApiException(MEMBER_NOT_FOUND) }

        // 2. 날씨 선호도 조회
        val weatherPreference = weatherPreferenceRepository.findByMemberId(member.id)
            .orElseThrow { ApiException(SENSITIVITY_PROFILE_NOT_FOUND) }

        // 3. 도시명으로 날씨 데이터 조회
        val weatherData = try {
            weatherService.getCurrentWeatherDataByCity(cityName)
        } catch (e: Exception) {
            throw ApiException(WEATHER_DATA_NOT_FOUND)
        }

        // 4. 개인화된 점수 계산
        val scoreResult = calculatePersonalizedScore(weatherData, weatherPreference)

        // 5. 개인화된 메시지 생성
        val personalizedMessage = generatePersonalizedMessage(scoreResult, weatherPreference)

        // ✅ 공통 메서드로 응답 생성
        return buildWeatherScoreResponse(
            scoreResult = scoreResult,
            personalizedMessage = personalizedMessage,
            weatherData = weatherData,
            weatherPreference = weatherPreference,
            latitude = weatherData.latitude,
            longitude = weatherData.longitude
        )
    }

    /**
     * WeatherScoreResponse 생성 공통 메서드
     */
    private fun buildWeatherScoreResponse(
        scoreResult: WeatherScoreResult,
        personalizedMessage: String,
        weatherData: WeatherData,
        weatherPreference: WeatherPreference,
        latitude: Double,
        longitude: Double
    ): WeatherScoreResponse {
        return WeatherScoreResponse(
            totalScore = scoreResult.totalScore.toInt(),
            grade = scoreResult.grade,
            message = personalizedMessage,
            elementScores = ElementScoreDetails(
                temperature = scoreResult.elementScores.temperature.toInt(),
                humidity = scoreResult.elementScores.humidity.toInt(),
                wind = scoreResult.elementScores.wind.toInt(),
                uv = scoreResult.elementScores.uv.toInt(),
                airQuality = scoreResult.elementScores.airQuality.toInt()
            ),
            weatherInfo = WeatherInfo(
                temperature = weatherData.temperature,
                feelsLikeTemperature = weatherPreference.calculateFeelsLikeTemperature(
                    weatherData.temperature,
                    weatherData.windSpeed,
                    weatherData.humidity.toDouble()
                ),
                humidity = weatherData.humidity.toDouble(),
                windSpeed = weatherData.windSpeed,
                uvIndex = weatherData.uvIndex ?: 0.0,
                pm25 = weatherData.pm25 ?: 0.0,
                pm10 = weatherData.pm10 ?: 0.0
            ),
            location = LocationInfo(
                latitude = latitude,
                longitude = longitude,
                cityName = weatherData.cityName
            )
        )
    }

    /**
     * 개인화된 점수 계산 (내부 메서드)
     */
    private fun calculatePersonalizedScore(
        weatherData: WeatherData,
        weatherPreference: WeatherPreference
    ): WeatherScoreResult {
        return try {
            weatherScoreCalculator.calculateTotalScore(
                temperature = weatherData.temperature,
                humidity = weatherData.humidity.toDouble(),
                windSpeed = weatherData.windSpeed,
                uvIndex = weatherData.uvIndex ?: 0.0,
                pm25 = weatherData.pm25 ?: 0.0,
                pm10 = weatherData.pm10 ?: 0.0,
                weatherPreference = weatherPreference
            )
        } catch (e: Exception) {
            throw ApiException(INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * 개인화된 메시지 생성
     */
    private fun generatePersonalizedMessage(
        scoreResult: WeatherScoreResult,
        weatherPreference: WeatherPreference
    ): String {
        val score = scoreResult.totalScore.toInt()
        val grade = scoreResult.grade

        // 기본 메시지
        val baseMessage = "${grade.emoji} ${score}점. ${grade.description}"

        // 개인 특성 추출
        val personalTraits = mutableListOf<String>()
        val priorities = weatherPreference.getPriorityList()

        // 우선순위 기반 특성 추출
        priorities.forEach { priority ->
            when (priority) {
                "heat" -> personalTraits.add("더위를 싫어하시는데")
                "cold" -> personalTraits.add("추위를 많이 타시는 편이라")
                "humidity" -> personalTraits.add("습함을 특히 싫어하시는데")
                "wind" -> personalTraits.add("바람에 민감하시는데")
                "uv" -> personalTraits.add("자외선에 예민하셔서")
                "pollution" -> personalTraits.add("공기질에 민감하시는데")
            }
        }

        // 상황 분석 및 결론 생성
        val situationAndConclusion = when {
            score >= 90 -> "날씨 조건이 완벽해서 외출하기 좋은 날이에요!"
            score >= 70 -> "전반적으로 괜찮은 날씨예요."
            score >= 50 -> "보통 수준의 날씨입니다."
            score >= 30 -> "조금 아쉬운 날씨네요."
            else -> "외출 시 주의가 필요해 보여요."
        }

        return if (personalTraits.isNotEmpty()) {
            "$baseMessage ${personalTraits.first()} $situationAndConclusion"
        } else {
            "$baseMessage $situationAndConclusion"
        }
    }
}