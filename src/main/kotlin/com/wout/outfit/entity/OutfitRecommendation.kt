package com.wout.outfit.entity

import com.wout.common.entity.BaseTimeEntity
import com.wout.outfit.entity.enums.BottomCategory
import com.wout.outfit.entity.enums.OuterCategory
import com.wout.outfit.entity.enums.TopCategory
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * packageName    : com.wout.outfit.entity
 * fileName       : OutfitRecommendation
 * author         : MinKyu Park
 * date           : 2025-06-02
 * description    : 아웃핏 추천 엔티티 (언더바 제거, QueryDSL 최적화)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       언더바 제거, 불변성 강화, QueryDSL 친화적으로 개선
 */
@Entity
class OutfitRecommendation private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("아웃핏 추천 ID")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    @Comment("회원 ID")
    val memberId: Long,

    @Column(name = "weather_data_id", nullable = false)
    @Comment("날씨 데이터 ID")
    val weatherDataId: Long,

    // === 기본 날씨 조건 ===
    @Column(name = "temperature", nullable = false)
    @Comment("기온")
    val temperature: Double,

    @Column(name = "feels_like_temperature", nullable = false)
    @Comment("개인화된 체감온도")
    val feelsLikeTemperature: Double,

    @Column(name = "weather_score", nullable = false)
    @Comment("개인화된 날씨 점수")
    val weatherScore: Int,

    // === 추천 아웃핏 구성 ===
    @Enumerated(EnumType.STRING)
    @Column(name = "top_category", nullable = false)
    @Comment("상의 카테고리")
    val topCategory: TopCategory,

    @Column(name = "top_items", nullable = false, length = 500)
    @Comment("상의 아이템들 (JSON 문자열)")
    val topItems: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "bottom_category", nullable = false)
    @Comment("하의 카테고리")
    val bottomCategory: BottomCategory,

    @Column(name = "bottom_items", nullable = false, length = 500)
    @Comment("하의 아이템들 (JSON 문자열)")
    val bottomItems: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "outer_category")
    @Comment("외투 카테고리")
    val outerCategory: OuterCategory? = null,

    @Column(name = "outer_items", length = 500)
    @Comment("외투 아이템들 (JSON 문자열)")
    val outerItems: String? = null,

    @Column(name = "accessory_items", length = 500)
    @Comment("소품 아이템들 (JSON 문자열)")
    val accessoryItems: String? = null,

    // === 추천 근거 및 팁 ===
    @Column(name = "recommendation_reason", nullable = false, length = 1000)
    @Comment("추천 근거")
    val recommendationReason: String,

    @Column(name = "personal_tip", length = 500)
    @Comment("개인 맞춤 팁")
    val personalTip: String? = null,

    @Column(name = "confidence_score", nullable = false)
    @Comment("추천 신뢰도 점수 (0-100)")
    val confidenceScore: Int = 85
) : BaseTimeEntity() {

    protected constructor() : this(
        memberId = 0L, weatherDataId = 0L, temperature = 0.0,
        feelsLikeTemperature = 0.0, weatherScore = 0, topCategory = TopCategory.T_SHIRT,
        topItems = "[]", bottomCategory = BottomCategory.JEANS, bottomItems = "[]",
        recommendationReason = ""
    )

    companion object {
        /**
         * 추천 생성 팩토리 메서드
         */
        fun create(
            memberId: Long,
            weatherDataId: Long,
            temperature: Double,
            feelsLikeTemperature: Double,
            weatherScore: Int,
            topCategory: TopCategory,
            topItems: List<String>,
            bottomCategory: BottomCategory,
            bottomItems: List<String>,
            outerCategory: OuterCategory? = null,
            outerItems: List<String>? = null,
            accessoryItems: List<String>? = null,
            recommendationReason: String,
            personalTip: String? = null
        ): OutfitRecommendation {
            require(memberId > 0) { "Member ID는 양수여야 합니다" }
            require(weatherDataId > 0) { "Weather Data ID는 양수여야 합니다" }
            require(temperature >= -50 && temperature <= 60) { "기온은 -50~60도 범위여야 합니다" }
            require(weatherScore in 0..100) { "날씨 점수는 0-100 범위여야 합니다" }
            require(topItems.isNotEmpty()) { "상의 아이템은 최소 1개 이상이어야 합니다" }
            require(bottomItems.isNotEmpty()) { "하의 아이템은 최소 1개 이상이어야 합니다" }
            require(recommendationReason.isNotBlank()) { "추천 근거는 필수입니다" }

            val confidenceScore = calculateConfidenceScore(weatherScore, feelsLikeTemperature, temperature)

            return OutfitRecommendation(
                memberId = memberId,
                weatherDataId = weatherDataId,
                temperature = temperature,
                feelsLikeTemperature = feelsLikeTemperature,
                weatherScore = weatherScore,
                topCategory = topCategory,
                topItems = convertToJson(topItems),
                bottomCategory = bottomCategory,
                bottomItems = convertToJson(bottomItems),
                outerCategory = outerCategory,
                outerItems = outerItems?.let { convertToJson(it) },
                accessoryItems = accessoryItems?.let { convertToJson(it) },
                recommendationReason = recommendationReason,
                personalTip = personalTip,
                confidenceScore = confidenceScore
            )
        }

        private fun calculateConfidenceScore(weatherScore: Int, feelsLike: Double, actual: Double): Int {
            val baseScore = when {
                weatherScore >= 80 -> 95  // 좋은 날씨는 높은 신뢰도
                weatherScore >= 60 -> 85  // 보통 날씨
                weatherScore >= 40 -> 75  // 나쁜 날씨
                else -> 65                // 매우 나쁜 날씨
            }

            // 체감온도와 실제온도 차이가 클수록 신뢰도 감소
            val tempDifference = abs(feelsLike - actual)
            val penaltyScore = (tempDifference * 2).roundToInt()

            return (baseScore - penaltyScore).coerceIn(50, 100)
        }

        private fun convertToJson(items: List<String>): String {
            return items.joinToString(",", "[", "]") { "\"$it\"" }
        }
    }

    // ===== 도메인 로직 (개인 속성 기반) =====

    /**
     * 추천 카테고리가 온도에 적절한지 확인
     */
    fun isAppropriateForTemperature(): Boolean {
        return when {
            feelsLikeTemperature <= 5 -> topCategory in listOf(TopCategory.THICK_SWEATER, TopCategory.HOODIE_THICK)
            feelsLikeTemperature <= 15 -> topCategory in listOf(TopCategory.SWEATER, TopCategory.HOODIE, TopCategory.LONG_SLEEVE)
            feelsLikeTemperature <= 25 -> topCategory in listOf(TopCategory.LONG_SLEEVE, TopCategory.T_SHIRT, TopCategory.LIGHT_SWEATER)
            else -> topCategory in listOf(TopCategory.T_SHIRT, TopCategory.SLEEVELESS, TopCategory.LINEN_SHIRT)
        }
    }

    /**
     * 외투 추천이 필요한 온도인지 확인
     */
    fun needsOuterwear(): Boolean {
        return feelsLikeTemperature <= 20
    }

    /**
     * 실제로 외투가 추천되었는지 확인
     */
    fun hasOuterwearRecommendation(): Boolean {
        return outerCategory != null && !outerItems.isNullOrBlank() && outerItems != "[]"
    }

    /**
     * 소품 추천이 있는지 확인
     */
    fun hasAccessoryRecommendation(): Boolean {
        return !accessoryItems.isNullOrBlank() && accessoryItems != "[]"
    }

    /**
     * 개인 맞춤 팁이 있는지 확인
     */
    fun hasPersonalTip(): Boolean {
        return !personalTip.isNullOrBlank()
    }

    /**
     * 추천 신뢰도가 높은지 확인
     */
    fun isHighConfidence(): Boolean {
        return confidenceScore >= 80
    }

    /**
     * 추천 신뢰도가 낮은지 확인
     */
    fun isLowConfidence(): Boolean {
        return confidenceScore < 70
    }

    /**
     * 완전한 추천인지 확인 (상의+하의+외투+소품 모두 포함)
     */
    fun isCompleteRecommendation(): Boolean {
        val hasBasicItems = getTopItemsList().isNotEmpty() && getBottomItemsList().isNotEmpty()
        val hasOptionalItems = if (needsOuterwear()) hasOuterwearRecommendation() else true
        return hasBasicItems && hasOptionalItems
    }

    /**
     * JSON 문자열을 리스트로 변환
     */
    fun getTopItemsList(): List<String> {
        return parseJsonToList(topItems)
    }

    fun getBottomItemsList(): List<String> {
        return parseJsonToList(bottomItems)
    }

    fun getOuterItemsList(): List<String> {
        return outerItems?.let { parseJsonToList(it) } ?: emptyList()
    }

    fun getAccessoryItemsList(): List<String> {
        return accessoryItems?.let { parseJsonToList(it) } ?: emptyList()
    }

    private fun parseJsonToList(json: String): List<String> {
        return json.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }

    /**
     * 전체 추천 아이템 개수 반환
     */
    fun getTotalItemCount(): Int {
        return getTopItemsList().size +
                getBottomItemsList().size +
                getOuterItemsList().size +
                getAccessoryItemsList().size
    }

    /**
     * 추천 요약 메시지 생성
     */
    fun generateSummaryMessage(): String {
        val topItemsText = getTopItemsList().take(2).joinToString(", ")
        val bottomItemsText = getBottomItemsList().firstOrNull() ?: ""
        val outerText = getOuterItemsList().firstOrNull()?.let { " + $it" } ?: ""

        return "$topItemsText + $bottomItemsText$outerText"
    }

    /**
     * 상세 추천 메시지 생성
     */
    fun generateDetailedMessage(): String {
        val parts = mutableListOf<String>()

        // 상의
        val topItems = getTopItemsList()
        if (topItems.isNotEmpty()) {
            parts.add("상의: ${topItems.joinToString(", ")}")
        }

        // 하의
        val bottomItems = getBottomItemsList()
        if (bottomItems.isNotEmpty()) {
            parts.add("하의: ${bottomItems.joinToString(", ")}")
        }

        // 외투
        val outerItems = getOuterItemsList()
        if (outerItems.isNotEmpty()) {
            parts.add("외투: ${outerItems.joinToString(", ")}")
        }

        // 소품
        val accessoryItems = getAccessoryItemsList()
        if (accessoryItems.isNotEmpty()) {
            parts.add("소품: ${accessoryItems.joinToString(", ")}")
        }

        return parts.joinToString(" | ")
    }

    /**
     * 온도 범위별 적절성 점수 계산
     */
    fun calculateTemperatureAppropriateness(): Double {
        val categoryScore = when {
            feelsLikeTemperature <= 5 && topCategory in listOf(TopCategory.THICK_SWEATER, TopCategory.HOODIE_THICK) -> 1.0
            feelsLikeTemperature in 6.0..15.0 && topCategory in listOf(TopCategory.SWEATER, TopCategory.HOODIE) -> 1.0
            feelsLikeTemperature in 16.0..25.0 && topCategory in listOf(TopCategory.LONG_SLEEVE, TopCategory.T_SHIRT) -> 1.0
            feelsLikeTemperature > 25 && topCategory in listOf(TopCategory.T_SHIRT, TopCategory.SLEEVELESS) -> 1.0
            else -> 0.6  // 약간 부적절하지만 완전히 틀린 것은 아님
        }

        // 외투 필요성 고려
        val outerScore = if (needsOuterwear()) {
            if (hasOuterwearRecommendation()) 1.0 else 0.7
        } else {
            if (hasOuterwearRecommendation()) 0.8 else 1.0  // 불필요한 외투는 약간 감점
        }

        return (categoryScore + outerScore) / 2.0
    }

    /**
     * 종합 적절성 점수 계산 (온도 + 완성도 + 신뢰도)
     */
    fun calculateOverallAppropriatenesScore(): Double {
        val temperatureScore = calculateTemperatureAppropriateness()
        val completenessScore = if (isCompleteRecommendation()) 1.0 else 0.8
        val confidenceScore = this.confidenceScore / 100.0

        return (temperatureScore * 0.5 + completenessScore * 0.3 + confidenceScore * 0.2)
    }

    /**
     * 레이어링 복잡도 계산
     */
    fun getLayeringComplexity(): Int {
        var complexity = 1 // 기본 상의+하의

        if (hasOuterwearRecommendation()) complexity += 1
        if (hasAccessoryRecommendation()) complexity += 1
        if (getTopItemsList().size > 1) complexity += 1  // 상의 레이어링

        return complexity
    }

    /**
     * 계절별 적절성 확인
     */
    fun getSeasonalCategory(): String {
        return when {
            feelsLikeTemperature <= 5 -> "한겨울"
            feelsLikeTemperature <= 15 -> "늦가을/초겨울"
            feelsLikeTemperature <= 25 -> "봄/가을"
            feelsLikeTemperature <= 30 -> "초여름"
            else -> "한여름"
        }
    }

    /**
     * 추천 품질 등급 반환
     */
    fun getQualityGrade(): String {
        val score = calculateOverallAppropriatenesScore()
        return when {
            score >= 0.9 -> "A"
            score >= 0.8 -> "B"
            score >= 0.7 -> "C"
            score >= 0.6 -> "D"
            else -> "F"
        }
    }

    /**
     * 개선 필요 영역 분석
     */
    fun getImprovementAreas(): List<String> {
        val areas = mutableListOf<String>()

        if (!isAppropriateForTemperature()) {
            areas.add("온도 적절성")
        }

        if (needsOuterwear() && !hasOuterwearRecommendation()) {
            areas.add("외투 추천 누락")
        }

        if (isLowConfidence()) {
            areas.add("추천 신뢰도")
        }

        if (!isCompleteRecommendation()) {
            areas.add("추천 완성도")
        }

        return areas
    }
}