package com.wout.member.util

import com.wout.member.entity.WeatherPreference
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.member.util
 * fileName       : WeatherMessage
 * author         : MinKyu Park
 * date           : 2025-05-31
 * description    : 사용자에게 표시할 날씨 관련 메시지 생성 전담 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-31        MinKyu Park       최초 생성
 */
@Component
class WeatherMessage {

    companion object {
        // 개인 특성 메시지 매핑
        private val PERSONAL_TRAIT_MESSAGES = mapOf(
            "heat" to "더위를 싫어하시는데",
            "cold" to "추위를 많이 타시는 편이라",
            "humidity" to "습함을 특히 싫어하시는데",
            "wind" to "바람에 민감하시는데",
            "uv" to "자외선에 예민하셔서",
            "pollution" to "공기질에 민감하시는데"
        )

        // 점수별 상황 결론 메시지
        private val SCORE_CONCLUSION_MESSAGES = mapOf(
            90..100 to "날씨 조건이 완벽해서 외출하기 좋은 날이에요!",
            70..89 to "전반적으로 괜찮은 날씨예요.",
            50..69 to "보통 수준의 날씨입니다.",
            30..49 to "조금 아쉬운 날씨네요.",
            0..29 to "외출 시 주의가 필요해 보여요."
        )

        // 학습 트렌드 메시지
        private val LEARNING_TREND_MESSAGES = mapOf(
            0.3..Double.MAX_VALUE to "학습 중 (더위 방향)",
            Double.MIN_VALUE..-0.3 to "학습 중 (추위 방향)"
        )

        // 기본 학습 완료 메시지
        private const val DEFAULT_LEARNING_MESSAGE = "안정적 학습 완료"
    }

    /**
     * 개인화된 날씨 메시지 생성
     */
    fun generatePersonalizedMessage(
        scoreResult: WeatherScoreResult,
        weatherPreference: WeatherPreference
    ): String {
        val score = scoreResult.totalScore.toInt()
        val grade = scoreResult.grade

        // 기본 메시지
        val baseMessage = "${grade.emoji} ${score}점. ${grade.description}"

        // 개인 특성 추출
        val personalTraits = extractPersonalTraits(weatherPreference)

        // 상황 분석 및 결론 생성
        val situationAndConclusion = getSituationConclusion(score)

        return if (personalTraits.isNotEmpty()) {
            "$baseMessage ${personalTraits.first()} $situationAndConclusion"
        } else {
            "$baseMessage $situationAndConclusion"
        }
    }

    /**
     * 개인 특성 메시지 추출
     */
    private fun extractPersonalTraits(weatherPreference: WeatherPreference): List<String> {
        val priorities = weatherPreference.getPriorityList()

        return priorities.mapNotNull { priority ->
            PERSONAL_TRAIT_MESSAGES[priority]
        }
    }

    /**
     * 점수에 따른 상황 결론 메시지 반환
     */
    private fun getSituationConclusion(score: Int): String {
        return SCORE_CONCLUSION_MESSAGES.entries
            .firstOrNull { score in it.key }
            ?.value ?: "날씨 상태를 확인할 수 없습니다."
    }

    /**
     * 날씨 요약 메시지 생성 (WeatherService용)
     */
    fun generateWeatherSummaryMessage(
        avgTemperature: Double,
        maxTemperature: Double,
        minTemperature: Double
    ): String {
        return when {
            avgTemperature >= 25 -> "전국적으로 더운 날씨입니다"
            avgTemperature <= 10 -> "전국적으로 추운 날씨입니다"
            maxTemperature - minTemperature >= 15 -> "지역별 기온차가 큰 날씨입니다"
            else -> "전국적으로 쾌적한 날씨입니다"
        }
    }

    /**
     * 피드백 학습 트렌드 메시지 생성 (FeedbackService용)
     */
    fun generateLearningTrendMessage(averageAdjustment: Double): String {
        return LEARNING_TREND_MESSAGES.entries
            .firstOrNull { averageAdjustment in it.key }
            ?.value ?: DEFAULT_LEARNING_MESSAGE
    }

    /**
     * 데이터 부족 메시지
     */
    fun getDataInsufficientMessage(): String {
        return "학습 데이터 부족"
    }

    /**
     * 빈 날씨 데이터 메시지
     */
    fun getEmptyWeatherDataMessage(): String {
        return "현재 날씨 데이터를 조회할 수 없습니다."
    }
}