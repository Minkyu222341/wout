package com.wout.weather.entity.enums

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * packageName    : com.wout.weather.entity.enums
 * fileName       : KoreanMajorCity
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : 대한민국 주요 도시 목록 (인구 50만 이상)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성 (23개 주요 도시)
 * 25. 5. 25.        MinKyu Park       도시명 매칭 로직 개선, 인구수 필드 제거
 */
enum class KoreanMajorCity(
    val cityName: String,
    val englishName: String,
    val latitude: Double,
    val longitude: Double,
    val region: String
) {
    // 특별시/광역시 (8개)
    SEOUL("서울특별시", "Seoul", 37.5665, 126.9780, "수도권"),
    BUSAN("부산광역시", "Busan", 35.1796, 129.0756, "영남권"),
    INCHEON("인천광역시", "Incheon", 37.4563, 126.7052, "수도권"),
    DAEGU("대구광역시", "Daegu", 35.8714, 128.6014, "영남권"),
    DAEJEON("대전광역시", "Daejeon", 36.3504, 127.3845, "호서권"),
    GWANGJU("광주광역시", "Gwangju", 35.1595, 126.8526, "호남권"),
    ULSAN("울산광역시", "Ulsan", 35.5384, 129.3114, "영남권"),
    SEJONG("세종특별자치시", "Sejong", 36.4800, 127.2890, "호서권"),

    // 수도권 주요 시 (9개)
    SUWON("수원특례시", "Suwon", 37.2636, 127.0286, "수도권"),
    YONGIN("용인특례시", "Yongin", 37.2410, 127.1776, "수도권"),
    GOYANG("고양특례시", "Goyang", 37.6584, 126.8320, "수도권"),
    SEONGNAM("성남시", "Seongnam", 37.4449, 127.1388, "수도권"),
    HWASEONG("화성특례시", "Hwaseong", 37.1997, 126.8312, "수도권"),
    BUCHEON("부천시", "Bucheon", 37.4989, 126.7831, "수도권"),
    NAMYANGJU("남양주시", "Namyangju", 37.6360, 127.2164, "수도권"),
    ANSAN("안산시", "Ansan", 37.3219, 126.8309, "수도권"),
    ANYANG("안양시", "Anyang", 37.3943, 126.9568, "수도권"),

    // 영남권 주요 시 (3개)
    CHANGWON("창원특례시", "Changwon", 35.2280, 128.6811, "영남권"),
    GIMHAE("김해시", "Gimhae", 35.2342, 128.8895, "영남권"),
    POHANG("포항시", "Pohang", 36.0190, 129.3435, "영남권"),

    // 충청권 주요 시 (2개)
    CHEONGJU("청주시", "Cheongju", 36.6424, 127.4890, "호서권"),
    CHEONAN("천안시", "Cheonan", 36.8151, 127.1139, "호서권"),

    // 호남권 주요 시 (1개)
    JEONJU("전주시", "Jeonju", 35.8242, 127.1480, "호남권");


    companion object {
        /**
         * 사용자 위치에서 가장 가까운 도시 찾기
         */
        fun findNearestCity(userLat: Double, userLon: Double): KoreanMajorCity {
            return KoreanMajorCity.entries.toTypedArray().minByOrNull { city ->
                calculateDistance(userLat, userLon, city.latitude, city.longitude)
            } ?: SEOUL  // 기본값은 서울
        }

        /**
         * 두 지점 간의 거리 계산 (Haversine 공식)
         */
        private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadius = 6371.0 // 지구 반지름 (km)

            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)

            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return earthRadius * c
        }

        /**
         * 지역별 도시 목록 조회
         */
        fun getCitiesByRegion(region: String): List<KoreanMajorCity> {
            return KoreanMajorCity.entries.filter { it.region == region }
        }

        /**
         * 영어 이름으로 도시 찾기
         */
        fun findByEnglishName(englishName: String): KoreanMajorCity? {
            return KoreanMajorCity.entries.find { it.englishName.equals(englishName, ignoreCase = true) }
        }

        /**
         * 한글 이름으로 도시 찾기 (정확한 매칭 + 유연한 검색)
         * 1순위: 정확한 일치 (대소문자 구분 없음)
         * 2순위: 도시명에서 '시', '특별시', '광역시' 등을 제거한 핵심 이름 매칭
         */
        fun findByCityName(cityName: String): KoreanMajorCity? {
            val normalizedInput = cityName.trim()

            // 1순위: 정확한 매칭 (대소문자 구분 없음)
            KoreanMajorCity.entries.find {
                it.cityName.equals(normalizedInput, ignoreCase = true)
            }?.let { return it }

            // 2순위: 핵심 도시명 매칭 (행정구역 명칭 제거)
            val inputCore = extractCoreCity(normalizedInput)
            return KoreanMajorCity.entries.find { city ->
                val cityCore = extractCoreCity(city.cityName)
                cityCore.equals(inputCore, ignoreCase = true)
            }
        }

        /**
         * 도시명에서 핵심 이름 추출 (행정구역 명칭 제거)
         * 예: "서울특별시" → "서울", "부산광역시" → "부산", "수원특례시" → "수원"
         */
        private fun extractCoreCity(cityName: String): String {
            return cityName
                .replace("특별시", "")
                .replace("광역시", "")
                .replace("특례시", "")
                .replace("자치시", "")
                .replace("시", "")
                .trim()
        }

        /**
         * 모든 매칭 방식을 시도하는 통합 검색 (개발자용)
         */
        fun searchCity(query: String): List<KoreanMajorCity> {
            val normalizedQuery = query.trim()
            val results = mutableSetOf<KoreanMajorCity>()

            // 정확한 매칭
            findByCityName(normalizedQuery)?.let { results.add(it) }

            // 영어명 매칭
            findByEnglishName(normalizedQuery)?.let { results.add(it) }

            // 부분 매칭 (마지막 수단)
            if (results.isEmpty() && normalizedQuery.length >= 2) {
                KoreanMajorCity.entries.filter { city ->
                    city.cityName.contains(normalizedQuery) ||
                            city.englishName.contains(normalizedQuery, ignoreCase = true)
                }.let { results.addAll(it) }
            }

            return results.toList()
        }
    }
}