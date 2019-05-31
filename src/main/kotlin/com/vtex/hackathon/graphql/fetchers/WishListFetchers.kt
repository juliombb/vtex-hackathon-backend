package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Product
import com.vtex.hackathon.graphql.model.WishList
import com.vtex.hackathon.graphql.model.WishListId
import com.vtex.hackathon.graphql.util.*
import graphql.language.Field
import graphql.schema.DataFetcher
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
class WishListFetchers(private val jdbc: NamedParameterJdbcTemplate) {
    val wishListsByCustomerId: DataFetcher<List<WishList?>> =
        DataFetcher { environment ->
            val customerId = environment.getArgument<String>("customerId").toLong()

            val parentFields = environment.extractSelectionFields()
            val wishListFields = parentFields.map { it.name }.filter { it != "products" }



            jdbc.query(
                FIND_WISH_LISTS_QUERY.format(parentFields), mapOf(CUSTOMER_ID to customerId)
            ) { resultSet, _ -> resultSet.toWishList() }
        }


    private fun findProducts(wishListId: WishListId, params: Field): List<Product> {
        val subFields = params.selectionSet.selections.mapNotNull { it as? Field }.map { it.name }

        val query = FIND_PRODUCTS_QUERY.format(if (subFields.isEmpty()) "*" else subFields.joinToString(", "))

        return jdbc.query(query, mapOf(WISH_LIST_ID to wishListId)) { rs, _ ->
            rs.toProduct()
        }
    }

    private fun ResultSet.toWishList(): WishList {
        return WishList(
            wishListId = getLongIfPresent(ID),
            customerId = getLongIfPresent(CUSTOMER_ID),
            createdAt = getInstantIfPresent(CREATED_AT),
            active = getBooleanIfPresent(ACTIVE) ?: true
        )
    }


    companion object {

        const val WISH_LIST_ITEM = "wish_list_item"
        const val ID = "id"
        const val CUSTOMER_ID = "customer_id"
        const val WISH_LIST_ID = "wish_list_id"
        const val CREATED_AT = "created_at"
        const val ACTIVE = "active"
        const val PRODUCT_ID = "product_id"
        const val QUANTITY = "quantity"

        const val PRODUCT_TABLE = "product"

        val FIND_WISH_LIST_QUERY = "SELECT %s FROM wish_list WHERE $ID = :$ID"
        val FIND_WISH_LISTS_QUERY = "SELECT %s FROM wish_list WHERE $CUSTOMER_ID = :$CUSTOMER_ID"

        val FIND_PRODUCTS_QUERY = "SELECT %s FROM $PRODUCT_TABLE WHERE $ID in (" +
                "SELECT $PRODUCT_ID FROM $WISH_LIST_ITEM WHERE $WISH_LIST_ID = :$WISH_LIST_ID " +
                "AND $QUANTITY >= 0)"
    }
}