package com.vtex.hackathon.graphql.model

typealias ProductId = String
data class Product (
    val id: ProductId? = null,
    val category: CategoryId? = null,
    val asset: Asset? = null,
    val description: String? = null,
    val price: Long? = null
)