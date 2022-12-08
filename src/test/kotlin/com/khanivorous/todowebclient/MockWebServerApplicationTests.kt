package com.khanivorous.todowebclient

import com.khanivorous.todowebclient.model.Todo
import okhttp3.mockwebserver.*
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
import org.springframework.test.web.reactive.server.WebTestClient
import java.io.IOException


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource("classpath:application-test.properties")
class MockWebServerApplicationTests {

    @LocalServerPort
    var localServerPort = 0

    @Autowired
    private lateinit var testClient: WebTestClient

    @BeforeEach
    fun setUp() {
        baseUrl = "http://localhost:$localServerPort"
    }

    companion object {

        var baseUrl: String? = null

        var mockWebServer: MockWebServer? = null

        @JvmStatic
        @DynamicPropertySource
        @Throws(IOException::class)
        fun properties(r: DynamicPropertyRegistry) {
            r.add("port") {
                mockWebServer!!.port
            }
        }


        @JvmStatic
        @BeforeAll
        @Throws(IOException::class)
        fun beforeAll() {
            mockWebServer = MockWebServer()
            mockWebServer!!.start()
        }

        @JvmStatic
        @AfterAll
        @Throws(IOException::class)
        fun afterAll() {
            mockWebServer!!.shutdown()
        }


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

    }

}
