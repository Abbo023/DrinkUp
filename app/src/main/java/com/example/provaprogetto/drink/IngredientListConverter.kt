package com.example.provaprogetto.drink

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IngredientListConverter {


    @TypeConverter
    fun fromList(list: List<Ingredient>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromString(value: String): List<Ingredient> {
        val gson = Gson()
        val listType =object : TypeToken<List<Ingredient>>() {}.type
        return gson.fromJson(value, listType)
    }
}