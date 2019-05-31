package com.vtex.hackathon.graphql.model

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
data class PurchaseItem (
    val purchaseId: PurchaseId? = null,
    val productId: ProductId? = null,
    val quantity: Long? = null
)