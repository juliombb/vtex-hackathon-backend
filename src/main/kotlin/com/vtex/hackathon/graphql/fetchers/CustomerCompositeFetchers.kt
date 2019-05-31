package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Purchase
import com.vtex.hackathon.graphql.model.PurchaseStatus
import com.vtex.hackathon.graphql.model.WishList
import com.vtex.hackathon.graphql.util.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
class CustomerCompositeFetchers(private val jdbc: NamedParameterJdbcTemplate) {


    private fun ResultSet.toWishList(): WishList {
        return WishList(
            wishListId = getLongIfPresent(ID),
            customerId = getLongIfPresent(CUSTOMER_ID),
            createdAt = getInstantIfPresent(CREATED_AT),
            active = getBooleanIfPresent(ACTIVE) ?: true
        )
    }

    private fun ResultSet.toPurchase(): Purchase {
        return Purchase(
            id = getLong(ID),
            customerId = getLong(CUSTOMER_ID),
            cashBoxId = getLong(CASH_BOX_ID),
            status = getEnum<PurchaseStatus>(STATUS),
            startedAt = getInstant(STARTED_AT),
            finishedAt = getInstant(FINISHED_AT),
            total = getLong(TOTAL)
        )
    }

    companion object {
        const val WISH_LIST_TABLE = "wish_list"
        const val PURCHASE_TABLE = "purchase"
        const val PRODUCT_TABLE = "product"
        const val ID = "id"
        const val CUSTOMER_ID = "customer_id"
        const val CREATED_AT = "created_at"
        const val CASH_BOX_ID = "cash_box_id"
        const val PRODUCTS = "products"
        const val STATUS = "status"
        const val ACTIVE = "active"
        const val STARTED_AT = "started_at"
        const val FINISHED_AT = "finished_at"
        const val TOTAL = "total"
    }
}