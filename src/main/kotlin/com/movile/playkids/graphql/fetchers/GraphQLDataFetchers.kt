package com.movile.playkids.graphql.fetchers

import com.movile.playkids.graphql.model.Child
import com.movile.playkids.graphql.model.Person
import com.movile.playkids.graphql.model.PersonId
import graphql.language.Field
import graphql.schema.DataFetcher
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.*


/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 2/7/19
 */
@Component
class GraphQLDataFetchers(private val jdbc: NamedParameterJdbcTemplate) {

    val personByIdDataFetcher: DataFetcher<Person?> =
        DataFetcher { environment ->
            val personId = environment.getArgument<String>("id")

            people.firstOrNull {
                person -> person.id == personId
            }
        }

    val allPersonsFetcher: DataFetcher<List<Person>> =
        DataFetcher { environment ->
            val last = environment.getArgument<Int?>("last")

            val parentFields = environment.fields
                .flatMap { it.selectionSet.selections }
                .mapNotNull { it as? Field }

            val filteredFields = parentFields
                .filter { it.selectionSet == null }
                .toSet()

            val people = fetchFieldsFromLastUsers(filteredFields, last)

            val children = if (parentFields.any { it.name == "children" }) {
                val childFields = parentFields
                    .filter { it.selectionSet != null }
                    .flatMap { it.selectionSet.selections }
                    .mapNotNull { it as? Field }
                    .toSet()

                fetchChildren(childFields, people.mapNotNull { it.id })
            } else {
                emptyList()
            }
                .groupBy { it.parentId }

            people.map { it.copy(children = children[it.id] ?: emptyList()) }
            //if (fields.contains("children"))
        }

    private fun fetchChildren(fields: Set<Field>, people: List<PersonId>): List<Child> {
        val fieldsQuery = fields.map { it.name }.joinToString { ", " }
        val query = "SELECT $fieldsQuery FROM children WHERE parent_id = ANY(:ids)"

        return jdbc.query(query, mapOf("ids" to people.toTypedArray())) { resultSet, _ ->
            Child(
                id = resultSet.getString("id"),
                name = resultSet.getString("name"),
                age = resultSet.getString("age")?.toIntOrNull(),
                parentId = resultSet.getString("parent_id"),
                favoriteCartoonId = resultSet.getString("favorite_cartoon")?.toIntOrNull()
            )
        }
    }

    private fun fetchFieldsFromLastUsers(fields: Set<Field>, last: Int?): List<Person> {
        val fieldsQuery = fields.map { it.name }.joinToString { ", " }
        val query = "SELECT $fieldsQuery FROM user ORDER BY created_at DESC" +
                (last?.let { " LIMIT $it" } ?: "")

        return jdbc.query(query) { resultSet, _ ->
            Person(
                id = resultSet.getString("id"),
                name = resultSet.getString("name"),
                age = resultSet.getString("age")?.toIntOrNull()
            )
        }
    }

    val createPerson: DataFetcher<Person> =
        DataFetcher { environment ->
            Person(
                id = UUID.randomUUID().toString(),
                name = environment.getArgument<String>("name"),
                age = environment.getArgument<Int>("age"),
                children = emptyList()
            ).also {
                people.add(it)
            }
        }

    val newChild: DataFetcher<Person> =
        DataFetcher { env ->
            val parent = people.first {
                it.id == env.getArgument<PersonId>("parentId")
            }

            val child = Child(
                id = UUID.randomUUID().toString(),
                name = env.getArgument<String>("name"),
                age = env.getArgument<Int>("age"),
                parentId = parent.id,
                favoriteCartoonId = env.getArgument<Int>("favoriteCartoonId")
            )

            parent.copy(
                children = parent.children + child
            ).also {
                people.removeIf { p -> p.id == parent.id }
                people.add(it)
            }
        }

    companion object {
        private val children = listOf(
            Child(
                id = "child-1",
                name = "Adriano",
                age = 20,
                parentId = "person-2",
                favoriteCartoonId = 3
            )
        )

        private val people = mutableListOf(
            Person(
                id = "person-1",
                name = "Julio",
                age = 20,
                children = emptyList()
            ),
            Person(
                id = "person-2",
                name = "Kiq",
                age = 24,
                children = emptyList()
            ),
            Person(
                id = "person-3",
                name = "Jao",
                age = 22,
                children = children
            )
        )
    }
}