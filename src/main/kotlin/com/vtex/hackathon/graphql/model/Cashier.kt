package com.vtex.hackathon.graphql.model

data class CashBox(
    val id: Long? = null,
    val marketId: Long? = null,
    val status: CashBoxStatus? = null
)

enum class CashBoxStatus {
    AVAILABLE,
    IN_USE,
    CLOSED
}