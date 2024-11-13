package com.example.provaprogetto.drink

import com.squareup.moshi.Json

data class DrinkList(

    @Json(name = "drinks")
    var list: MutableList<Drink> = ArrayList()
)