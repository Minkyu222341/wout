package com.wout.member.entity

import com.wout.common.entity.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * packageName    : com.wout.member.entity
 * fileName       : Member
 * author         : MinKyu Park
 * date           : 2025-06-01
 * description    : 사용자 기본 정보 엔티티 (deviceId 기반 익명 사용자)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-05-27        MinKyu Park       최초 생성
 * 2025-06-01        MinKyu Park       개발 가이드에 맞게 수정 (Builder 제거, 팩토리 메서드 사용)
 */
@Entity
class Member private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("회원 고유 ID")
    val id: Long = 0L,

    @Column(name = "device_id", nullable = false, unique = true, length = 255)
    @Comment("기기 고유 식별자 (익명 사용자 구분용)")
    val deviceId: String,

    @Column(name = "nickname", length = 50)
    @Comment("사용자가 설정한 별명")
    private var _nickname: String? = null,

    @Column(name = "default_latitude")
    @Comment("기본 위도 (사용자 설정 지역)")
    private var _defaultLatitude: Double? = null,

    @Column(name = "default_longitude")
    @Comment("기본 경도 (사용자 설정 지역)")
    private var _defaultLongitude: Double? = null,

    @Column(name = "default_city_name", length = 100)
    @Comment("기본 지역명 (예: 부산 해운대구)")
    private var _defaultCityName: String? = null,

    @Column(name = "is_active", nullable = false)
    @Comment("활성 상태 (탈퇴 시 false)")
    private var _isActive: Boolean = true
) : BaseTimeEntity() {

    // 읽기 전용 프로퍼티
    val nickname: String? get() = _nickname
    val defaultLatitude: Double? get() = _defaultLatitude
    val defaultLongitude: Double? get() = _defaultLongitude
    val defaultCityName: String? get() = _defaultCityName
    val isActive: Boolean get() = _isActive

    // JPA용 기본 생성자
    protected constructor() : this(deviceId = "")

    companion object {
        /**
         * 새 회원 생성 (최소 정보)
         */
        fun create(deviceId: String): Member {
            require(deviceId.isNotBlank()) { "DeviceId는 필수값입니다" }
            return Member(deviceId = deviceId)
        }

        /**
         * MemberCreateRequest로부터 생성
         */
        fun from(request: com.wout.member.dto.request.MemberCreateRequest): Member {
            require(request.deviceId.isNotBlank()) { "DeviceId는 필수값입니다" }

            // 위도/경도 유효성 검사
            validateLocation(request.latitude, request.longitude)

            return Member(
                deviceId = request.deviceId,
                _nickname = request.nickname,
                _defaultLatitude = request.latitude,
                _defaultLongitude = request.longitude,
                _defaultCityName = request.cityName
            )
        }

        /**
         * 완전한 정보로 회원 생성 (테스트용)
         */
        fun createWithFullInfo(
            deviceId: String,
            nickname: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            cityName: String? = null
        ): Member {
            require(deviceId.isNotBlank()) { "DeviceId는 필수값입니다" }
            validateLocation(latitude, longitude)

            return Member(
                deviceId = deviceId,
                _nickname = nickname,
                _defaultLatitude = latitude,
                _defaultLongitude = longitude,
                _defaultCityName = cityName
            )
        }

        /**
         * 위치 정보 유효성 검증
         */
        private fun validateLocation(latitude: Double?, longitude: Double?) {
            if (latitude != null || longitude != null) {
                require(latitude != null && longitude != null) {
                    "위도와 경도는 함께 설정되어야 합니다"
                }
                require(latitude in -90.0..90.0) { "위도는 -90~90 범위여야 합니다" }
                require(longitude in -180.0..180.0) { "경도는 -180~180 범위여야 합니다" }
            }
        }
    }

    // ===== 도메인 로직 (비즈니스 규칙) =====

    /**
     * 닉네임 변경
     */
    fun updateNickname(newNickname: String?): Member {
        newNickname?.let {
            require(it.length <= 50) { "닉네임은 50자를 초과할 수 없습니다" }
        }
        return copy(_nickname = newNickname)
    }

    /**
     * 기본 위치 변경
     */
    fun updateDefaultLocation(
        latitude: Double?,
        longitude: Double?,
        cityName: String?
    ): Member {
        // 위도/경도 유효성 검사
        validateLocation(latitude, longitude)

        return copy(
            _defaultLatitude = latitude,
            _defaultLongitude = longitude,
            _defaultCityName = cityName
        )
    }

    /**
     * 회원 비활성화 (탈퇴)
     */
    fun deactivate(): Member {
        return copy(_isActive = false)
    }

    // ===== 질의 메서드 =====

    /**
     * 기본 위치가 설정되어 있는지 확인
     */
    fun hasDefaultLocation(): Boolean {
        return _defaultLatitude != null && _defaultLongitude != null
    }

    /**
     * 활성 회원인지 확인
     */
    fun isActiveMember(): Boolean {
        return _isActive
    }

    /**
     * 닉네임이 설정되어 있는지 확인
     */
    fun hasNickname(): Boolean {
        return !_nickname.isNullOrBlank()
    }

    /**
     * 위치 정보를 Pair로 반환
     */
    fun getLocationPair(): Pair<Double, Double>? {
        return if (hasDefaultLocation()) {
            Pair(_defaultLatitude!!, _defaultLongitude!!)
        } else null
    }

    // ===== 불변성 보장을 위한 copy 메서드 =====

    private fun copy(
        _nickname: String? = this._nickname,
        _defaultLatitude: Double? = this._defaultLatitude,
        _defaultLongitude: Double? = this._defaultLongitude,
        _defaultCityName: String? = this._defaultCityName,
        _isActive: Boolean = this._isActive
    ): Member {
        return Member(
            id = this.id,
            deviceId = this.deviceId,
            _nickname = _nickname,
            _defaultLatitude = _defaultLatitude,
            _defaultLongitude = _defaultLongitude,
            _defaultCityName = _defaultCityName,
            _isActive = _isActive
        )
    }
}