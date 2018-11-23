package com.devops.pokemon.service

import com.devops.pokemon.model.dto.PokemonDto
import com.devops.pokemon.model.WrappedResponse
import com.devops.pokemon.model.hal.PageDto
import org.springframework.http.ResponseEntity

interface PokemonService{

    fun createPokemon(pokemonDto : PokemonDto) : ResponseEntity<WrappedResponse<PageDto<PokemonDto>>>

    fun batchCreatePokemon(pokemons: List<PokemonDto>): ResponseEntity<Void>

    fun findBy(name: String?, num: String?, type: String?, weaknesses: String?, offset: Int, limit: Int): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>>

    fun find(num: String?): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>>

    fun update(num: String?, dto: PokemonDto): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>>

    fun patch(num: String?, jsonBody: String): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>>

    fun delete(num: String?): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>>
}