package com.wout.schedule.job

import com.wout.schedule.service.OpenWeatherApiService
import com.wout.weather.entity.KoreanMajorCity
import com.wout.weather.repository.WeatherDataRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * packageName    : com.wout.schedule.job
 * fileName       : WeatherSchedulerJob
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : 주요 도시 날씨 데이터 스케줄링 수집
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       OpenWeatherApiService 연동
 */
@Component
class WeatherSchedulerJob(
    private val openWeatherApiService: OpenWeatherApiService,
    private val weatherDataRepository: WeatherDataRepository
) {

    private val log = LoggerFactory.getLogger(WeatherSchedulerJob::class.java)

    /**
     * 30분마다 주요 도시 날씨 데이터 수집
     * 23개 도시 × 48회/일 = 1,104회/일 (무료 API 범위)
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 1800000ms
    fun collectMajorCityWeatherData() {
        log.info("=== 주요 도시 날씨 데이터 수집 시작 ===")
        val startTime = System.currentTimeMillis()
        var successCount = 0
        var failCount = 0

        KoreanMajorCity.entries.forEach { city ->
            try {
                log.debug("${city.cityName} 날씨 데이터 수집 시작")

                // OpenWeather API 호출
                val weatherData = openWeatherApiService.fetchWeatherData(
                    latitude = city.latitude,
                    longitude = city.longitude,
                    cityName = city.cityName
                )

                // DB 저장
                weatherDataRepository.save(weatherData)

                successCount++
                log.debug("${city.cityName} 날씨 데이터 수집 완료")

                // API 호출 간격 조절 (Rate Limit 방지)
                Thread.sleep(100) // 0.1초 대기

            } catch (e: Exception) {
                failCount++
                log.error("${city.cityName} 날씨 데이터 수집 실패: ${e.message}", e)
            }
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        log.info("=== 주요 도시 날씨 데이터 수집 완료 ===")
        log.info("성공: ${successCount}개, 실패: ${failCount}개, 소요시간: ${duration}ms")
    }

    /**
     * 애플리케이션 시작 시 즉시 한 번 실행 (개발/테스트용)
     */
    @Scheduled(initialDelay = 30000, fixedRate = Long.MAX_VALUE) // 30초 후 1회 실행
    fun initialWeatherDataCollection() {
        log.info("애플리케이션 시작 - 초기 날씨 데이터 수집")
        collectMajorCityWeatherData()
    }

    /**
     * 매일 새벽 2시에 오래된 날씨 데이터 정리 (7일 이상 된 데이터 삭제)
     */
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    fun cleanupOldWeatherData() {
        try {
            log.info("오래된 날씨 데이터 정리 시작")
            val cutoffDate = LocalDateTime.now().minusDays(7)
            val deletedCount = weatherDataRepository.deleteByCreatedAtBefore(cutoffDate)
            log.info("오래된 날씨 데이터 정리 완료: ${deletedCount}건 삭제")
        } catch (e: Exception) {
            log.error("오래된 날씨 데이터 정리 실패: ${e.message}", e)
        }
    }
}