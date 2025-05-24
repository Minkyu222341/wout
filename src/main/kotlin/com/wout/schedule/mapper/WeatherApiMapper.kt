package com.wout.schedule.mapper

import com.wout.infra.openweather.dto.response.AirPollutionResponse
import com.wout.infra.openweather.dto.response.OpenWeatherResponse
import com.wout.weather.entity.WeatherData
import org.springframework.stereotype.Component

/**
 * packageName    : com.wout.schedule.mapper
 * fileName       : WeatherApiMapper
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : OpenWeather API 응답을 WeatherData 엔티티로 변환하는 매퍼
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 */
@Component
class WeatherApiMapper {

    /**
     * OpenWeather API 응답을 WeatherData 엔티티로 변환
     */
    fun toWeatherData(
        latitude: Double,
        longitude: Double,
        cityName: String,
        weatherResponse: OpenWeatherResponse,
        airResponse: AirPollutionResponse
    ): WeatherData {
        return WeatherData.create(
            latitude = latitude,
            longitude = longitude,
            cityName = cityName,
            temperature = weatherResponse.main.temp,
            feelsLike = weatherResponse.main.feelsLike,
            tempMin = weatherResponse.main.tempMin,
            tempMax = weatherResponse.main.tempMax,
            humidity = weatherResponse.main.humidity,
            pressure = weatherResponse.main.pressure,
            seaLevelPressure = weatherResponse.main.seaLevel,
            groundLevelPressure = weatherResponse.main.grndLevel,
            windSpeed = weatherResponse.wind.speed,
            windDirection = weatherResponse.wind.deg,
            windGust = weatherResponse.wind.gust,
            weatherMain = weatherResponse.weather[0].main,
            weatherDescription = weatherResponse.weather[0].description,
            weatherIcon = weatherResponse.weather[0].icon,
            visibility = weatherResponse.visibility,
            cloudiness = weatherResponse.clouds.all,
            rain1h = weatherResponse.rain?.oneHour,
            rain3h = weatherResponse.rain?.threeHours,
            snow1h = weatherResponse.snow?.oneHour,
            snow3h = weatherResponse.snow?.threeHours,
            pm25 = airResponse.list[0].components.pm25,
            pm10 = airResponse.list[0].components.pm10,
            co = airResponse.list[0].components.co,
            no2 = airResponse.list[0].components.no2,
            ozone = airResponse.list[0].components.o3,
            so2 = airResponse.list[0].components.so2,
            sunrise = weatherResponse.sys.sunrise,
            sunset = weatherResponse.sys.sunset,
            dataTimestamp = weatherResponse.dt
        )
    }
}