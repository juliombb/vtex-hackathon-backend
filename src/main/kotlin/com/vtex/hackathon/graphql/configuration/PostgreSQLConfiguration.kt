package com.vtex.hackathon.graphql.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import java.sql.Driver
import java.sql.DriverManager
import javax.sql.DataSource

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 2/7/19
 */

@Configuration
open class PostgreSQLConfiguration {

    @Bean
    open fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()

        dataSource.setDriverClassName("org.postgresql.Driver")
        dataSource.url = System.getenv("JDBC_URL")
        dataSource.username = "gql3"
        dataSource.password = System.getenv("JDBC_PASSWORD")

        return dataSource
    }

    @Bean
    open fun jdbc(): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource())
    }
}