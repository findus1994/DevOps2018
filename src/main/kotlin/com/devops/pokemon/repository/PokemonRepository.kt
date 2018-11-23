package com.devops.pokemon.repository

import com.devops.pokemon.model.entity.Pokemon
//import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
//import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface PokemonRepository : CrudRepository<Pokemon, Long> {

    fun findAllByNameContainingIgnoreCase(name: String): Iterable<Pokemon>

    fun findByNum(num: String): Iterable<Pokemon>

    fun findByTypeIgnoreCase(type: String): Iterable<Pokemon>

    fun findByWeaknessesIgnoreCase(weakness: String): Iterable<Pokemon>

    fun findByWeaknessesIgnoreCaseAndTypeIgnoreCase(weaknesses: String, type: String): Iterable<Pokemon>
}