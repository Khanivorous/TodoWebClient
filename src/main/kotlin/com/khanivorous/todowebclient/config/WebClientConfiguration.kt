package com.khanivorous.todowebclient.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {

    @Value("\${todo.baseurl}")
    private val todoBaseUrl: String? = null

    @Bean
    fun webClient(): WebClient? {
        return WebClient.create(todoBaseUrl!!)
    }

}