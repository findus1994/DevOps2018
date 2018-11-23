package com.devops.pokemon.service

import com.devops.pokemon.model.PokemonResponse
import com.devops.pokemon.model.PokemonResponses
import com.devops.pokemon.model.WrappedResponse
import com.devops.pokemon.model.hal.HalLink
import com.devops.pokemon.model.hal.PageDto
import com.devops.pokemon.model.dto.PokemonDto
import com.devops.pokemon.repository.PokemonRepository
import com.devops.pokemon.util.PokemonConverter.Companion.convertFromDto
import com.devops.pokemon.util.PokemonConverter.Companion.convertFromSimplePokemonDto
import com.devops.pokemon.util.PokemonConverter.Companion.convertToDto
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Throwables
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import javax.validation.ConstraintViolationException
import com.fasterxml.jackson.module.kotlin.*
import org.springframework.web.util.UriComponentsBuilder
import kotlin.streams.toList


@Service("PokemonService")
class PokemonServiceImp : PokemonService {


    @Autowired
    private lateinit var pokemonRepository : PokemonRepository


    override fun createPokemon(pokemonDto : PokemonDto) : ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        if (pokemonDto.num == null || pokemonDto.name == null ||
                pokemonDto.candy_count == null || pokemonDto.egg == null ||
                pokemonDto.img == null || pokemonDto.type == null || pokemonDto.weaknesses == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    PokemonResponse(
                            code = HttpStatus.BAD_REQUEST.value(),
                            message = "One ore more fields was not defined"
                    ).validated()
            )

        val id: Long?

        try {
            id = pokemonRepository.save(convertFromDto(pokemonDto)).id
        } catch (e : Exception) {
            if(Throwables.getRootCause(e) is ConstraintViolationException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        PokemonResponses(
                                code = HttpStatus.BAD_REQUEST.value(),
                                message = "Unable to create Pokemon due to constraint violation in the submitted DTO"
                        ).validated()
                )
            }
            throw e
        }

        val dto = PageDto(
                list = mutableListOf(PokemonDto(id = id.toString())),
                totalSize = 1)

        val uriBuilder = UriComponentsBuilder
                .fromPath("/pokemon/${id.toString()}")

        dto._self = HalLink(uriBuilder.cloneBuilder()
                .build().toString())

        return ResponseEntity.status(HttpStatus.CREATED).body(
                PokemonResponses(
                        code = HttpStatus.CREATED.value(),
                        message = "Pokemon successfully created",
                        data = dto
                ).validated()
        )
    }

    override fun batchCreatePokemon(pokemons: List<PokemonDto>): ResponseEntity<Void> {
        pokemonRepository.saveAll(convertFromDto(pokemons))
        return ResponseEntity.ok().build()
    }

    override fun findBy(name: String?, num: String?, type: String?, weaknesses: String?, offset: Int, limit: Int): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {

        if (offset < 0 || limit < 1){
            return ResponseEntity.status(400).body(
                    PokemonResponses(
                            code = 400,
                            message = "offset has to be a positive number and limit har to be 1 or greater."
                    ).validated()
            )
        }

        val list = if (
                name.isNullOrBlank() && num.isNullOrBlank() &&
                type.isNullOrBlank() && weaknesses.isNullOrBlank()) {
            pokemonRepository.findAll()
        }else if(
                !name.isNullOrBlank() && !num.isNullOrBlank()       ||
                !num.isNullOrBlank() && !type.isNullOrBlank()       ||
                !name.isNullOrBlank() && !type.isNullOrBlank()      ||
                !name.isNullOrBlank() && !weaknesses.isNullOrBlank()||
                !num.isNullOrBlank() && !weaknesses.isNullOrBlank()) {
            return ResponseEntity.status(400).body(
                    PokemonResponses(
                            code = 400,
                            message = "You can´t use these filters together. " +
                                    "You can only use 'types' and 'weaknesses' at the same time, " +
                                    "otherwise 1 filter at a time."
                    ).validated()
            )
        } else if (!name.isNullOrBlank()){
            pokemonRepository.findAllByNameContainingIgnoreCase(name!!)
        } else if (!num.isNullOrBlank()){
            pokemonRepository.findByNum(num!!)
        }else if (!type.isNullOrBlank() && !weaknesses.isNullOrBlank()) {
            pokemonRepository.findByWeaknessesIgnoreCaseAndTypeIgnoreCase(weaknesses!!, type!!)
        } else if (!type.isNullOrBlank()){
            pokemonRepository.findByTypeIgnoreCase(type!!)
        } else {
            pokemonRepository.findByWeaknessesIgnoreCase(weaknesses!!)
        }

        if (offset != 0 && offset >= list.count()){
            return ResponseEntity.status(400).body(
                    PokemonResponses(
                            code = 400,
                            message = "Your offset is larger than the number of elements returned by your request."
                    )
            )
        }

        val convertedList = list.toList()
                .stream()
                .skip(offset.toLong())
                .limit(limit.toLong())
                .map { convertToDto(it) }
                .toList().toMutableList()

        val dto = PageDto<PokemonDto>(convertedList, offset, limit, list.count())

        var uriBuilder = UriComponentsBuilder
                .fromPath("/pokemon")
                .queryParam("limit", limit)

        if (name != null){
            uriBuilder = uriBuilder.queryParam("name", name)
        }
        if (num != null){
            uriBuilder = uriBuilder.queryParam("num", num)
        }

        dto._self = HalLink(uriBuilder.cloneBuilder()
                .queryParam("offset", offset)
                .build().toString())

        if (!convertedList.isEmpty() && offset > 0) {
            dto.previous = HalLink(uriBuilder.cloneBuilder()
                    .queryParam("offset", Math.max(offset - limit, 0))
                    .build().toString())
        }

        if (offset + limit < list.count()) {
            dto.next = HalLink(uriBuilder.cloneBuilder()
                    .queryParam("offset", offset + limit)
                    .build().toString())
        }

        return ResponseEntity.ok(
                PokemonResponses(
                        code = 200,
                        data = dto
                ).validated()
        )
    }

    override fun find(num: String?): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        val id: Long

        try {
            id = num!!.toLong()
        } catch (e: Exception) {
            val message: String = if (num.equals("undefined")){
                "Missing required field: num"
            } else {
                "Invalid num parameter, This should be a numeric string"
            }


            return ResponseEntity.status(400).body(
                    PokemonResponse(
                            code = 400,
                            message = message
                    ).validated()
            )
        }

        val dto = pokemonRepository
                .findById(id)
                .orElse(null) ?: return ResponseEntity
                .status(404)
                .body(
                        PokemonResponse(
                                code = 404,
                                message = "Pokemon with id: $num is not found in our database"
                        ).validated()
        )

        return ResponseEntity.ok(
                PokemonResponse(
                        code = 200,
                        data = PageDto(mutableListOf(convertToDto(dto)))
                ).validated()
        )
    }

    override fun update(num: String?, dto: PokemonDto): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        val id: Long

        try {
            id = num!!.toLong()
        } catch (e: Exception) {
            val message: String = if (num.equals("undefined")){
                "Missing required field: num"
            } else {
                "Invalid num parameter, This should be a numeric string"
            }
            return ResponseEntity.status(400).body(
                    PokemonResponse(
                            code = 400,
                            message = message
                    ).validated()
            )
        }

        if (!pokemonRepository.existsById(id)) {
            return ResponseEntity.status(404).body(
                    PokemonResponse(
                            code = 404,
                            message = "Pokemon with number: $num is not found in our database"
                    ).validated()
            )
        }

        if (dto.num == null || dto.img == null || dto.egg == null
                || dto.candy_count == null || dto.name == null
                || dto.type == null || dto.weaknesses == null) {
            return ResponseEntity.status(400).body(
                    PokemonResponse(
                            code = 400,
                            message = "Your PUT request are missing one ore more of the required field(s)."
                    ).validated()
            )
        }

        val pokemon = pokemonRepository.findById(id).get()

        pokemon.num = dto.num!!
        pokemon.candyCount = dto.candy_count!!
        pokemon.egg = dto.egg!!
        pokemon.img = dto.img!!
        pokemon.name = dto.name!!
        pokemon.weaknesses = dto.weaknesses!!
        pokemon.type = dto.type!!
        pokemon.prevEvolution = convertFromSimplePokemonDto(dto.prev_evolution)
        pokemon.nextEvolution = convertFromSimplePokemonDto(dto.next_evolution)

        pokemonRepository.save(pokemon).id

        return ResponseEntity.status(201).body(
                PokemonResponse(
                        code = 201,
                        data = PageDto(mutableListOf(convertToDto(pokemon)))
                ).validated()
        )
    }

    override fun patch(num: String?, jsonBody: String): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        val id: Long

        try {
            id = num!!.toLong()
        } catch (e: Exception) {
            val message: String = if (num.equals("undefined")){
                "Missing required field: num"
            } else {
                "Invalid num parameter, This should be a numeric string"
            }
            return ResponseEntity.status(400).body(
                    PokemonResponse(
                            code = 400,
                            message = message
                    ).validated()
            )
        }

        if (!pokemonRepository.existsById(id)) {
            return ResponseEntity.status(404).body(
                    PokemonResponse(
                            code = 404,
                            message = "Pokemon with number: $num is not found in our database"
                    ).validated()
            )
        }

        val jackson = ObjectMapper()

        val jsonNode: JsonNode

        try {
            jsonNode = jackson.readValue(jsonBody, JsonNode::class.java)
        } catch (e: Exception) {
            //Invalid JSON data as input
            return ResponseEntity.status(400).body(
                    PokemonResponse(
                            code = 400,
                            message = "Invalid JSON object, this should be a valid JSON containing Pokemon meta data"
                    ).validated()
            )
        }

        if (jsonNode.has("id")) {
            //shouldn't be allowed to modify the counter id
            return ResponseEntity.status(409).body(
                    PokemonResponse(
                            code = 409,
                            message = "Illegal operation. You are not allowed to change the database id of a Pokemon"
                    ).validated()
            )
        }

        val pokemon = pokemonRepository.findById(id).get()

        if (jsonNode.has("num")){
            val num = jsonNode.get("num")
            if (num.isTextual){
                pokemon.num = num.asText()
            } else {
                return jsonFieldErrorMessage("num", "String")
            }
        }

        if (jsonNode.has("name")){
            val name = jsonNode.get("name")
            if (name.isTextual){
                pokemon.name = name.asText()
            } else {
                return jsonFieldErrorMessage("name", "String")
            }
        }

        if (jsonNode.has("img")){
            val img = jsonNode.get("img")
            if (img.isTextual){
                pokemon.img = img.asText()
            } else {
                return jsonFieldErrorMessage("img", "String")
            }
        }

        if (jsonNode.has("candy_count")){
            val candyCount = jsonNode.get("candy_count")
            if (candyCount.isInt){
                pokemon.candyCount = candyCount.asInt()
            } else {
                return jsonFieldErrorMessage("candy_count", "Int")

            }
        }

        if (jsonNode.has("egg")){
            val egg = jsonNode.get("egg")
            if (egg.isTextual){
                pokemon.egg = egg.asText()
            } else {
                return jsonFieldErrorMessage("egg", "String")
            }
        }

        if (jsonNode.has("type")){
            val type = jsonNode.get("type")
            when {
                type.isNull -> pokemon.type = null
                type.isArray -> pokemon.type = type.asIterable().map { it.asText() }.toMutableSet()
                else -> return jsonFieldErrorMessage("type", "Array")
            }
        }

        if (jsonNode.has("weaknesses")){
            val weaknesses = jsonNode.get("weaknesses")
            when {
                weaknesses.isNull -> pokemon.weaknesses = null
                weaknesses.isArray -> pokemon.weaknesses = weaknesses.asIterable().map { it.asText() }.toMutableSet()
                else -> return jsonFieldErrorMessage("weaknesses", "Array")
            }
        }

        if (jsonNode.has("prev_evolution")){
            val prevEvolution = jsonNode.get("prev_evolution")
            when {
                prevEvolution.isNull -> pokemon.prevEvolution = null
                prevEvolution.isArray -> {
                    val mapper = jacksonObjectMapper()
                    val tmp: Set<PokemonDto> = mapper.readValue(prevEvolution.toString())
                    pokemon.prevEvolution = convertFromSimplePokemonDto(tmp)

                }
                else -> return jsonFieldErrorMessage("prev_evolution", "Array")
            }
        }

        if (jsonNode.has("next_evolution")){
            val nextEvolution = jsonNode.get("next_evolution")
            when {
                nextEvolution.isNull -> pokemon.nextEvolution = null
                nextEvolution.isArray -> {
                    val mapper = jacksonObjectMapper()
                    val tmp: Set<PokemonDto> = mapper.readValue(nextEvolution.toString())
                    pokemon.nextEvolution = convertFromSimplePokemonDto(tmp)

                }
                else -> return jsonFieldErrorMessage("next_evolution", "Array")
            }
        }

        pokemonRepository.save(pokemon).id

        return ResponseEntity.status(204).body(
                PokemonResponse(
                        code = 204,
                        data = PageDto(mutableListOf(convertToDto(pokemon)))
                ).validated()
        )
    }

    private fun jsonFieldErrorMessage(field: String, type: String) : ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        return ResponseEntity.status(400).body(
                PokemonResponse(
                        code = 400,
                        message = "Invalid field type of: $field, this should be a valid JSON field of type: $type"
                ).validated()
        )
    }

    override fun delete(num: String?): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        val id: Long

        try {
            id = num!!.toLong()
        } catch (e: Exception) {
            val message: String = if (num.equals("undefined")){
                "Missing required field: num"
            } else {
                "Invalid num parameter, This should be a numeric string"
            }
            return ResponseEntity.status(400).body(
                    PokemonResponse(
                            code = 400,
                            message = message
                    ).validated()
            )
        }

        if (!pokemonRepository.existsById(id)) {
            return ResponseEntity.status(404).body(
                    PokemonResponse(
                            code = 404,
                            message = "Pokemon with number: $num is not found in our database"
                    ).validated()
            )
        }

        pokemonRepository.deleteById(id)
        return ResponseEntity.status(204).body(
                PokemonResponse(
                        code = 204
                ).validated()
        )
    }
}