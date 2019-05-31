package com.movile.playkids.graphql.configuration

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
        dataSource.url = "jdbc:postgresql://localhost:5432/graphql_2"
        dataSource.username = "gql3"
        dataSource.password = "gqlrocks"

        return dataSource
    }

    @Bean
    open fun jdbc(): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource())
    }
}