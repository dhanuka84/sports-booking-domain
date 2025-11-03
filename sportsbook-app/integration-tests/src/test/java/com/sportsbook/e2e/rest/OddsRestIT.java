package com.sportsbook.e2e.rest;

import com.sportsbook.e2e.support.E2eTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = { com.sportsbook.OddsServiceApplication.class })
public class OddsRestIT extends E2eTest {

  @LocalServerPort int port;

  @BeforeEach void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void getMarkets_returnsSeedData() {
    given()
      .when().get("/api/odds/markets")
      .then().statusCode(200)
      .body("size()", greaterThan(0))
      .body("[0].sport", notNullValue());
  }
}
