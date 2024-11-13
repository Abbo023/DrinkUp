package com.example.provaprogetto.drink

import com.squareup.moshi.Json

class Drink (

    @Json(name = "idDrink")
    var id: Long = 0,

    @Json(name = "strDrink")
    var name: String? = "",

    @Json(name = "strInstructionsIT")
    var instruction: String? = "",

    @Json(name = "strDrinkThumb")
    var image: String? = "",

    @Json(name = "strIngredient1")
    var ingredient1: String? = "",

    @Json(name = "strIngredient2")
    var ingredient2: String? = "",

    @Json(name = "strIngredient3")
    var ingredient3: String? = "",

    @Json(name = "strIngredient4")
    var ingredient4: String? = "",

    @Json(name = "strIngredient5")
    var ingredient5: String? = "",

    @Json(name = "strIngredient6")
    var ingredient6: String? = "",

    @Json(name = "strIngredient7")
    var ingredient7: String? = "",

    @Json(name = "strIngredient8")
    var ingredient8: String? = "",

    @Json(name = "strIngredient9")
    var ingredient9: String? = "",

    @Json(name = "strIngredient10")
    var ingredient10: String? = "",

    @Json(name = "strIngredient11")
    var ingredient11: String? = "",

    @Json(name = "strIngredient12")
    var ingredient12: String? = "",

    @Json(name = "strIngredient13")
    var ingredient13: String? = "",

    @Json(name = "strIngredient14")
    var ingredient14: String? = "",

    @Json(name = "strIngredient15")
    var ingredient15: String? = "",

    @Json(name = "strMeasure1")
    var measure1: String? = "",

    @Json(name = "strMeasure2")
    var measure2: String? = "",

    @Json(name = "strMeasure3")
    var measure3: String? = "",

    @Json(name = "strMeasure4")
    var measure4: String? = "",

    @Json(name = "strMeasure5")
    var measure5: String? = "",

    @Json(name = "strMeasure6")
    var measure6: String? = "",

    @Json(name = "strMeasure7")
    var measure7: String? = "",

    @Json(name = "strMeasure8")
    var measure8: String? = "",

    @Json(name = "strMeasure9")
    var measure9: String? = "",

    @Json(name = "strMeasure10")
    var measure10: String? = "",

    @Json(name = "strMeasure11")
    var measure11: String? = "",

    @Json(name = "strMeasure12")
    var measure12: String? = "",

    @Json(name = "strMeasure13")
    var measure13: String? = "",

    @Json(name = "strMeasure14")
    var measure14: String? = "",

    @Json(name = "strMeasure15")
    var measure15: String? = "",

    var isLike: Boolean = false
){
    fun getIngredients(): MutableList<Ingredient> {
        val ingredientList: MutableList<Ingredient> = ArrayList()

        ingredient1?.let { measure1?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient2?.let { measure2?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient3?.let { measure3?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient4?.let { measure4?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient5?.let { measure5?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient6?.let { measure6?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient7?.let { measure7?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient8?.let { measure8?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient9?.let { measure9?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient10?.let { measure10?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient11?.let { measure11?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient12?.let { measure12?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient13?.let { measure13?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient14?.let { measure14?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }
        ingredient15?.let { measure15?.let { it1 -> ingredientList.add(Ingredient(it, it1)) } }

        return ingredientList
    }

    fun getParsedInstruction(): String? {
        return instruction?.replace(".", ".\n\n")
    }

}
