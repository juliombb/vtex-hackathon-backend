package com.movile.playkids.graphql.model

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 2/7/19
 */
typealias ChildId = String

data class Child (
    val id: ChildId? = null,
    val name: String? = null,
    val age: Int? = null,
    val parentId: PersonId? = null,
    val favoriteCartoonId: Int? = null
)