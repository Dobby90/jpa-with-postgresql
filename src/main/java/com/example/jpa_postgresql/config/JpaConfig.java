package com.example.jpa_postgresql.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

/**
 * <pre>
 * com.example.jpa_postgresql.config
 * └ JpaConfig
 *
 *
 * </pre>
 *
 * @author : hycho
 * @date : 2022-11-30
 **/
@Configuration
@RequiredArgsConstructor
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.example.jpa_postgresql.repository"})
public class JpaConfig {

    // DataSource Config
    @Bean
    @Qualifier("postgresql_jpa")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/demo");
        dataSource.setUsername("dobby");
        dataSource.setPassword("1234");
        return dataSource;
    }

    // JPA Transaction Config
    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("postgresql_jpa") DataSource dataSource) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setDataSource(dataSource);
        return jpaTransactionManager;
    }

    // QueryDsl Config
    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }
}
