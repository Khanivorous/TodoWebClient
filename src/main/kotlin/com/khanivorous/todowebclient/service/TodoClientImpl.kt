package com.khanivorous.todowebclient.service

import com.khanivorous.todowebclient.model.Todo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class TodoClientImpl @Autowired constructor(webClient: WebClient) : TodoClient {

    private val webClient : WebClient

    init {
        this.webClient = webClient
    }

    override fun getTodoById(id: Int): Todo? {
        return webClient.get().uri("/posts/$id").accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(Todo::class.java)
            .block()
    }


}