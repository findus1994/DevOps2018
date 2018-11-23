package com.devops.pokemon.model

import com.devops.pokemon.model.dto.PokemonDto
import com.devops.pokemon.model.hal.PageDto

class PokemonResponse(
        code: Int? = null,
        data: PageDto<PokemonDto>? = null,
        message: String? = null,
        status: ResponseStatus? = null

) : WrappedResponse<PageDto<PokemonDto>>(code, data, message, status)