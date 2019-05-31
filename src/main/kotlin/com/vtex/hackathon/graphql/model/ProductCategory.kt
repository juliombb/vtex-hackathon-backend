package com.vtex.hackathon.graphql.model

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */

typealias CategoryId = String
data class ProductCategory (
    val id: CategoryId? = null,
    val description: String? = null
)