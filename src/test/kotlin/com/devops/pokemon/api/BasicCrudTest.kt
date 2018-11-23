package com.devops.pokemon.api

import com.devops.pokemon.model.PokemonResponse
import com.devops.pokemon.model.dto.PokemonDto
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import junit.framework.Assert.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.springframework.http.HttpStatus


class BasicCrudTest : TestSetup() {


    @Test
    fun testCleanup() {

        given().get().then()
                .statusCode(200)
                .body("data.list.size()", equalTo(0))

    }

    @Test
    fun testCreateAndGetPokemon() {

        createPokemonWithDefault()

    }

    @Test
    fun testUpdatePokemon() {
        val response = createPokemonWithDefault()

        response.name = response.name + response.num

        val updated = given()
                .contentType(ContentType.JSON)
                .body(response)
                .put("/${response.id}")
                .then()
                .statusCode(201)
                .extract()
                .`as`(PokemonResponse::class.java)
                .data!!.list[0]

        assertNotSame(response.name, updated.name)
        assertEquals(response.num, updated.num)
    }

    @Test
    fun testDelete() {
        val response = createPokemonWithDefault()

        given()
                .contentType(ContentType.JSON)
                .delete("/${response.id}")
                .then()
                .statusCode(204)
    }

    private fun createPokemonWithDefault() : PokemonDto {
        val num = "1001"

        val prev = PokemonDto(num = "1000")
        val next = PokemonDto(num = "1002")

        val dto = PokemonDto(
                num = num,
                name = "Test",
                img = "url.com",
                candy_count = 100,
                egg = "5km",
                prev_evolution = mutableSetOf(prev),
                next_evolution = mutableSetOf(next),
                type = mutableSetOf("Grass"),
                weaknesses = mutableSetOf("Fire"))

        val response = createPokemon(dto)

        assertTrue(response.data!!.list.size == 1)
        assertNotNull(response.data)
        println(response.data!!.list.size)
        assertNotNull(response.data!!.list[0].id!!)
        assertExistInDatabase(response.data!!.list[0])

        return getDtoById(response.data!!.list[0].id!!)

    }

    private fun createPokemon(dto: PokemonDto): PokemonResponse {

        return given()
                .contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .`as`(PokemonResponse::class.java)
    }

    private fun assertExistInDatabase(dto: PokemonDto) {

        val responseDto =
                getDtoById(dto.id!!)

        assertNotNull(responseDto.id)
    }

    private fun getDtoById(id: String): PokemonDto {
        return given().contentType(ContentType.JSON)
                .get("/$id")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .`as`(PokemonResponse::class.java)
                .data!!.list[0]
    }
}