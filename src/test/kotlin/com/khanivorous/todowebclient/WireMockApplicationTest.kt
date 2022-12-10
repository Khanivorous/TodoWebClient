package com.khanivorous.todowebclient

import app.getxray.xray.junit.customjunitxml.annotations.Requirement
import app.getxray.xray.junit.customjunitxml.annotations.XrayTest
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.khanivorous.todowebclient.model.Todo
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
import java.io.IOException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Tag("integration")
@TestPropertySource("classpath:application-test.properties")
class WireMockApplicationTest {

    companion object {

        private val wireMockServerPort =
            TestSocketUtils.findAvailableTcpPort() //Springboot 2.x.x uses SocketUtils not TestSocketUtils

        @JvmStatic
        @DynamicPropertySource
        @Throws(IOException::class)
        fun properties(r: DynamicPropertyRegistry) {
            r.add("port") {
                wireMockServerPort
            }
        }

    }

    @LocalServerPort
    var localServerPort = 0

    var baseUrl: String? = null

    @Autowired
    private lateinit var testClient: WebTestClient

    var wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setUp() {
        baseUrl = "http://localhost:$localServerPort"
        wireMockServer = WireMockServer(wireMockServerPort)
        wireMockServer!!.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer!!.shutdown()
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
            WireMock.get(WireMock.urlEqualTo("/posts/1"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(dummyResponse)
                        .withFixedDelay(4000)
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
        key = "KHAN-3",
        summary = "Get Todo by id webclient timeout",
        description = "This gets throws a timeout error"
    )
    @Requirement("KHAN-45", "KHAN-46")
    @Throws(IOException::class)
    @Test
    fun todoByIdTimeout() {

        println("wiremock port is " + wireMockServer!!.port())
        println("local server port = $localServerPort")

        val dummyResponse: String = this::class.java.classLoader.getResource("todo/todoResponse.json")!!.readText()

        wireMockServer!!.stubFor(
            WireMock.get(WireMock.urlEqualTo("/posts/1"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(dummyResponse)
                        .withFixedDelay(5000)
                )
        )

        val exception = assertThrows(IllegalStateException::class.java) {
            testClient.get()
                .uri("${baseUrl}/todo/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
        }

        assertEquals("Timeout on blocking read for 5000000000 NANOSECONDS", exception.message)

    }

    @XrayTest(
        key = "KHAN-4",
        summary = "Get Todo by id error",
        description = "This checks the error when service returns 500"
    )
    @Requirement("KHAN-45", "KHAN-46")
    @Throws(IOException::class)
    @Test
    fun todoByIdError() {

        wireMockServer!!.stubFor(
            WireMock.get(WireMock.urlEqualTo("/posts/2"))
                .willReturn(
                    WireMock.aResponse()
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