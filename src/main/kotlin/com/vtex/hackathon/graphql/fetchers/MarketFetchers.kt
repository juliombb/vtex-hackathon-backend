package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Market
import com.vtex.hackathon.graphql.util.extractSelectionFields
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
class MarketFetchers(private val jdbc: NamedParameterJdbcTemplate) {
    val marketByIdFetcher: DataFetcher<Market?> =
        DataFetcher { environment ->
            val id = environment.getArgument<String>("id").toLong()

            val parentFields = environment.extractSelectionFields().map { it.name }

            jdbc.query(
                FIND_MARKET_QUERY.format(parentFields), mapOf(ID to id)
            ) { resultSet, _ -> resultSet.toMarket() }.firstOrNull()
        }

    private fun ResultSet.toMarket(): Market {
        return Market(
            id = getLongIfPresent(ID),
            address = getStringIfPresent(ADDRESS)
        )
    }

    companion object {
        const val ID = "id"
        const val ADDRESS = "address"


        val FIND_MARKET_QUERY = "SELECT %s FROM market WHERE id = :$ID"
    }
}