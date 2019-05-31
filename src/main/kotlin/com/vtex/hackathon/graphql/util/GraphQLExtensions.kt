package com.vtex.hackathon.graphql.util

import graphql.language.Field
import graphql.schema.DataFetchingEnvironment

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
fun DataFetchingEnvironment.extractSelectionFields() =
    this.fields
        .flatMap { it.selectionSet.selections }
        .mapNotNull { it as? Field }