package com.vtex.hackathon.graphql.util

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 5/31/19
 */
fun String.toSnakeCase() =
    this.map { if (it.isUpperCase()) "_$it" else "$it" }.joinToString("")