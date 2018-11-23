package com.devops.pokemon.api

import com.devops.pokemon.PokemonApplication
import com.devops.pokemon.model.PokemonResponse
import io.restassured.RestAssured
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [(PokemonApplication::class)],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class TestSetup {

    @LocalServerPort
    protected var port = 0

    @Before
    @After
    fun clean(){
        baseURI = "http://localhost"
        RestAssured.port = port
        basePath = "/api/pokemon"
        enableLoggingOfRequestAndResponseIfValidationFails()

        /*
           Here, we read each resource (GET), and then delete them
           one by one (DELETE)
         */
        val response = given().accept(ContentType.JSON)
                .param("limit", 200)
                .get()
                .then()
                .statusCode(200)
                .extract()
                .`as`(PokemonResponse::class.java)

        /*
            Code 204: "No Content". The server has successfully processed the request,
            but the return HTTP response will have no body.
         */

        response.data!!.list.forEach {
            given()
                    .delete("/${ it.id }")
                    .then()
                    .statusCode(204)
        }

        given().get()
                .then()
                .statusCode(200)
                .body("data.list.size()", equalTo(0))
    }


}