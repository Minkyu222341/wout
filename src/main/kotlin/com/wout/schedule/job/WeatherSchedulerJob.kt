package com.wout.schedule.job

import com.wout.schedule.service.OpenWeatherApiService
import com.wout.weather.entity.WeatherData
import com.wout.weather.entity.enums.KoreanMajorCity
import com.wout.weather.repository.WeatherDataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * packageName    : com.wout.schedule.job
 * fileName       : WeatherSchedulerJob
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : 주요 도시 날씨 데이터 스케줄링 수집 (3초 딜레이 최적화)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 * 25. 5. 24.        MinKyu Park       OpenWeatherApiService 연동
 * 25. 5. 25.        MinKyu Park       3초 딜레이 최적화, API 한도 준수
 */
@Component
class WeatherSchedulerJob(
    private val openWeatherApiService: OpenWeatherApiService,
    private val weatherDataRepository: WeatherDataRepository
) {

    private val log = LoggerFactory.getLogger(WeatherSchedulerJob::class.java)

    /**
     * 60분마다 주요 도시 날씨 데이터 수집 (3초 딜레이 + 배치 인서트 최적화)
     * - 23개 도시 × 3 API calls × 24회/일 = 1,656 calls/일
     * - 도시별 3초 간격 = 분당 20 calls < 60 calls/minute ✅
     * - 월간: 49,680 calls < 1,000,000 calls ✅
     * - 전체 소요 시간: 약 69초 (1분 10초)
     * - DB 최적화: 배치 인서트로 커넥션 1회 사용
     */
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각 (00분 00초)
    @Async
    fun collectMajorCityWeatherData() = runBlocking {
        log.info("=== 주요 도시 날씨 데이터 수집 시작 ===")
        val startTime = System.currentTimeMillis()
        var successCount = 0
        var failCount = 0
        var totalApiCalls = 0

        // 수집된 데이터를 임시 저장할 리스트
        val collectedWeatherData = mutableListOf<WeatherData>()
        val citiesToDelete = mutableListOf<String>()

        // 1단계: API 호출하여 데이터 수집
        KoreanMajorCity.entries.forEach { city ->
            try {
                log.debug("${city.cityName} 날씨 데이터 수집 시작")

                // 전체 날씨 데이터 수집 (3 API calls)
                val weatherData = openWeatherApiService.fetchWeatherData(
                    latitude = city.latitude,
                    longitude = city.longitude,
                    cityName = city.cityName
                )

                // 메모리에 임시 저장
                collectedWeatherData.add(weatherData)
                citiesToDelete.add(city.cityName)
                totalApiCalls += 3 // 날씨 + 대기질 + UV

                successCount++
                log.debug("${city.cityName} 날씨 데이터 수집 완료")

                // 3초 대기 (API Rate Limit 준수)
                delay(3000)

            } catch (e: Exception) {
                failCount++
                log.error("${city.cityName} 날씨 데이터 수집 실패: ${e.message}", e)

                // 실패해도 3초 대기 (다음 도시 처리 안정성)
                delay(3000)
            }
        }

        // 2단계: 배치 삭제 + 배치 인서트
        if (collectedWeatherData.isNotEmpty()) {
            log.info("배치 DB 처리 시작 - 삭제: ${citiesToDelete.size}개, 삽입: ${collectedWeatherData.size}개")

            // 삭제
            val deletedCount = weatherDataRepository.batchDeleteByCityNames(citiesToDelete)
            log.debug("배치 삭제 완료: ${deletedCount}건")

            // 배치 인서트
            val insertedData = weatherDataRepository.batchInsert(collectedWeatherData)
            log.debug("배치 인서트 완료: ${insertedData.size}건")
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        val avgTimePerCity = if (successCount > 0) duration / (successCount + failCount) else 0

        log.info("=== 주요 도시 날씨 데이터 수집 완료 ===")
        log.info("성공: ${successCount}개, 실패: ${failCount}개")
        log.info("총 API 호출 수: ${totalApiCalls}회")
        log.info("소요시간: ${duration}ms (${duration / 1000}초)")
        log.info("도시당 평균 처리시간: ${avgTimePerCity}ms")
        log.info("예상 일일 API 호출: ${totalApiCalls * 24}회 (한도: 1,000,000회/월)")

        // 분당 호출 수 계산
        val callsPerMinute = (totalApiCalls * 60000.0 / duration).toInt()
        log.info("현재 분당 API 호출 수: ${callsPerMinute}회 (한도: 60회/분)")
        log.info("DB 최적화: 배치 처리로 커넥션 사용 최소화")
    }

    /**
     * 애플리케이션 시작 시 즉시 한 번 실행 (개발/테스트용)
     */
    @Scheduled(initialDelay = 30000, fixedRate = Long.MAX_VALUE) // 30초 후 1회 실행
    @Async
    fun initialWeatherDataCollection() {
        log.info("애플리케이션 시작 - 초기 날씨 데이터 수집")
        collectMajorCityWeatherData()
    }

    /**
     * 매일 새벽 2시에 오래된 날씨 데이터 정리 (7일 이상 된 데이터 삭제)
     */
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    @Async
    fun cleanupOldWeatherData() = runBlocking {
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