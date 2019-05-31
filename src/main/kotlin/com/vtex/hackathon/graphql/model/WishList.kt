package com.vtex.hackathon.graphql.model

import java.time.Instant

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
typealias WishListId = Long
data class WishList (
    val id: WishListId? = null,
    val customerId: CustomerId? = null,
    val createdAt: Instant? = null,
    val active: Boolean,
    val products: List<FullItem> = emptyList()
)

data class FullItem(
    val id: ProductId? = null,
    val category: CategoryId? = null,
    val asset: Asset? = null,
    val description: String? = null,
    val price: Long? = null,
    val quantity: Long? = null
)

data class WishListItem (
    val productId: ProductId? = null,
    val wishListId: WishListId? = null,
    val quantity: Long = 1
)