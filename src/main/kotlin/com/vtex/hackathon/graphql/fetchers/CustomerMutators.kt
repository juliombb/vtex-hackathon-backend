package com.vtex.hackathon.graphql.fetchers

import com.vtex.hackathon.graphql.model.Customer
import com.vtex.hackathon.graphql.model.Gender
import graphql.schema.DataFetcher
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
@Component
class CustomerMutators(
    private val jdbc: NamedParameterJdbcTemplate,
    private val clock: Clock
) {
    val create: DataFetcher<Customer> =
        DataFetcher { env ->
            val email = env.getArgument<String>("email")
            val name = env.getArgument<String>("name")
            val cpf = env.getArgument<String>("cpf")
            val gender = env.getArgument<String>("gender")
            val phone = env.getArgument<String>("phone")
            val birthDate = env.getArgument<String>("birthDate")

            val id = jdbc.query(
                INSERT_CUSTOMER_QUERY,
                mapOf(
                    NAME to name,
                    CPF to cpf,
                    EMAIL to email,
                    PHONE to phone,
                    GENDER to gender,
                    BIRTH_DATE to birthDate)
            ) { rs, _ -> rs.getLong(1) }.first()

            Customer(
                id = id,
                name = name,
                cpf = cpf,
                email = email,
                phone = phone,
                gender = if (gender != null) Gender.valueOf(gender) else null,
                birthDate = if (birthDate != null) Instant.parse(birthDate) else null
            )
        }

    companion object {
        const val CUSTOMER_TABLE = "customer"
        const val CUSTOMER_ID = "id"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val NAME = "name"
        const val CPF = "cpf"
        const val GENDER = "gender"
        const val BIRTH_DATE = "birth_date"

        val INSERT_CUSTOMER_QUERY = "INSERT INTO $CUSTOMER_TABLE " +
                "($NAME, $CPF, $EMAIL, $PHONE, $GENDER, $BIRTH_DATE) " +
                "VALUES(:$NAME, :$CPF, :$EMAIL, :$PHONE, :$GENDER, :$BIRTH_DATE::timestamp) " +
                "RETURNING $CUSTOMER_ID"

    }
}