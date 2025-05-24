package com.wout.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * packageName    : com.wout.common.config
 * fileName       : OpenApiConfig
 * author         : MinKyu Park
 * date           : 25. 5. 21.
 * description    : Swagger(SpringDoc) 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 21.        MinKyu Park       최초 생성
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(apiInfo())
        .servers(listOf(
            Server().url("/").description("Default Server")
        ))
        .components(Components())

    private fun apiInfo() = Info()
        .title("Wout API")
        .description("날씨 기반 아웃핏 추천 서비스 API 문서")
        .version("v1.0.0")
        .contact(
            Contact()
                .name("MinKyu Park")
                .email("your.email@example.com")
        )
        .license(
            License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0")
        )
}