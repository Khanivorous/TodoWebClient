package com.khanivorous.todowebclient

import com.khanivorous.todowebclient.model.Todo
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.util.TestSocketUtils
import org.springframework.test.web.reactive.server.WebTestClient
import org.junit.jupiter.api.Assertions.assertThrows
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource("classpath:application-test.properties")
@Tag("integration")
class MockWebServerApplicationTest {

    companion object {

        private val mockWebServerPort = TestSocketUtils.findAvailableTcpPort() //Springboot 2.x.x uses SocketUtils not TestSocketUtils

        @JvmStatic
        @DynamicPropertySource
        @Throws(IOException::class)
        fun properties(r: DynamicPropertyRegistry) {
            r.add("port") {
                mockWebServerPort
            }
        }

    }

    @LocalServerPort
    var localServerPort = 0

    @Autowired
    private lateinit var testClient: WebTestClient

    var mockWebServer: MockWebServer? = null

    var baseUrl: String? = null

    @BeforeEach
    fun setUp() {
        baseUrl = "http://localhost:$localServerPort"
        mockWebServer = MockWebServer()
        mockWebServer!!.start(mockWebServerPort)
    }

    @AfterEach
    fun teardown() {
        mockWebServer!!.shutdown()
    }


    @Throws(IOException::class)
    @Test
    fun todoById() {

        println("mock server port = " + mockWebServer!!.port)
        println("local server port = $localServerPort")

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

        mockWebServer!!.dispatcher = dispatcher

        val response = testClient.get()
            .uri("$baseUrl/todo/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Todo::class.java)
            .returnResult()
            .responseBody

        assertEquals(1, response!!.id)
        assertEquals("test body id 1", response.body)

        testClient.get()
            .uri("$baseUrl/todo/2")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .is5xxServerError

    }

    @Throws(IOException::class)
    @Test
    fun todoByIdMockServerEnqueue() {

        println("mock server port = " + mockWebServer!!.port)
        println("local server port = $localServerPort")

        val dummyResponse: String = this::class.java.classLoader.getResource("todo/todoResponse.json")!!.readText()
        val bites = dummyResponse.toByteArray(StandardCharsets.UTF_8).size.toLong()
        println("BITES = $bites")

        val mockResponse = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(dummyResponse)
            .throttleBody(bites, 5, TimeUnit.SECONDS)//simulate network response

        mockWebServer!!.enqueue(mockResponse)

        val response = testClient.get()
            .uri("$baseUrl/todo/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Todo::class.java)
            .returnResult()
            .responseBody

        assertEquals(1, response!!.id)
        assertEquals("test body id 1", response.body)

    }

    @Throws(IOException::class)
    @Test
    fun todoByIdMockServerEnqueueFailTimeout() {

        println("mock server port = " + mockWebServer!!.port)
        println("local server port = $localServerPort")

        val dummyResponse: String = this::class.java.classLoader.getResource("todo/todoResponse.json")!!.readText()
        val bites = dummyResponse.toByteArray(StandardCharsets.UTF_8).size.toLong()
        val lessBites = Math.subtractExact(bites, 1)

        val mockResponse = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(dummyResponse)
            .throttleBody(lessBites, 5, TimeUnit.SECONDS)//simulate network response

        mockWebServer!!.enqueue(mockResponse)

        val exception = assertThrows(IllegalStateException::class.java) {
            testClient.get()
                .uri("$baseUrl/todo/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
        }

        assertEquals("Timeout on blocking read for 5000000000 NANOSECONDS", exception.message)

    }
}