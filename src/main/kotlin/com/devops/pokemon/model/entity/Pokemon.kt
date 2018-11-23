package com.devops.pokemon.model.entity


import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Entity
class Pokemon(

        @get:NotBlank @get:Size(max = 5)
        var num: String? = null,

        @get:NotBlank @get:Size(max = 128)
        var name: String,

        @get:NotBlank @get:Size(max = 2048)
        var img: String? = null,

        var candyCount: Int? = null,

        @get:NotNull
        var egg: String,

        @get:ElementCollection
        @get:NotNull
        var type: MutableSet<String>? = mutableSetOf(),

        @get:ElementCollection
        @get:NotNull
        var weaknesses: MutableSet<String>? = mutableSetOf(),

        @get:ElementCollection
        var prevEvolution: MutableSet<String>? = mutableSetOf(),

        @get:ElementCollection
        var nextEvolution: MutableSet<String>? = mutableSetOf(),

        @get:Id @get:GeneratedValue
        var id: Long? = null

        /*
            Note how we need to explicitly state that id can be null (eg when entity
            is not in sync with database).
            The "= null" is used to provide a default value if caller does not
            provide one.
         */
)