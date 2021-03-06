package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Purchase
import com.vtex.hackathon.graphql.model.PurchaseId
import com.vtex.hackathon.graphql.model.PurchaseItem
import com.vtex.hackathon.graphql.model.PurchaseStatus
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
class PurchaseMutators(
    private val jdbc: NamedParameterJdbcTemplate,
    private val purchasetBaseFetcher: DataFetcher<Purchase>,
    private val clock: Clock
) {
    val create: DataFetcher<Purchase> =
        DataFetcher { env ->
            val customerId = env.getArgument<String>("customerId").toLong()
            val cashboxId = env.getArgument<String>("cashboxId").toLong()
            val status = PurchaseStatus.STARTED
            val startedAt = clock.instant()

            val id = jdbc.query(
                INSERT_QUERY,
                mapOf(
                    CUSTOMER_ID to customerId,
                    CASH_BOX_ID to cashboxId,
                    STATUS to status.name,
                    STARTED_AT to startedAt.toString(),
                    TOTAL to 0
                )
            ) { rs, _ -> rs.getLong(1) }.first()

            Purchase(
                id = id,
                customerId = customerId,
                cashboxId = cashboxId,
                status = status,
                startedAt = startedAt)
        }

    val addProduct: DataFetcher<Purchase> =
        DataFetcher { env ->
            val purchaseId = env.getArgument<String>("purchaseId").toLong()
            val productId = env.getArgument<String>("productId")

            val basePurchase = purchasetBaseFetcher.get(env)

            if (basePurchase.status != PurchaseStatus.STARTED) {
                println("Cannot add item to purchase with status ${basePurchase.status}")
                return@DataFetcher basePurchase
            }

            incrementItem(productId, purchaseId)
                ?: insertItem(productId, purchaseId)

            purchasetBaseFetcher.get(env)
        }

    val cashboxApprove: DataFetcher<Purchase> =
            DataFetcher { env ->
                val purchaseId = env.getArgument<String>("purchaseId").toLong()

                val basePurchase = purchasetBaseFetcher.get(env)

                if (basePurchase.status != PurchaseStatus.STARTED) {
                    println("Cannot cashbox approve to purchase with status ${basePurchase.status}")
                    return@DataFetcher basePurchase
                }

                cashboxApprove(purchaseId)

                purchasetBaseFetcher.get(env)
            }

    val customerApprove: DataFetcher<Purchase> =
        DataFetcher { env ->
            val purchaseId = env.getArgument<String>("purchaseId").toLong()

            val basePurchase = purchasetBaseFetcher.get(env)

            if (basePurchase.status != PurchaseStatus.CASHBOX_APPROVED) {
                println("Cannot customer approve to purchase with status ${basePurchase.status}")
                return@DataFetcher basePurchase
            }

            customerApprove(purchaseId)

            purchasetBaseFetcher.get(env)
        }

    fun cashboxApprove (purchaseId: PurchaseId) {
        jdbc.update(
            UPDATE_STATUS,
            mapOf(STATUS to PurchaseStatus.CASHBOX_APPROVED.name, ID to purchaseId)
        )
    }

    fun customerApprove (purchaseId: PurchaseId) {
        jdbc.update(
            UPDATE_STATUS_AND_FINISH,
            mapOf(
                STATUS to PurchaseStatus.FINISHED.name,
                ID to purchaseId,
                FINISHED_AT to clock.instant().toString()
            )
        )
    }

    private fun insertItem(productId: String?, purchaseId: Long): PurchaseItem {
        return jdbc.query(
            INSERT_ITEM_QUERY,
            mapOf(
                PRODUCT_ID to productId,
                PURCHASE_ID to purchaseId
            )
        ) { rs, _ -> rs.toPurchaseItem() }
            .first()
    }

    private fun incrementItem(productId: String?, purchaseId: Long): PurchaseItem? =
        jdbc.query(
            ADD_ITEM_QUERY,
            mapOf(
                PRODUCT_ID to productId,
                PURCHASE_ID to purchaseId
            )
        ) { rs, _ -> rs.toPurchaseItem() }
            .firstOrNull()

    private fun increaseTotal(purchaseId: Long, amount: Long) {
        jdbc.query(
            INCREASE_TOTAL_QUERY,
            mapOf(
                PURCHASE_ID to purchaseId,
                AMOUNT to amount
            )
        ) { _, _ -> }
    }

    private fun decrementItem(productId: String?, wishListId: Long): PurchaseItem? =
        jdbc.query(
            DEC_ITEM_QUERY,
            mapOf(
                PRODUCT_ID to productId,
                PURCHASE_ID to wishListId
            )
        ) { rs, _ -> rs.toPurchaseItem() }
            .firstOrNull()

    private fun decreaseTotal(purchaseId: Long, amount: Long) {
        jdbc.query(
            DECREASE_TOTAL_QUERY,
            mapOf(
                PURCHASE_ID to purchaseId,
                AMOUNT to amount
            )
        ) { _, _ -> }
    }

    private fun deleteItem(wishListItem: PurchaseItem) {
        jdbc.query(
            DELETE_ITEM_QUERY,
            mapOf(
                PRODUCT_ID to wishListItem.productId,
                PURCHASE_ID to wishListItem.purchaseId
            )
        ) { _, _ -> }
    }

    private fun extractSelectionFields(environment: DataFetchingEnvironment) =
        environment.fields
            .flatMap { it.selectionSet.selections }
            .mapNotNull { it as? Field }
            .filter { it.name != "__typename" }

    private fun ResultSet.toPurchaseItem() =
        PurchaseItem(
            productId = getStringIfPresent(PRODUCT_ID),
            purchaseId = getLongIfPresent(PURCHASE_ID),
            quantity = getLong(QUANTITY)
        )

    companion object {
        const val PURCHASE_TABLE = "purchase"
        const val ID = "id"
        const val CUSTOMER_ID = "customer_id"
        const val CASH_BOX_ID = "cash_box_id"
        const val STATUS = "status"
        const val STARTED_AT = "started_at"
        const val FINISHED_AT = "finished_at"
        const val TOTAL = "total"
        const val AMOUNT = "amount"

        val INSERT_QUERY = "INSERT INTO $PURCHASE_TABLE ($CUSTOMER_ID, $CASH_BOX_ID, $STATUS, " +
                "$STARTED_AT, $FINISHED_AT, $TOTAL) " +
                "VALUES (:$CUSTOMER_ID, :$CASH_BOX_ID, :$STATUS, " +
                ":$STARTED_AT::timestamp, null, :$TOTAL) RETURNING $ID"

        val INCREASE_TOTAL_QUERY = "UPDATE $PURCHASE_TABLE SET $TOTAL = $TOTAL + :$AMOUNT " +
                "WHERE $ID = :$ID"

        val DECREASE_TOTAL_QUERY = "UPDATE $PURCHASE_TABLE SET $TOTAL = $TOTAL - :$AMOUNT " +
                "WHERE $ID = :$ID"

        val UPDATE_STATUS = "UPDATE $PURCHASE_TABLE SET $STATUS = :$STATUS " +
                "WHERE $ID = :$ID"

        val UPDATE_STATUS_AND_FINISH = "UPDATE $PURCHASE_TABLE " +
                "SET $STATUS = :$STATUS, $FINISHED_AT = :$FINISHED_AT::timestamp " +
            "WHERE $ID = :$ID"

        const val PURCHASE_ITEM = "purchase_item"
        const val PURCHASE_ID = "purchase_id"
        const val PRODUCT_ID = "product_id"
        const val QUANTITY = "quantity"

        val INSERT_ITEM_QUERY = "INSERT INTO $PURCHASE_ITEM ($PURCHASE_ID, $PRODUCT_ID, $QUANTITY) " +
                "VALUES (:$PURCHASE_ID, :$PRODUCT_ID, 1) RETURNING $PURCHASE_ID, $PRODUCT_ID, $QUANTITY"

        val ADD_ITEM_QUERY = "UPDATE $PURCHASE_ITEM SET $QUANTITY = $QUANTITY + 1 " +
                "WHERE $PURCHASE_ID = :$PURCHASE_ID AND $PRODUCT_ID = :$PRODUCT_ID " +
                "RETURNING $PURCHASE_ID, $PRODUCT_ID, $QUANTITY"

        val DEC_ITEM_QUERY = "UPDATE $PURCHASE_ITEM SET $QUANTITY = $QUANTITY - 1 " +
                "WHERE $PURCHASE_ID = :$PURCHASE_ID AND $PRODUCT_ID = :$PRODUCT_ID " +
                "RETURNING $PURCHASE_ID, $PRODUCT_ID, $QUANTITY"

        val DELETE_ITEM_QUERY = "DELETE FROM $PURCHASE_ITEM " +
                "WHERE $PURCHASE_ID = :$PURCHASE_ID AND $PRODUCT_ID = :$PRODUCT_ID "
    }
}