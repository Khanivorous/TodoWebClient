package com.khanivorous.todowebclient

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.khanivorous.todowebclient.service.TodoClientImpl
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.IOException

class WireMockClientImplTest {

    companion object {

        private val wireMockServer: WireMockServer = WireMockServer(0)
        private var serviceUnderTest: TodoClientImpl? = null

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            wireMockServer.start()
            serviceUnderTest = TodoClientImpl(WebClient.create(wireMockServer.baseUrl()))
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            wireMockServer.shutdown()
        }
    }

    @Throws(IOException::class)
    @AfterEach
    fun tearDown() {
        wireMockServer.resetAll()
    }

    @Throws(IOException::class)
    @Test
    fun todoById() {

        val dummyResponse: String = this::class.java.classLoader.getResource("todo/todoResponse.json")!!.readText()

        wireMockServer.stubFor(
            get(urlEqualTo("/posts/1"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(dummyResponse)
                )
        )

        val response = serviceUnderTest!!.getTodoById(1)

        assertEquals(1, response!!.id)
        assertEquals("test body id 1", response.body)

    }

    @Throws(IOException::class)
    @Test
    fun todoByIdError() {

        wireMockServer.stubFor(
            get(urlEqualTo("/posts/2"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                )
        )

        assertThrows(WebClientResponseException::class.java) { serviceUnderTest!!.getTodoById(2) }

    }


}