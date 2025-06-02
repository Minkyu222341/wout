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
 * description    : 아웃핏 추천 엔티티 (실용적 DDD 적용)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-02        MinKyu Park       최초 생성
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
    private var _topCategory: TopCategory,

    @Column(name = "top_items", nullable = false, length = 500)
    @Comment("상의 아이템들 (JSON 문자열)")
    private var _topItems: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "bottom_category", nullable = false)
    @Comment("하의 카테고리")
    private var _bottomCategory: BottomCategory,

    @Column(name = "bottom_items", nullable = false, length = 500)
    @Comment("하의 아이템들 (JSON 문자열)")
    private var _bottomItems: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "outer_category")
    @Comment("외투 카테고리")
    private var _outerCategory: OuterCategory? = null,

    @Column(name = "outer_items", length = 500)
    @Comment("외투 아이템들 (JSON 문자열)")
    private var _outerItems: String? = null,

    @Column(name = "accessory_items", length = 500)
    @Comment("소품 아이템들 (JSON 문자열)")
    private var _accessoryItems: String? = null,

    // === 추천 근거 및 팁 ===
    @Column(name = "recommendation_reason", nullable = false, length = 1000)
    @Comment("추천 근거")
    private var _recommendationReason: String,

    @Column(name = "personal_tip", length = 500)
    @Comment("개인 맞춤 팁")
    private var _personalTip: String? = null,

    @Column(name = "confidence_score", nullable = false)
    @Comment("추천 신뢰도 점수 (0-100)")
    private var _confidenceScore: Int = 85
) : BaseTimeEntity() {

    // 읽기 전용 프로퍼티
    val topCategory: TopCategory get() = _topCategory
    val topItems: String get() = _topItems
    val bottomCategory: BottomCategory get() = _bottomCategory
    val bottomItems: String get() = _bottomItems
    val outerCategory: OuterCategory? get() = _outerCategory
    val outerItems: String? get() = _outerItems
    val accessoryItems: String? get() = _accessoryItems
    val recommendationReason: String get() = _recommendationReason
    val personalTip: String? get() = _personalTip
    val confidenceScore: Int get() = _confidenceScore

    protected constructor() : this(
        memberId = 0L, weatherDataId = 0L, temperature = 0.0,
        feelsLikeTemperature = 0.0, weatherScore = 0, _topCategory = TopCategory.T_SHIRT,
        _topItems = "[]", _bottomCategory = BottomCategory.JEANS, _bottomItems = "[]",
        _recommendationReason = ""
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

            val confidenceScore = calculateConfidenceScore(weatherScore, feelsLikeTemperature, temperature)

            return OutfitRecommendation(
                memberId = memberId,
                weatherDataId = weatherDataId,
                temperature = temperature,
                feelsLikeTemperature = feelsLikeTemperature,
                weatherScore = weatherScore,
                _topCategory = topCategory,
                _topItems = convertToJson(topItems),
                _bottomCategory = bottomCategory,
                _bottomItems = convertToJson(bottomItems),
                _outerCategory = outerCategory,
                _outerItems = outerItems?.let { convertToJson(it) },
                _accessoryItems = accessoryItems?.let { convertToJson(it) },
                _recommendationReason = recommendationReason,
                _personalTip = personalTip,
                _confidenceScore = confidenceScore
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
     * 추천 카테고리가 적절한지 확인
     */
    fun isAppropriateForTemperature(): Boolean {
        return when {
            feelsLikeTemperature <= 5 -> _topCategory in listOf(TopCategory.THICK_SWEATER, TopCategory.HOODIE_THICK)
            feelsLikeTemperature <= 15 -> _topCategory in listOf(TopCategory.SWEATER, TopCategory.HOODIE, TopCategory.LONG_SLEEVE)
            feelsLikeTemperature <= 25 -> _topCategory in listOf(TopCategory.LONG_SLEEVE, TopCategory.T_SHIRT, TopCategory.LIGHT_SWEATER)
            else -> _topCategory in listOf(TopCategory.T_SHIRT, TopCategory.SLEEVELESS, TopCategory.LINEN_SHIRT)
        }
    }

    /**
     * 외투 추천이 필요한지 확인
     */
    fun needsOuterwear(): Boolean {
        return feelsLikeTemperature <= 20 || _outerCategory != null
    }

    /**
     * 소품 추천이 있는지 확인
     */
    fun hasAccessoryRecommendation(): Boolean {
        return !_accessoryItems.isNullOrBlank() && _accessoryItems != "[]"
    }

    /**
     * 개인 맞춤 팁이 있는지 확인
     */
    fun hasPersonalTip(): Boolean {
        return !_personalTip.isNullOrBlank()
    }

    /**
     * 추천 신뢰도가 높은지 확인
     */
    fun isHighConfidence(): Boolean {
        return _confidenceScore >= 80
    }

    /**
     * JSON 문자열을 리스트로 변환
     */
    fun getTopItemsList(): List<String> {
        return parseJsonToList(_topItems)
    }

    fun getBottomItemsList(): List<String> {
        return parseJsonToList(_bottomItems)
    }

    fun getOuterItemsList(): List<String>? {
        return _outerItems?.let { parseJsonToList(it) }
    }

    fun getAccessoryItemsList(): List<String>? {
        return _accessoryItems?.let { parseJsonToList(it) }
    }

    private fun parseJsonToList(json: String): List<String> {
        return json.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }

    /**
     * 추천 요약 메시지 생성
     */
    fun generateSummaryMessage(): String {
        val topItemsText = getTopItemsList().take(2).joinToString(", ")
        val bottomItemsText = getBottomItemsList().firstOrNull() ?: ""
        val outerText = getOuterItemsList()?.firstOrNull()?.let { " + $it" } ?: ""

        return "$topItemsText + $bottomItemsText$outerText"
    }

    /**
     * 온도 범위별 적절성 점수 계산
     */
    fun calculateTemperatureAppropriateness(): Double {
        val categoryScore = when {
            feelsLikeTemperature <= 5 && _topCategory in listOf(TopCategory.THICK_SWEATER, TopCategory.HOODIE_THICK) -> 1.0
            feelsLikeTemperature in 6.0..15.0 && _topCategory in listOf(TopCategory.SWEATER, TopCategory.HOODIE) -> 1.0
            feelsLikeTemperature in 16.0..25.0 && _topCategory in listOf(TopCategory.LONG_SLEEVE, TopCategory.T_SHIRT) -> 1.0
            feelsLikeTemperature > 25 && _topCategory in listOf(TopCategory.T_SHIRT, TopCategory.SLEEVELESS) -> 1.0
            else -> 0.6  // 약간 부적절하지만 완전히 틀린 것은 아님
        }

        return categoryScore
    }
}