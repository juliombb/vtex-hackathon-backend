package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Product
import com.vtex.hackathon.graphql.util.getLongIfPresent
import com.vtex.hackathon.graphql.util.getStringIfPresent
import graphql.language.Field
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

/**
 * @author Alexandre Ladeira (alexandreladeira13@gmail.com)
 * @since 5/31/19
 */
@Component
class ProductFetchers(private val jdbc: NamedParameterJdbcTemplate) {
    val productByIdFetcher: DataFetcher<Product?> =
        DataFetcher { environment ->
            val id = environment.getArgument<String>("id")

            val parentFields = extractSelectionFields(environment)

            jdbc.query(
                FIND_PRODUCT_QUERY.format(parentFields), mapOf(ID to id)
            ) { resultSet, _ -> resultSet.toProduct() }.firstOrNull()
        }

    private fun extractSelectionFields(environment: DataFetchingEnvironment): String {
        val parentFields = environment.fields
            .flatMap { it.selectionSet.selections }
            .mapNotNull { it as? Field }
            .joinToString(", ") { it.name }
        return parentFields
    }

    private fun ResultSet.toProduct(): Product {
        return Product(
            id = getStringIfPresent(ID),
            category = getStringIfPresent(CATEGORY),
            asset = getStringIfPresent(ASSET),
            description = getStringIfPresent(DESCRIPTION),
            price = getLongIfPresent(PRICE)
        )
    }

    companion object {
        const val ID = "id"
        const val CATEGORY = "category"
        const val ASSET = "asset"
        const val DESCRIPTION = "description"
        const val PRICE = "price"


        val FIND_PRODUCT_QUERY = "SELECT %s FROM product WHERE id = :$ID"

    }
}