package com.mercadolivro.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()

        .info(Info()
            .title("Mercado Livro")
            .version("1.0")
            .description("API do mercado livro"))
}