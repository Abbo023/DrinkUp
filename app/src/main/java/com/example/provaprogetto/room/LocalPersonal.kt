package com.example.provaprogetto.room

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.provaprogetto.drink.Ingredient
import com.example.provaprogetto.drink.IngredientListConverter

@Parcelize
@Entity(tableName = "personal")
@TypeConverters(IngredientListConverter::class)
data class LocalPersonal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var personalId: String = "",
    val name: String = "",
    val ingredients: List<Ingredient> = listOf(),
    val instructions: String = "",
    val imageUrl: String = "",
    val autore: String = "",
    var isFavorite: Boolean = false,
    var isLike: Boolean = false,
    var likes: Int = 0,
    var isUpload: Boolean = false
) : Parcelable {
    fun obtainIngredientsList(): List<Ingredient> {
        return ingredients
    }

}
