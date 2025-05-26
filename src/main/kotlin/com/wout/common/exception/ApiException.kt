package com.wout.common.exception

/**
 * packageName    : com.wout.common.exception
 * fileName       : ApiException
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : API 관련 커스텀 예외 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
class ApiException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.message
) : RuntimeException(message)

/**
 * 에러 코드 정의
 */
enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    // 공통 에러
    INTERNAL_SERVER_ERROR(500, "C001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(400, "C002", "유효하지 않은 입력값입니다."),
    RESOURCE_NOT_FOUND(404, "C003", "요청한 리소스를 찾을 수 없습니다."),

    // 날씨 관련 에러
    WEATHER_API_ERROR(500, "W001", "날씨 API 호출 중 오류가 발생했습니다."),
    WEATHER_DATA_NOT_FOUND(404, "W002", "날씨 데이터를 찾을 수 없습니다."),

    // 회원 관련 에러 추가
    MEMBER_NOT_FOUND(404, "M001", "회원을 찾을 수 없습니다."),
    DEVICE_ALREADY_EXISTS(400, "M002", "이미 등록된 디바이스입니다."),
    SENSITIVITY_PROFILE_NOT_FOUND(404, "M003", "민감도 프로필을 찾을 수 없습니다.");
}