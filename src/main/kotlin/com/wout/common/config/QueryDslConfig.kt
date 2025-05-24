package com.wout.common.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * packageName    : com.wout.common.config
 * fileName       : QueryDslConfig
 * author         : MinKyu Park
 * date           : 25. 5. 24.
 * description    : QueryDSL 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 5. 24.        MinKyu Park       최초 생성
 */
@Configuration
class QueryDslConfig {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}