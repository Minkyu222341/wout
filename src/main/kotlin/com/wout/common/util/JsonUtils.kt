package com.wout.common.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory

/**
 * packageName    : com.wout.common.util
 * fileName       : JsonUtils
 * author         : MinKyu Park
 * date           : 25. 6. 4.
 * description    : JSON 변환 유틸리티 클래스 (Jackson 기반 안전한 JSON 처리)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 6. 4.        MinKyu Park       최초 생성
 * 25. 6. 4.        MinKyu Park       특수문자, 이스케이프 문자 안전 처리
 * 25. 6. 4.        MinKyu Park       예외 처리 및 로깅 추가
 */
class JsonUtils {

    companion object {
        private val logger = LoggerFactory.getLogger(JsonUtils::class.java)
        private val objectMapper: ObjectMapper = jacksonObjectMapper()

        /**
         * 문자열 리스트를 JSON 문자열로 변환
         *
         * @param items 변환할 문자열 리스트
         * @return JSON 문자열 (실패 시 빈 배열 "[]")
         *
         * 예시:
         * listOf("니트", "자켓") → "[\"니트\",\"자켓\"]"
         * listOf("니트 \"겨울용\"") → "[\"니트 \\\"겨울용\\\"\"]"
         */
        fun toJson(items: List<String>): String {
            return try {
                objectMapper.writeValueAsString(items)
            } catch (e: Exception) {
                logger.warn("Failed to convert list to JSON: $items", e)
                "[]" // 실패 시 빈 배열
            }
        }

        /**
         * JSON 문자열을 문자열 리스트로 변환
         *
         * @param json 변환할 JSON 문자열
         * @return 문자열 리스트 (실패 시 빈 리스트)
         *
         * 예시:
         * "[\"니트\",\"자켓\"]" → listOf("니트", "자켓")
         * "[\"니트 \\\"겨울용\\\"\"]" → listOf("니트 \"겨울용\"")
         */
        fun fromJson(json: String): List<String> {
            return try {
                if (json.isBlank()) return emptyList()
                objectMapper.readValue(json, object : TypeReference<List<String>>() {})
            } catch (e: Exception) {
                logger.warn("Failed to parse JSON to list: $json", e)
                emptyList() // 실패 시 빈 리스트
            }
        }

        /**
         * JSON 문자열 유효성 검증
         *
         * @param json 검증할 JSON 문자열
         * @return 유효하면 true, 아니면 false
         */
        fun isValidJson(json: String): Boolean {
            return try {
                if (json.isBlank()) return false
                objectMapper.readTree(json)
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * 빈 JSON 배열 문자열인지 확인
         *
         * @param json 확인할 JSON 문자열
         * @return 빈 배열이면 true
         */
        fun isEmptyJsonArray(json: String?): Boolean {
            if (json.isNullOrBlank()) return true
            val trimmed = json.trim()
            return trimmed == "[]" || fromJson(trimmed).isEmpty()
        }

        /**
         * 리스트가 비어있지 않으면 JSON으로 변환, 비어있으면 null 반환
         *
         * @param items 변환할 문자열 리스트
         * @return JSON 문자열 또는 null
         */
        fun toJsonOrNull(items: List<String>?): String? {
            return if (items.isNullOrEmpty()) null else toJson(items)
        }

        /**
         * JSON 문자열에서 아이템 개수 반환
         *
         * @param json JSON 문자열
         * @return 아이템 개수 (파싱 실패 시 0)
         */
        fun getItemCount(json: String?): Int {
            return if (json.isNullOrBlank()) 0 else fromJson(json).size
        }

        /**
         * 여러 JSON 배열을 합쳐서 하나의 리스트로 반환
         *
         * @param jsonArrays 합칠 JSON 배열들
         * @return 합쳐진 문자열 리스트
         */
        fun mergeJsonArrays(vararg jsonArrays: String?): List<String> {
            return jsonArrays
                .filterNotNull()
                .flatMap { fromJson(it) }
                .distinct()
        }

        /**
         * JSON 배열에서 특정 아이템이 포함되어 있는지 확인
         *
         * @param json JSON 문자열
         * @param item 찾을 아이템
         * @return 포함되어 있으면 true
         */
        fun containsItem(json: String?, item: String): Boolean {
            return if (json.isNullOrBlank()) false else fromJson(json).contains(item)
        }

        /**
         * JSON 배열에서 첫 번째 아이템 반환
         *
         * @param json JSON 문자열
         * @return 첫 번째 아이템 (없으면 null)
         */
        fun getFirstItem(json: String?): String? {
            return if (json.isNullOrBlank()) null else fromJson(json).firstOrNull()
        }

        /**
         * JSON 배열을 읽기 쉬운 문자열로 변환
         *
         * @param json JSON 문자열
         * @param separator 구분자 (기본값: ", ")
         * @return 읽기 쉬운 문자열
         *
         * 예시: "[\"니트\",\"자켓\"]" → "니트, 자켓"
         */
        fun toReadableString(json: String?, separator: String = ", "): String {
            return if (json.isNullOrBlank()) "" else fromJson(json).joinToString(separator)
        }
    }
}