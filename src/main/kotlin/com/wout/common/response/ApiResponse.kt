package com.wout.common.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

/**
 * packageName    : com.wout.common.response
 * fileName       : ApiResponse
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : API 응답을 위한 공통 래퍼 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Schema(description = "API 응답 래퍼")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    @Schema(description = "요청 성공 여부", example = "true")
    val success: Boolean,

    @Schema(description = "응답 코드", example = "SUCCESS")
    val code: String? = null,

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    val message: String? = null,

    @Schema(description = "응답 데이터")
    val data: T? = null
) {
    companion object {
        /**
         * 성공 응답 생성
         */
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                code = "SUCCESS",
                message = "요청이 성공적으로 처리되었습니다.",
                data = data
            )
        }

        /**
         * 성공 응답 생성 (데이터 없음)
         */
        fun success(): ApiResponse<Nothing> {
            return ApiResponse(
                success = true,
                code = "SUCCESS",
                message = "요청이 성공적으로 처리되었습니다."
            )
        }

        /**
         * 에러 응답 생성
         */
        fun error(code: String, message: String): ApiResponse<Nothing> {
            return ApiResponse(
                success = false,
                code = code,
                message = message
            )
        }
    }
}