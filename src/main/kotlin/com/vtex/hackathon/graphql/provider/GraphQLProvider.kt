package com.vtex.hackathon.graphql.provider

import com.google.common.io.Resources
import com.vtex.hackathon.graphql.fetchers.*
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import kotlin.text.Charsets
import javax.annotation.PostConstruct
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring

/**
 * @author Júlio Moreira Blás de Barros (julio.barros@movile.com)
 * @since 2/7/19
 */
@Component
class GraphQLProvider(
    val customerFetchers: CustomerFetchers,
    val customerMutators: CustomerMutators,
    val marketFetchers: MarketFetchers,
    val productFetchers: ProductFetchers,
    val purchaseFetchers: PurchaseFetchers,
    val purchaseMutators: PurchaseMutators,
    val wishListFetchers: WishListFetchers,
    val wishListMutators: WishListMutators
) {

    private var graphQL: GraphQL? = null

    @Bean
    fun graphQL() = graphQL

    @PostConstruct
    fun init() {
        val url = Resources.getResource("schema.graphqls")
        val sdl = Resources.toString(url, Charsets.UTF_8)
        val graphQLSchema = buildSchema(sdl)
        this.graphQL = GraphQL.newGraphQL(graphQLSchema)
            .build()
    }

    private fun buildSchema(sdl: String): GraphQLSchema {
        val typeRegistry = SchemaParser().parse(sdl)
        val runtimeWiring = buildWiring()
        val schemaGenerator = SchemaGenerator()
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)
    }

    private fun buildWiring(): RuntimeWiring {
        return RuntimeWiring.newRuntimeWiring()
            .type(
                newTypeWiring("Query")
                    .dataFetcher("customerById", customerFetchers.customerByIdDataFetcher)
                    .dataFetcher("customerByEmail", customerFetchers.customerByEmailDataFetcher)
                    .dataFetcher("customerPurchases", purchaseFetchers.purchasesByCustomerId)
                    .dataFetcher("customerWishLists", wishListFetchers.wishListsByCustomerId)

                    .dataFetcher("allProducts", productFetchers.allProductsFetcher)
                    .dataFetcher("product", productFetchers.productByIdFetcher)
                    .dataFetcher("allMarkets", marketFetchers.allMarketsFetcher)
                    .dataFetcher("market", marketFetchers.marketByIdFetcher)
            )
            .type(
                newTypeWiring("Mutation")
                    .dataFetcher("createCustomer", customerMutators.create)
                    .dataFetcher("createWishList", wishListMutators.create)
                    .dataFetcher("addProductToWishList", wishListMutators.addProduct)
                    .dataFetcher("removeProductFromWishList", wishListMutators.removeProduct)
                    .dataFetcher("disableWishList", wishListMutators.disable)

                    .dataFetcher("startPurchase", purchaseMutators.create)
                    .dataFetcher("addItemToPurchase", purchaseMutators.addProduct)
                    .dataFetcher("cashboxApprovePurchase", purchaseMutators.cashBoxApprove)
                    .dataFetcher("customerApprovePurchase", purchaseMutators.customerApprove)
            )
            .build()
    }
}