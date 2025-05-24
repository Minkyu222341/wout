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
 */
enum class KoreanMajorCity(
    val cityName: String,
    val englishName: String,
    val latitude: Double,
    val longitude: Double,
    val population: Int,
    val region: String
) {
    // 특별시/광역시 (8개)
    SEOUL("서울특별시", "Seoul", 37.5665, 126.9780, 9407540, "수도권"),
    BUSAN("부산광역시", "Busan", 35.1796, 129.0756, 3320276, "영남권"),
    INCHEON("인천광역시", "Incheon", 37.4563, 126.7052, 2964820, "수도권"),
    DAEGU("대구광역시", "Daegu", 35.8714, 128.6014, 2365619, "영남권"),
    DAEJEON("대전광역시", "Daejeon", 36.3504, 127.3845, 1446749, "호서권"),
    GWANGJU("광주광역시", "Gwangju", 35.1595, 126.8526, 1432049, "호남권"),
    ULSAN("울산광역시", "Ulsan", 35.5384, 129.3114, 1111371, "영남권"),
    SEJONG("세종특별자치시", "Sejong", 36.4800, 127.2890, 342328, "호서권"),

    // 수도권 주요 시 (9개)
    SUWON("수원특례시", "Suwon", 37.2636, 127.0286, 1190368, "수도권"),
    YONGIN("용인특례시", "Yongin", 37.2410, 127.1776, 1075570, "수도권"),
    GOYANG("고양특례시", "Goyang", 37.6584, 126.8320, 1075202, "수도권"),
    SEONGNAM("성남시", "Seongnam", 37.4449, 127.1388, 920362, "수도권"),
    HWASEONG("화성특례시", "Hwaseong", 37.1997, 126.8312, 907958, "수도권"),
    BUCHEON("부천시", "Bucheon", 37.4989, 126.7831, 791263, "수도권"),
    NAMYANGJU("남양주시", "Namyangju", 37.6360, 127.2164, 737366, "수도권"),
    ANSAN("안산시", "Ansan", 37.3219, 126.8309, 643044, "수도권"),
    ANYANG("안양시", "Anyang", 37.3943, 126.9568, 565392, "수도권"),

    // 영남권 주요 시 (3개)
    CHANGWON("창원특례시", "Changwon", 35.2280, 128.6811, 1003737, "영남권"),
    GIMHAE("김해시", "Gimhae", 35.2342, 128.8895, 542713, "영남권"),
    POHANG("포항시", "Pohang", 36.0190, 129.3435, 506494, "영남권"),

    // 충청권 주요 시 (2개)
    CHEONGJU("청주시", "Cheongju", 36.6424, 127.4890, 849388, "호서권"),
    CHEONAN("천안시", "Cheonan", 36.8151, 127.1139, 657821, "호서권"),

    // 호남권 주요 시 (1개)
    JEONJU("전주시", "Jeonju", 35.8242, 127.1480, 652458, "호남권");


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
         * 한글 이름으로 도시 찾기
         */
        fun findByCityName(cityName: String): KoreanMajorCity? {
            return KoreanMajorCity.entries.find { it.cityName.contains(cityName) }
        }
    }
}