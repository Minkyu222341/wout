package com.wout.outfit.entity

import com.wout.common.entity.BaseTimeEntity
import com.wout.common.util.JsonUtils
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
 * description    : 아웃핏 추천 엔티티 (Jackson 기반 안전한 JSON 처리)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
 * 2025-06-03        MinKyu Park       언더바 제거, 불변성 강화, QueryDSL 친화적으로 개선
 * 2025-06-04        MinKyu Park       JsonUtils 도입으로 안전한 JSON 처리
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
         * 추천 생성 팩토리 메서드 - JsonUtils 활용
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
                topItems = JsonUtils.toJson(topItems),                     // ✅ JsonUtils 사용
                bottomCategory = bottomCategory,
                bottomItems = JsonUtils.toJson(bottomItems),               // ✅ JsonUtils 사용
                outerCategory = outerCategory,
                outerItems = JsonUtils.toJsonOrNull(outerItems),           // ✅ JsonUtils 사용 (null 처리)
                accessoryItems = JsonUtils.toJsonOrNull(accessoryItems),   // ✅ JsonUtils 사용 (null 처리)
                recommendationReason = recommendationReason,
                personalTip = personalTip,
                confidenceScore = confidenceScore
            )
        }

        /**
         * 추천 신뢰도 점수 계산
         */
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
    }

    // ===== 도메인 로직 (JsonUtils 기반 안전한 JSON 처리) =====

    /**
     * JSON 문자열을 리스트로 변환 - JsonUtils 사용
     */
    fun getTopItemsList(): List<String> {
        return JsonUtils.fromJson(topItems)                        // ✅ JsonUtils 사용
    }

    fun getBottomItemsList(): List<String> {
        return JsonUtils.fromJson(bottomItems)                     // ✅ JsonUtils 사용
    }

    fun getOuterItemsList(): List<String> {
        return outerItems?.let { JsonUtils.fromJson(it) } ?: emptyList()
    }

    fun getAccessoryItemsList(): List<String> {
        return accessoryItems?.let { JsonUtils.fromJson(it) } ?: emptyList()
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
     * 실제로 외투가 추천되었는지 확인 - JsonUtils 활용
     */
    fun hasOuterwearRecommendation(): Boolean {
        return outerCategory != null && !JsonUtils.isEmptyJsonArray(outerItems)
    }

    /**
     * 소품 추천이 있는지 확인 - JsonUtils 활용
     */
    fun hasAccessoryRecommendation(): Boolean {
        return !JsonUtils.isEmptyJsonArray(accessoryItems)
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
     * 전체 추천 아이템 개수 반환 - JsonUtils 활용
     */
    fun getTotalItemCount(): Int {
        return JsonUtils.getItemCount(topItems) +
                JsonUtils.getItemCount(bottomItems) +
                JsonUtils.getItemCount(outerItems) +
                JsonUtils.getItemCount(accessoryItems)
    }

    /**
     * 추천 요약 메시지 생성 - JsonUtils 활용
     */
    fun generateSummaryMessage(): String {
        val topItemsText = getTopItemsList().take(2).joinToString(", ")
        val bottomItemsText = JsonUtils.getFirstItem(bottomItems) ?: ""
        val outerText = JsonUtils.getFirstItem(outerItems)?.let { " + $it" } ?: ""

        return "$topItemsText + $bottomItemsText$outerText"
    }

    /**
     * 상세 추천 메시지 생성 - JsonUtils 활용
     */
    fun generateDetailedMessage(): String {
        val parts = mutableListOf<String>()

        // 상의
        val topItemsText = JsonUtils.toReadableString(topItems)
        if (topItemsText.isNotBlank()) {
            parts.add("상의: $topItemsText")
        }

        // 하의
        val bottomItemsText = JsonUtils.toReadableString(bottomItems)
        if (bottomItemsText.isNotBlank()) {
            parts.add("하의: $bottomItemsText")
        }

        // 외투
        val outerItemsText = JsonUtils.toReadableString(outerItems)
        if (outerItemsText.isNotBlank()) {
            parts.add("외투: $outerItemsText")
        }

        // 소품
        val accessoryItemsText = JsonUtils.toReadableString(accessoryItems)
        if (accessoryItemsText.isNotBlank()) {
            parts.add("소품: $accessoryItemsText")
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
    fun calculateOverallAppropriatenessScore(): Double {
        val temperatureScore = calculateTemperatureAppropriateness()
        val completenessScore = if (isCompleteRecommendation()) 1.0 else 0.8
        val confidenceScore = this.confidenceScore / 100.0

        return (temperatureScore * 0.5 + completenessScore * 0.3 + confidenceScore * 0.2)
    }

    /**
     * 레이어링 복잡도 계산 - JsonUtils 활용
     */
    fun getLayeringComplexity(): Int {
        var complexity = 1 // 기본 상의+하의

        if (hasOuterwearRecommendation()) complexity += 1
        if (hasAccessoryRecommendation()) complexity += 1
        if (JsonUtils.getItemCount(topItems) > 1) complexity += 1  // 상의 레이어링

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
        val score = calculateOverallAppropriatenessScore()
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

    /**
     * 특정 아이템 타입이 포함되어 있는지 확인 - JsonUtils 활용
     */
    fun containsItemType(itemName: String): Boolean {
        return JsonUtils.containsItem(topItems, itemName) ||
                JsonUtils.containsItem(bottomItems, itemName) ||
                JsonUtils.containsItem(outerItems, itemName) ||
                JsonUtils.containsItem(accessoryItems, itemName)
    }

    /**
     * 모든 추천 아이템을 하나의 리스트로 반환 - JsonUtils 활용
     */
    fun getAllRecommendedItems(): List<String> {
        return JsonUtils.mergeJsonArrays(topItems, bottomItems, outerItems, accessoryItems)
    }

    /**
     * 추천 아이템 개수 요약 반환
     */
    fun getItemCountSummary(): Map<String, Int> {
        return mapOf(
            "상의" to JsonUtils.getItemCount(topItems),
            "하의" to JsonUtils.getItemCount(bottomItems),
            "외투" to JsonUtils.getItemCount(outerItems),
            "소품" to JsonUtils.getItemCount(accessoryItems)
        )
    }
}