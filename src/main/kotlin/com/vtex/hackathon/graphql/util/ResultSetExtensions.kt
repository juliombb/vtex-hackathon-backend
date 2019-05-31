package com.vtex.hackathon.graphql.util

import com.vtex.hackathon.graphql.fetchers.ProductFetchers
import com.vtex.hackathon.graphql.model.FullItem
import com.vtex.hackathon.graphql.model.Product
import java.sql.ResultSet

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
fun ResultSet.getInstant(field: String) = this.getTimestamp(field)?.toInstant()
inline fun <reified T: Enum<T>> ResultSet.getEnum(field: String): T? = this.getString(field)?.let { enumValueOf<T>(it) }

fun ResultSet.getInstantIfPresent(field: String) = if (this.hasColumn(field)) this.getInstant(field) else null
fun ResultSet.getStringIfPresent(field: String) = if (this.hasColumn(field)) this.getString(field) else null
fun ResultSet.getLongIfPresent(field: String) = if (this.hasColumn(field)) this.getLong(field) else null
fun ResultSet.getBooleanIfPresent(field: String) = if (this.hasColumn(field)) this.getBoolean(field) else null
inline fun <reified T: Enum<T>> ResultSet.getEnumIfPresent(field: String) =
    if (this.hasColumn(field)) this.getEnum<T>(field) else null

fun ResultSet.hasColumn(field: String): Boolean {
    for (column in 1..metaData.columnCount) {
        if (metaData.getColumnName(column) == field) {
            return true
        }
    }

    return false
}

fun ResultSet.toProduct(): Product {
    return Product(
        id = getStringIfPresent(ProductFetchers.ID),
        category = getStringIfPresent(ProductFetchers.CATEGORY),
        asset = getStringIfPresent(ProductFetchers.ASSET),
        description = getStringIfPresent(ProductFetchers.DESCRIPTION),
        price = getLongIfPresent(ProductFetchers.PRICE)
    )
}


private const val QUANTITY = "quantity"
fun ResultSet.toFullItem(): FullItem {
    return FullItem(
        id = getStringIfPresent(ProductFetchers.ID),
        category = getStringIfPresent(ProductFetchers.CATEGORY),
        asset = getStringIfPresent(ProductFetchers.ASSET),
        description = getStringIfPresent(ProductFetchers.DESCRIPTION),
        price = getLongIfPresent(ProductFetchers.PRICE),
        quantity = getLongIfPresent(QUANTITY)
    )
}

