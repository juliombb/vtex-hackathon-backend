package com.vtex.hackathon.graphql.model

import java.time.Instant

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
typealias WishListId = Long
data class WishList (
    val wishListId: WishListId? = null,
    val customerId: CustomerId? = null,
    val createdAt: Instant? = null,
    val active: Boolean,
    val products: List<Product> = emptyList()
)

data class WishListItem (
    val productId: ProductId? = null,
    val wishListId: WishListId? = null
)