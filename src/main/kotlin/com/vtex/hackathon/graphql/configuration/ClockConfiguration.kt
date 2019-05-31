package com.vtex.hackathon.graphql.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneOffset

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
@Configuration
open class ClockConfiguration {
    @Bean
    open fun clock() = Clock.system(ZoneOffset.UTC)!!
}