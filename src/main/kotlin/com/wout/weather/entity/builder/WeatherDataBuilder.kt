package com.wout.weather.entity.builder

import com.wout.weather.entity.WeatherData

/**
 * packageName    : com.wout.weather.entity.builder
 * fileName       : WeatherDataBuilder
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : WeatherData 엔티티 생성을 위한 Builder 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 */
class WeatherDataBuilder {

    // 필수 필드
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var cityName: String? = null
    private var temperature: Double? = null
    private var feelsLike: Double? = null
    private var tempMin: Double? = null
    private var tempMax: Double? = null
    private var humidity: Int? = null
    private var pressure: Int? = null
    private var windSpeed: Double? = null
    private var weatherMain: String? = null
    private var weatherDescription: String? = null
    private var weatherIcon: String? = null
    private var cloudiness: Int? = null
    private var dataTimestamp: Long? = null

    // 선택 필드
    private var seaLevelPressure: Int? = null
    private var groundLevelPressure: Int? = null
    private var windDirection: Int? = null
    private var windGust: Double? = null
    private var visibility: Int? = null
    private var rain1h: Double? = null
    private var rain3h: Double? = null
    private var snow1h: Double? = null
    private var snow3h: Double? = null
    private var uvIndex: Double? = null
    private var pm25: Double? = null
    private var pm10: Double? = null
    private var co: Double? = null
    private var no2: Double? = null
    private var ozone: Double? = null
    private var so2: Double? = null
    private var sunrise: Long? = null
    private var sunset: Long? = null

    /**
     * 위치 정보 설정 (필수)
     */
    fun location(latitude: Double, longitude: Double, cityName: String): WeatherDataBuilder {
        this.latitude = latitude
        this.longitude = longitude
        this.cityName = cityName
        return this
    }

    /**
     * 온도 정보 설정 (필수)
     */
    fun temperature(current: Double, feelsLike: Double, min: Double, max: Double): WeatherDataBuilder {
        this.temperature = current
        this.feelsLike = feelsLike
        this.tempMin = min
        this.tempMax = max
        return this
    }

    /**
     * 대기 정보 설정 (필수)
     */
    fun atmosphere(humidity: Int, pressure: Int, seaLevel: Int? = null, groundLevel: Int? = null): WeatherDataBuilder {
        this.humidity = humidity
        this.pressure = pressure
        this.seaLevelPressure = seaLevel
        this.groundLevelPressure = groundLevel
        return this
    }

    /**
     * 바람 정보 설정 (필수)
     */
    fun wind(speed: Double, direction: Int? = null, gust: Double? = null): WeatherDataBuilder {
        this.windSpeed = speed
        this.windDirection = direction
        this.windGust = gust
        return this
    }

    /**
     * 날씨 상태 설정 (필수)
     */
    fun weather(main: String, description: String, icon: String): WeatherDataBuilder {
        this.weatherMain = main
        this.weatherDescription = description
        this.weatherIcon = icon
        return this
    }

    /**
     * 가시거리 설정
     */
    fun visibility(visibility: Int?): WeatherDataBuilder {
        this.visibility = visibility
        return this
    }

    /**
     * 구름량 설정 (필수)
     */
    fun cloudiness(cloudiness: Int): WeatherDataBuilder {
        this.cloudiness = cloudiness
        return this
    }

    /**
     * 강수량 설정
     */
    fun precipitation(oneHour: Double? = null, threeHours: Double? = null): WeatherDataBuilder {
        this.rain1h = oneHour
        this.rain3h = threeHours
        return this
    }

    /**
     * 적설량 설정
     */
    fun snowfall(oneHour: Double? = null, threeHours: Double? = null): WeatherDataBuilder {
        this.snow1h = oneHour
        this.snow3h = threeHours
        return this
    }

    /**
     * UV 지수 설정
     */
    fun uvIndex(uvIndex: Double?): WeatherDataBuilder {
        this.uvIndex = uvIndex
        return this
    }

    /**
     * 대기질 정보 설정
     */
    fun airQuality(
        pm25: Double? = null,
        pm10: Double? = null,
        co: Double? = null,
        no2: Double? = null,
        ozone: Double? = null,
        so2: Double? = null
    ): WeatherDataBuilder {
        this.pm25 = pm25
        this.pm10 = pm10
        this.co = co
        this.no2 = no2
        this.ozone = ozone
        this.so2 = so2
        return this
    }

    /**
     * 일출/일몰 정보 설정
     */
    fun sunInfo(sunrise: Long? = null, sunset: Long? = null): WeatherDataBuilder {
        this.sunrise = sunrise
        this.sunset = sunset
        return this
    }

    /**
     * 데이터 타임스탬프 설정 (필수)
     */
    fun dataTimestamp(timestamp: Long): WeatherDataBuilder {
        this.dataTimestamp = timestamp
        return this
    }

    /**
     * WeatherData 인스턴스 생성
     */
    fun build(): WeatherData {
        // 필수 필드 검증
        requireNotNull(latitude) { "위도는 필수입니다" }
        requireNotNull(longitude) { "경도는 필수입니다" }
        requireNotNull(cityName) { "도시명은 필수입니다" }
        requireNotNull(temperature) { "온도는 필수입니다" }
        requireNotNull(feelsLike) { "체감온도는 필수입니다" }
        requireNotNull(tempMin) { "최저온도는 필수입니다" }
        requireNotNull(tempMax) { "최고온도는 필수입니다" }
        requireNotNull(humidity) { "습도는 필수입니다" }
        requireNotNull(pressure) { "기압은 필수입니다" }
        requireNotNull(windSpeed) { "풍속은 필수입니다" }
        requireNotNull(weatherMain) { "날씨 상태는 필수입니다" }
        requireNotNull(weatherDescription) { "날씨 설명은 필수입니다" }
        requireNotNull(weatherIcon) { "날씨 아이콘은 필수입니다" }
        requireNotNull(cloudiness) { "구름량은 필수입니다" }
        requireNotNull(dataTimestamp) { "데이터 타임스탬프는 필수입니다" }

        return WeatherData(
            latitude = latitude!!,
            longitude = longitude!!,
            cityName = cityName!!,
            temperature = temperature!!,
            feelsLike = feelsLike!!,
            tempMin = tempMin!!,
            tempMax = tempMax!!,
            humidity = humidity!!,
            pressure = pressure!!,
            seaLevelPressure = seaLevelPressure,
            groundLevelPressure = groundLevelPressure,
            windSpeed = windSpeed!!,
            windDirection = windDirection,
            windGust = windGust,
            weatherMain = weatherMain!!,
            weatherDescription = weatherDescription!!,
            weatherIcon = weatherIcon!!,
            visibility = visibility,
            cloudiness = cloudiness!!,
            rain1h = rain1h,
            rain3h = rain3h,
            snow1h = snow1h,
            snow3h = snow3h,
            uvIndex = uvIndex,
            pm25 = pm25,
            pm10 = pm10,
            co = co,
            no2 = no2,
            ozone = ozone,
            so2 = so2,
            sunrise = sunrise,
            sunset = sunset,
            dataTimestamp = dataTimestamp!!
        )
    }
}