package com.movile.playkids.graphql.model

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 2/7/19
 */

typealias PersonId = String

data class Person(
    val id: PersonId? = null,
    val name: String? = null,
    val age: Int? = null,
    val children: List<Child> = emptyList()
)