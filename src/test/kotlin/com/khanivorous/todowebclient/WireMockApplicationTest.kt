package com.khanivorous.todowebclient

import app.getxray.xray.junit.customjunitxml.annotations.Requirement
import app.getxray.xray.junit.customjunitxml.annotations.XrayTest
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.khanivorous.todowebclient.model.Todo
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
@Tag("wiremockapplication")
@TestPropertySource("classpath:application-test.properties")
class WireMockApplicationTest {

    @LocalServerPort
    var localServerPort = 0

    @Autowired
    private lateinit var testClient: WebTestClient


    @BeforeEach
    fun setUp() {
        baseUrl = "http://localhost:$localServerPort"
    }

    @AfterEach
    fun afterEach() {
        wireMockServer!!.resetAll()
    }

    companion object {

        var baseUrl: String? = null

        @JvmStatic
        var wireMockServer: WireMockServer? = WireMockServer(0)

        @JvmStatic
        @BeforeAll
        @Throws(IOException::class)
        fun beforeAll() {
            wireMockServer!!.start()
        }

        @JvmStatic
        @AfterAll
        @Throws(IOException::class)
        fun afterAll() {
            wireMockServer!!.shutdown()
        }

        @JvmStatic
        @DynamicPropertySource
        @Throws(IOException::class)
        fun properties(r: DynamicPropertyRegistry) {
            r.add("port") {
                wireMockServer!!.port()
            }
        }

    }

    @XrayTest(
        key = "KHAN-1",
        summary = "Get Todo by id",
        description = "This gets the Todo response and checks the id matches"
    )
    @Requirement("KHAN-45", "KHAN-46")
    @Throws(IOException::class)
    @Test
    fun todoById() {

        println("wiremock port is " + wireMockServer!!.port())
        println("local server port = $localServerPort")

        val dummyResponse: String = this::class.java.classLoader.getResource("todo/todoResponse.json")!!.readText()

        wireMockServer!!.stubFor(
            get(urlEqualTo("/posts/1"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(dummyResponse)
                )
        )

        val response = testClient.get()
            .uri("${baseUrl}/todo/1")
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

    @XrayTest(
        key = "KHAN-2",
        summary = "Get Todo by id error",
        description = "This checks the error when service returns 500"
    )
    @Requirement("KHAN-45", "KHAN-46")
    @Throws(IOException::class)
    @Test
    fun todoByIdError() {

        wireMockServer!!.stubFor(
            get(urlEqualTo("/posts/2"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                )
        )

        testClient.get()
            .uri("${baseUrl}/todo/2")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .is5xxServerError

    }


}