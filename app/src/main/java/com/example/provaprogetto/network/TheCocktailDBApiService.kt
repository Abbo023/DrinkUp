package com.example.provaprogetto.network

import com.example.provaprogetto.drink.DrinkList
import com.example.provaprogetto.drink.IngredientList
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface TheCocktailDBApiService {

    @GET("lookup.php")
    suspend fun getDrink(@Query("i") id: Long,  @Query("lang") lang: String = "it"): DrinkList

    @GET("filter.php?a=Alcoholic")
    suspend fun getAlcoholicDrinks( @Query("lang") lang: String = "it"): DrinkList

    @GET("search.php")
    suspend fun searchDrinkByName(@Query("s") name: String,  @Query("lang") lang: String = "it"): DrinkList

    @GET("filter.php")
    suspend fun searchDrinkByIngredient(@Query("i") ingredient: String,  @Query("lang") lang: String = "it"): DrinkList
    @GET("list.php?i=list")
    suspend fun getIngredients( @Query("lang") lang: String = "it"): IngredientList


}
object TheCocktailDBApi {
    val retrofitService: TheCocktailDBApiService by lazy { retrofit.create(TheCocktailDBApiService::class.java) }

}
