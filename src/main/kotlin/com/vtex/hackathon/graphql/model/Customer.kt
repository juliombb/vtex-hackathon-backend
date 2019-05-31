package com.vtex.hackathon.graphql.model

import java.time.Instant

typealias CustomerId = Long
data class Customer (
    val id: CustomerId? = null,
    val name: String? = null,
    val cpf: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val gender: Gender? = null,
    val birthDate: Instant? = null
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER;
}