package com.wout.feedback.dto.response

/**
 * packageName    : com.wout.feedback.dto.response
 * fileName       : LearningTrend
 * author         : MinKyu Park
 * date           : 25. 6. 2.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 2.        MinKyu Park       최초 생성
 */

enum class LearningTrend(
    val description: String,
    val emoji: String
) {
    IMPROVING(
        description = "추천이 점점 정확해지고 있어요",
        emoji = "📈"
    ),
    STABLE(
        description = "추천 정확도가 안정적이에요",
        emoji = "📊"
    ),
    DECLINING(
        description = "계절 변화를 반영해서 다시 학습 중이에요",
        emoji = "🔄"
    );

    /**
     * 사용자에게 표시할 메시지 생성
     */
    fun getDisplayMessage(): String = "$emoji $description"
}