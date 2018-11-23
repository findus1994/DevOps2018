package com.devops.pokemon.util

import com.devops.pokemon.model.dto.PokemonDto
import com.devops.pokemon.service.PokemonService
import com.google.gson.reflect.TypeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.FileReader



@Component
class DefaultData {


    @Autowired
    private lateinit var pokemonService: PokemonService

    @PostConstruct
    fun initializeDefault() {
        //val reader = JsonReader(FileReader("pokemons.json"))
        //val pokemons: List<PokemonDto> = Gson().fromJson(reader, object : TypeToken<List<PokemonDto>>() {}.type)



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

        val nun2 = "1004"

        val prev2 = PokemonDto(num = "1003")
        val next2 = PokemonDto(num = "1004")

        val dto2 = PokemonDto(
                num = nun2,
                name = "Test 2",
                img = "url2.com",
                candy_count = 50,
                egg = "2km",
                prev_evolution = mutableSetOf(prev2),
                next_evolution = mutableSetOf(next2),
                type = mutableSetOf("Water"),
                weaknesses = mutableSetOf("Grass"))

        val pokemons = mutableListOf(dto, dto2)

        println("=== List from JSON ===")
        pokemons.forEach { println(it) }

        pokemonService.batchCreatePokemon(pokemons)


    }
}