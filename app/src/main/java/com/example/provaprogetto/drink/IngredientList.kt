package com.example.provaprogetto.drink

import com.squareup.moshi.Json

data class IngredientList (
    @Json(name = "drinks")
    var ingredients: List<Ingredient> = ArrayList()
)