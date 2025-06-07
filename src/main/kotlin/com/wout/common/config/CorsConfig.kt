package com.wout.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * packageName    : com.wout.common.config
 * fileName       : CorsConfig
 * author         : MinKyu Park
 * date           : 2025-06-03
 * description    : 개발용 CORS 전체 허용 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-06-03        MinKyu Park       최초 생성 (개발용)
 */
@Configuration
class CorsConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")  // 모든 경로
            .allowedOriginPatterns("*")  // 모든 도메인 허용
            .allowedMethods("*")  // 모든 HTTP 메서드 허용
            .allowedHeaders("*")  // 모든 헤더 허용
            .allowCredentials(true)  // 쿠키/인증 정보 허용
            .maxAge(3600)
    }
}