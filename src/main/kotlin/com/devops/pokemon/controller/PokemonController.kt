package com.devops.pokemon.controller

import com.codahale.metrics.MetricRegistry
import com.devops.pokemon.model.dto.PokemonDto
import com.devops.pokemon.model.WrappedResponse
import com.devops.pokemon.model.hal.PageDto
import com.devops.pokemon.service.PokemonService
//import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Deprecated


const val BASE_JSON = "application/json;charset=UTF-8"

//@Api(value = "/pokemon", description = "Handling of creating and retrieving pokemon's")
@RequestMapping(
        path = ["/pokemon"],
        produces = [BASE_JSON]
)
@RestController
class PokemonController {


    @Value("\${server.servlet.context-path}")
    private lateinit var contextPath : String

    @Autowired
    private lateinit var pokemonService: PokemonService

    @Autowired
    private lateinit var registry: MetricRegistry


    //@ApiOperation("Get pokemon's")
    @GetMapping
    fun getAll(//@ApiParam("Name of the Pokemon")
               @RequestParam("name", required = false)
               name : String?,

               //@ApiParam("Pokedex number of the Pokemon (has to be 3 characters long f.eks: 005)")
               @RequestParam("num", required = false)
               num: String?,

               //@ApiParam("Pokemon type")
               @RequestParam("type", required = false)
               type: String?,

               //@ApiParam("Pokemon weaknesses")
               @RequestParam("weaknesses", required = false)
               weaknesses: String?,

               //@ApiParam("Offset in the list of pokemon's")
               @RequestParam("offset", defaultValue = "0")
               offset: Int,

               //@ApiParam("Limit of pokemons in a single retrieved page")
               @RequestParam("limit", defaultValue = "10")
               limit: Int
    ): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        val counter = registry.counter("Get Pokemons")
        counter.inc()
        val timer = registry.timer("Time to retrive pokemons with limit amount: $limit")
        val context = timer.time()
        try {
            return pokemonService.findBy(name, num, type, weaknesses, offset, limit)
        } finally {
            context.stop()
        }
    }

    //@ApiOperation("Create new Pokemon")
    @PostMapping
    fun post(@RequestBody pokemonDto: PokemonDto): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        registry.meter("Creating Pokemon").mark()
        return pokemonService.createPokemon(pokemonDto)
    }

   // @ApiOperation("Get single pokemon by the id")
    @GetMapping(path = ["/{id}"])
    fun get(//@ApiParam("The id of the pokemon")
            @PathVariable("id")
            num: String?) : ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
       registry.meter("Getting pokemon with id: $num").mark()
       return pokemonService.find(num)
    }

    //@ApiOperation("Update the whole pokemon with new information")
    @PutMapping(path = ["/{id}"])
    fun update(//@ApiParam("The id of the pokemon")
               @PathVariable("id")
               id: String?,
               //@ApiParam("Pokemon data")
               @RequestBody
               pokemonDto: PokemonDto): ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        return pokemonService.update(id, pokemonDto)
    }

    //@ApiOperation("Update part of the pokemon's data")
    @PatchMapping(path = ["/{id}"])
    fun patch(//@ApiParam("The id of the pokemon")
            @PathVariable("id")
            num: String?,
            //@ApiParam("The partial patch")
            @RequestBody
            jsonPatch: String) : ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        return pokemonService.patch(num, jsonPatch)
    }

    //@ApiOperation("Delete a Pokemon by id")
    @DeleteMapping(path = ["/{id}"])
    fun delete(//@ApiParam("The id of the pokemon")
                @PathVariable("id")
                num: String?) : ResponseEntity<WrappedResponse<PageDto<PokemonDto>>> {
        return pokemonService.delete(num)
    }

    //@ApiOperation("Get single pokemon by the id")
    //@ApiResponses(ApiResponse(code = 301, message = "Deprecated URI, moved permanently."))
    @GetMapping(path = ["/id/{id}"])
    @Deprecated
    fun deprecatedFindById(
            //@ApiParam("Id of a Pokemon")
            @PathVariable("id")
            num: String?) : ResponseEntity<PokemonDto> {
        return ResponseEntity.status(301)
                .location(UriComponentsBuilder.fromUriString("$contextPath/pokemon/$num")
                        .build()
                        .toUri())
                .build()
    }
}