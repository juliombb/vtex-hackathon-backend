package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.FullItem
import com.vtex.hackathon.graphql.model.Product
import com.vtex.hackathon.graphql.model.WishList
import com.vtex.hackathon.graphql.model.WishListId
import com.vtex.hackathon.graphql.util.*
import graphql.language.Field
import graphql.schema.DataFetcher
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
@Component
class WishListFetchers(private val jdbc: NamedParameterJdbcTemplate) {

    @Bean
    fun wishListBaseFetcher() = wishListsById

    val wishListsById: DataFetcher<WishList?> =
        DataFetcher { environment ->
            val id = if (environment.containsArgument("id")) {
                environment.getArgument<String>("id").toLong()
            } else {
                environment.getArgument<String>("wishListId").toLong()
            }

            val parentFields = environment.extractSelectionFields()
            val wishListFields = (
                    parentFields
                        .map { it.name.toSnakeCase() }
                        .filter { it != "products" } + ID
                    ).toSet()

            val result = jdbc.query(
                FIND_WISH_LIST_QUERY.format(wishListFields.joinToString(", ")), mapOf(ID to id)
            ) { resultSet, _ -> resultSet.toWishList() }
                .firstOrNull()

            parentFields.find { it.name == "products" }
                ?.let { field ->
                    result?.copy(products = findProducts(result.id!!, field))
                }
                ?: result
        }

    val wishListsByCustomerId: DataFetcher<List<WishList?>> =
        DataFetcher { environment ->
            val customerId = environment.getArgument<String>("customerId").toLong()
            val active = environment.getArgument<Boolean>("active")

            val parentFields = environment.extractSelectionFields()
            val wishListFields = (
                    parentFields
                        .map { it.name.toSnakeCase() }
                        .filter { it != "products" } + ID
                    ).toSet()

            val results = jdbc.query(
                FIND_WISH_LISTS_QUERY.format(
                    wishListFields.joinToString(", ")),
                mapOf(CUSTOMER_ID to customerId, ACTIVE to active)
            ) { resultSet, _ -> resultSet.toWishList() }

            parentFields.find { it.name == "products" }
                ?.let { field ->
                    results.map { it.copy(products = findProducts(it.id!!, field)) }
                }
                ?: results
        }


    private fun findProducts(wishListId: WishListId, params: Field): List<FullItem> {
        val subFields = params.selectionSet.selections.mapNotNull { it as? Field }.map { it.name.toSnakeCase() }

        val query = FIND_PRODUCTS_QUERY.format(if (subFields.isEmpty()) "*" else subFields.joinToString(", "))

        return jdbc.query(query, mapOf(WISH_LIST_ID to wishListId)) { rs, _ ->
            rs.toFullItem()
        }
    }

    private fun ResultSet.toWishList(): WishList {
        return WishList(
            id = getLongIfPresent(ID),
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
        val FIND_WISH_LISTS_QUERY = "SELECT %s FROM wish_list WHERE $CUSTOMER_ID = :$CUSTOMER_ID " +
                "AND $ACTIVE = :$ACTIVE"

        val FIND_PRODUCTS_QUERY = "SELECT %s FROM $PRODUCT_TABLE INNER JOIN $WISH_LIST_ITEM " +
                "ON $ID = $PRODUCT_ID AND $QUANTITY > 0 AND $WISH_LIST_ID = :$WISH_LIST_ID"
    }
}