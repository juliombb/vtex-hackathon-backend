package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.FullItem
import com.vtex.hackathon.graphql.model.Purchase
import com.vtex.hackathon.graphql.model.PurchaseId
import com.vtex.hackathon.graphql.model.PurchaseStatus
import com.vtex.hackathon.graphql.util.*
import graphql.language.Field
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
@Component
class PurchaseFetchers(private val jdbc: NamedParameterJdbcTemplate) {

    @Bean
    fun purchaseBaseFetcher() = purchaseById

    val purchaseById: DataFetcher<Purchase?> =
        DataFetcher { environment ->
            val purchaseId =
                if (environment.containsArgument("id")) {
                    environment.getArgument<String>("id").toLong()
                } else {
                    environment.getArgument<String>("purchaseId").toLong()
                }

            val parentFields = extractSelectionFields(environment)

            val fields = (
                    parentFields.map { it.name.toSnakeCase() }.filter { it != "products" } + STATUS + ID
                ).toSet()

            val result = jdbc.query(
                FIND_PURCHASE_QUERY.format(fields.joinToString(", ")), mapOf(ID to purchaseId)
            ) { resultSet, _ -> resultSet.toPurchase() }.firstOrNull()

            parentFields.find { it.name == "products" }
                ?.let {
                    result?.copy(products = findProducts(result.id!!, it))
                }
                ?: result
        }

    val purchasesByCustomerId: DataFetcher<List<Purchase>> =
        DataFetcher { environment ->
            val customerId = environment.getArgument<String>("customerId").toLong()

            val parentFields = extractSelectionFields(environment)

            val fields = (
                parentFields.map { it.name.toSnakeCase() }.filter { it != "products" } + STATUS + ID
            ).toSet()

            val result = jdbc.query(
                FIND_PURCHASES_QUERY.format(fields.joinToString(", ")), mapOf(CUSTOMER_ID to customerId)
            ) { resultSet, _ -> resultSet.toPurchase() }

            parentFields.find { it.name == "products" }
                ?.let { field -> result.map { it.copy(products = findProducts(it.id!!, field)) } }
                ?: result
        }


    private fun findProducts(purchaseId: PurchaseId, params: Field): List<FullItem> {
        val subFields = params.selectionSet.selections.mapNotNull { it as? Field }.map { it.name.toSnakeCase() }

        val query = FIND_PRODUCTS_QUERY.format(if (subFields.isEmpty()) "*" else subFields.joinToString(", "))

        return jdbc.query(query, mapOf(PURCHASE_ID to purchaseId)) { rs, _ ->
            rs.toFullItem()
        }
    }

    private fun extractSelectionFields(environment: DataFetchingEnvironment): List<Field> {
        val parentFields = environment.fields
            .flatMap { it.selectionSet.selections }
            .mapNotNull { it as? Field }
            .filter { it.name != "__typename" }
        return parentFields
    }

    private fun ResultSet.toPurchase(): Purchase {
        return Purchase(
            id = getLongIfPresent(ID),
            customerId = getLongIfPresent(CUSTOMER_ID),
            cashboxId = getLongIfPresent(CASH_BOX_ID),
            status = getEnumIfPresent<PurchaseStatus>(STATUS),
            startedAt = getInstantIfPresent(STARTED_AT),
            finishedAt = getInstantIfPresent(FINISHED_AT),
            total = getLongIfPresent(TOTAL)
        )
    }

    companion object {
        const val PURCHASE_TABLE = "purchase"
        const val ID = "id"
        const val CUSTOMER_ID = "customer_id"
        const val CASH_BOX_ID = "cash_box_id"
        const val STATUS = "status"
        const val STARTED_AT = "started_at"
        const val FINISHED_AT = "finished_at"
        const val TOTAL = "total"

        val FIND_PURCHASE_QUERY = "SELECT %s FROM $PURCHASE_TABLE WHERE $ID = :$ID"
        val FIND_PURCHASES_QUERY = "SELECT %s FROM $PURCHASE_TABLE WHERE $CUSTOMER_ID = :$CUSTOMER_ID"

        const val PRODUCT_ID = "product_id"
        const val PURCHASE_ID = "purchase_id"
        const val QUANTITY = "quantity"

        const val PRODUCT_TABLE = "product"
        const val PURCHASE_ITEM = "purchase_item"

        val FIND_PRODUCTS_QUERY = "SELECT %s FROM $PRODUCT_TABLE INNER JOIN $PURCHASE_ITEM " +
                "ON $ID = $PRODUCT_ID AND $QUANTITY > 0 AND $PURCHASE_ID = :$PURCHASE_ID"
    }
}