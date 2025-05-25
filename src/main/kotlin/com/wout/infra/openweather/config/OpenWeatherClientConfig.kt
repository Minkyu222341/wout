package com.wout.infra.openweather.config

import org.springframework.context.annotation.Configuration

/**
 * packageName    : com.wout.infra.openweather.config
 * fileName       : OpenWeatherClientConfig
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : OpenWeather Feign 클라이언트 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Configuration
class OpenWeatherClientConfig {

//    /**
//     * Feign 로깅 레벨 설정
//     */
//    @Bean
//    fun feignLoggerLevel(): Logger.Level {
//        return Logger.Level.FULL
//    }
//
//    /**
//     * Feign 에러 핸들링
//     */
//    @Bean
//    fun errorDecoder(): ErrorDecoder {
//        return OpenWeatherErrorDecoder()
//    }
//
//    /**
//     * OpenWeather API 에러 디코더
//     */
//    class OpenWeatherErrorDecoder : ErrorDecoder {
//        override fun decode(methodKey: String, response: Response): Exception {
//            val status = response.status()
//            val reason = response.reason() ?: "Unknown error"
//
//            return when (status) {
//                401 -> ApiException(ErrorCode.WEATHER_API_ERROR, "날씨 API 인증 실패: 유효하지 않은 API 키")
//                404 -> ApiException(ErrorCode.WEATHER_DATA_NOT_FOUND, "날씨 데이터를 찾을 수 없습니다.")
//                429 -> ApiException(ErrorCode.WEATHER_API_ERROR, "날씨 API 호출 횟수 초과")
//                else -> ApiException(ErrorCode.WEATHER_API_ERROR, "날씨 API 호출 중 오류 발생: $status $reason")
//            }
//        }
//    }
}