package com.example.provaprogetto.drink

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    @JvmField val id: String = "",
    @JvmField val name: String = "",
    @JvmField val ingredients: List<Ingredient> = listOf(),
    @JvmField val instructions: String = "",
    @JvmField val imageUrl: String = "",
    @JvmField val autore: String = "",
    @JvmField var likes: Int = 0,
    @JvmField var isLike: Boolean = false
) : Parcelable {
    constructor() : this("", "", emptyList(), "", "")
    fun obtainIngredientsList(): List<Ingredient> {
        return ingredients
    }

}

