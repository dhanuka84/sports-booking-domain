package com.sportsbook.e2e.rest

import com.sportsbook.OddsServiceApplication
import com.sportsbook.e2e.support.E2eTest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [OddsServiceApplication::class],
)
class OddsRestIT : E2eTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
    }

    @Test
    fun getMarkets_returnsSeedData() {
        given()
            .`when`().get("/api/odds/markets")
            .then().statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].sport", notNullValue())
    }
}
