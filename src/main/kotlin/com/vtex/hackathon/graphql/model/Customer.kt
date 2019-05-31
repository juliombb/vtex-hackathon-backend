package com.vtex.hackathon.graphql.model

data class Customer (
    val id: Int? = null,
    val name: String? = null,
    val cpf: String? = null,
    val gender: String? = null,
    val birthDate: Instant? = null
)