package com.wout.member.model

/**
 * packageName    : com.wout.member.model
 * fileName       : ElementWeights
 * author         : MinKyu Park
 * date           : 2025-06-08
 * description    : 날씨 요소별 개인화 가중치 모델 (정수 25~75 범위 보정)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-08        MinKyu Park       최초 생성 (Double 버전)
 * 2025-06-08        MinKyu Park       Double → Int 타입 변경, 로직 호환성 개선
 */
data class ElementWeights(
    val temperature: Int,
    val humidity: Int,
    val wind: Int,
    val uv: Int,
    val airQuality: Int
) {
    companion object {
        private const val MIN_WEIGHT = 25
        private const val MAX_WEIGHT = 75

        /**
         * 원본 가중치(1–100) 값을 25–75 범위로 보정해 Int 타입 모델로 반환
         */
        fun fromRaw(
            temperatureWeight: Int,
            humidityWeight: Int,
            windWeight: Int,
            uvWeight: Int,
            airQualityWeight: Int
        ): ElementWeights {
            fun limit(w: Int) = w.coerceIn(MIN_WEIGHT, MAX_WEIGHT)
            return ElementWeights(
                temperature = limit(temperatureWeight),
                humidity = limit(humidityWeight),
                wind = limit(windWeight),
                uv = limit(uvWeight),
                airQuality = limit(airQualityWeight)
            )
        }
    }
}