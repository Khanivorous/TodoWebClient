package com.khanivorous.todowebclient

import com.khanivorous.todowebclient.service.TodoClientImpl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


class MockWebSeverClientImplTest {

    private val mockWebServer: MockWebServer = MockWebServer()
    private var serviceUnderTest: TodoClientImpl? = null

    @Throws(IOException::class)
    @BeforeEach
    fun setUp() {
        mockWebServer.start()
        serviceUnderTest = TodoClientImpl(WebClient.create(mockWebServer.url("/").toString()))
    }

    @Throws(IOException::class)
    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Throws(IOException::class)
    @Test
    fun todoByIdMockServerEnqueue() {

        val dummyResponse: String = this::class.java.classLoader.getResource("todo/todoResponse.json")!!.readText()

        val bites = dummyResponse.toByteArray(StandardCharsets.UTF_8).size.toLong()

        val mockResponse = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(dummyResponse)
            .throttleBody(bites, 5, TimeUnit.SECONDS) //simulate network response

        mockWebServer.enqueue(mockResponse)

        val response = serviceUnderTest?.getTodoById(1)

        assertEquals(1, response!!.id)
        assertEquals("test body id 1", response.body)

    }


    @Throws(IOException::class)
    @Test
    fun todoByIdMockServerDispatcher() {

        val dummyResponse: String = this::class.java.classLoader.getResource("todo/todoResponse.json")!!.readText()

        val dispatcher: Dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                when (request.path) {
                    "/posts/1" -> return MockResponse().setHeader("Content-Type", "application/json; charset=utf-8")
                        .setBody(dummyResponse)
                    "/posts/2" -> return MockResponse().setHeader("Content-Type", "application/json; charset=utf-8")
                        .setResponseCode(500)
                }
                return MockResponse().setResponseCode(404)
            }
        }

        mockWebServer.dispatcher = dispatcher


        val response = serviceUnderTest?.getTodoById(1)

        assertEquals(1, response!!.id)
        assertEquals("test body id 1", response.body)

        assertThrows(WebClientResponseException::class.java) { serviceUnderTest?.getTodoById(2) }
    }


}