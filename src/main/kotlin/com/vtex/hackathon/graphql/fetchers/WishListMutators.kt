package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Product
import com.vtex.hackathon.graphql.model.WishList
import com.vtex.hackathon.graphql.model.WishListId
import com.vtex.hackathon.graphql.model.WishListItem
import com.vtex.hackathon.graphql.util.getLongIfPresent
import com.vtex.hackathon.graphql.util.getStringIfPresent
import graphql.language.Field
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.Clock

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */

@Component
class WishListMutators(
    private val jdbc: NamedParameterJdbcTemplate,
    private val wishListBaseFetcher: DataFetcher<WishList>,
    private val clock: Clock
) {
    val creator: DataFetcher<WishList> =
        DataFetcher { env ->
            val customerId = env.getArgument<String>("customerId").toLong()
            val createdAt = clock.instant()

            val id = jdbc.query(
                INSERT_QUERY,
                mapOf(CUSTOMER_ID to customerId, CREATED_AT to createdAt, ACTIVE to true)
            ) { rs, _ -> rs.getLong(1) }.first()

            WishList(id, customerId, createdAt, active = true)
        }

    val addProduct: DataFetcher<WishList> =
        DataFetcher { env ->
            val wishListId = env.getArgument<String>("wishListId").toLong()
            val productId = env.getArgument<String>("productId")

            updateItem(productId, wishListId)
                ?: insertItem(productId, wishListId)

            wishListBaseFetcher.get(env)
        }

    private fun insertItem(productId: String?, wishListId: Long): WishListItem {
        return jdbc.query(
            INSERT_ITEM_QUERY,
            mapOf(
                PRODUCT_ID to productId,
                WISH_LIST_ID to wishListId
            )
        ) { rs, _ -> rs.toWishListItem() }
            .first()
    }

    private fun updateItem(productId: String?, wishListId: Long): WishListItem? {
        return jdbc.query(
            ADD_ITEM_QUERY,
            mapOf(
                PRODUCT_ID to productId,
                WISH_LIST_ID to wishListId
            )
        ) { rs, _ -> rs.toWishListItem() }
            .firstOrNull()
    }

    private fun extractSelectionFields(environment: DataFetchingEnvironment) =
        environment.fields
            .flatMap { it.selectionSet.selections }
            .mapNotNull { it as? Field }

    private fun ResultSet.toWishListItem() =
        WishListItem(
            productId = getStringIfPresent(PRODUCT_ID),
            wishListId = getLongIfPresent(WISH_LIST_ID),
            quantity = getLong(QUANTITY)
        )

    companion object {
        const val WISH_LIST_TABLE = "wish_list"
        const val ID = "id"
        const val CUSTOMER_ID = "customer_id"
        const val CREATED_AT = "created_at"
        const val ACTIVE = "active"

        val INSERT_QUERY = "INSERT INTO $WISH_LIST_TABLE ($CUSTOMER_ID, $ACTIVE, $CREATED_AT) " +
                "VALUES (:$CUSTOMER_ID, :$ACTIVE, :$CREATED_AT) RETURNING $ID"

        const val WISH_LIST_ITEM = "wish_list_item"
        const val WISH_LIST_ID = "wish_list_id"
        const val PRODUCT_ID = "product_id"
        const val QUANTITY = "quantity"

        val INSERT_ITEM_QUERY = "INSERT INTO $WISH_LIST_ITEM ($WISH_LIST_ID, $PRODUCT_ID, $QUANTITY) " +
                "VALUES ($WISH_LIST_ID, $PRODUCT_ID, 1) RETURNING $WISH_LIST_ID, $PRODUCT_ID, $QUANTITY"

        val ADD_ITEM_QUERY = "UPDATE $WISH_LIST_ITEM SET $QUANTITY = $QUANTITY + 1 " +
                "WHERE $WISH_LIST_ID = :$WISH_LIST_ID AND $PRODUCT_ID = :$PRODUCT_ID " +
                "RETURNING $WISH_LIST_ID, $PRODUCT_ID, $QUANTITY"
    }
}