package com.example.provaprogetto.drink
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class  Ingredient(
    @JvmField var strIngredient1: String?,
    @JvmField var strMeasure1: String = ""
): Parcelable
{
    constructor() : this("", "")
}
