package com.vtex.hackathon.graphql.model

import java.time.Instant

typealias PurchaseId = Long
data class Purchase (
    val id: PurchaseId? = null,
    val customerId: Long? = null,
    val cashBoxId: Long? = null,
    val products: List<Product> = emptyList(),
    val status: PurchaseStatus? = null,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val total: Long? = null
)

enum class PurchaseStatus {
    STARTED,
    CASHBOX_APPROVED,
    FINISHED,
    ABORTED
}