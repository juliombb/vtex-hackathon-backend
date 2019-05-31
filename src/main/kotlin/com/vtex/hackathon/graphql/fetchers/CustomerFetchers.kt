package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Customer
import com.vtex.hackathon.graphql.util.getInstant
import com.vtex.hackathon.graphql.util.getInstantIfPresent
import com.vtex.hackathon.graphql.util.getLongIfPresent
import com.vtex.hackathon.graphql.util.getStringIfPresent
import graphql.language.Field
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
@Component
class CustomerFetchers(private val jdbc: NamedParameterJdbcTemplate) {

    val customerByIdDataFetcher: DataFetcher<Customer?> =
        DataFetcher { environment ->
            val id = environment.getArgument<String>("id").toLong()

            val parentFields = extractSelectionFields(environment)

            jdbc.query(
                FIND_CUSTOMER_QUERY.format(parentFields), mapOf(ID to id)
            ) { resultSet, _ -> resultSet.toCustomer() }.firstOrNull()
        }

    val customerByEmailDataFetcher: DataFetcher<Customer?> =
        DataFetcher { environment ->
            val email = environment.getArgument<String>("email")

            val parentFields = extractSelectionFields(environment)

            jdbc.query(
                FIND_CUSTOMER_QUERY.format(parentFields), mapOf(EMAIL to email)
            ) { resultSet, _ -> resultSet.toCustomer() }.firstOrNull()
        }

    private fun extractSelectionFields(environment: DataFetchingEnvironment): String {
        val parentFields = environment.fields
            .flatMap { it.selectionSet.selections }
            .mapNotNull { it as? Field }
            .joinToString(", ") { it.name }
        return parentFields
    }

    private fun ResultSet.toCustomer(): Customer {
        return Customer(
            id = getLongIfPresent(ID),
            email = getStringIfPresent(EMAIL),
            phone = getStringIfPresent(PHONE),
            name = getStringIfPresent(NAME),
            cpf = getStringIfPresent(CPF),
            birthDate = getInstantIfPresent(BIRTH_DATE)
        )
    }

    companion object {
        const val ID = "id"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val NAME = "name"
        const val CPF = "cpf"
        const val BIRTH_DATE = "birth_date"

        val FIND_CUSTOMER_QUERY = "SELECT %s FROM customer WHERE id = :$ID"
        val FIND_CUSTOMER_EMAIL_QUERY = "SELECT %s FROM customer WHERE email = :$EMAIL"
    }
}