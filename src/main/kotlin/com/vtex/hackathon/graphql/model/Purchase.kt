package com.vtex.hackathon.graphql.model

data class Purchase (
    val id: Int? = null,
    val customerId: Int? = null,
    val cashierId: Int? = null,
    val date: Instant? = null
)