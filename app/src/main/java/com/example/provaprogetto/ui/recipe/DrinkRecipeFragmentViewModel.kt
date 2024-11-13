package com.example.provaprogetto.ui.recipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.drink.Drink
import com.example.provaprogetto.drink.Recipe
import com.example.provaprogetto.drink.RicetteFav
import com.example.provaprogetto.repository.Repository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DrinkRecipeFragmentViewModel : ViewModel() {
    private val repository = Repository()
    val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val db = Firebase.firestore
    private var userRef = user.let { db.collection("users").document(it!!.uid) }

    fun toggleLike(recipe: Recipe) {
        viewModelScope.launch {
            if (user != null) {
                try {
                    val userRef = db.collection("users").document(user.uid)

                    val userData = userRef.get().await()

                    if (userData.exists()) {
                        val ricettePreferite = userData.get("ricette preferite") as? List<Map<String, String>> ?: emptyList()

                        val ricetteFavList = ricettePreferite.map {
                            RicetteFav(
                                origin = it["origin"] as String,
                                drinkId = it["drinkId"] as String
                            )
                        }.toMutableList()

                        val nuovaRicettaFav = RicetteFav(
                            origin = "firebase",
                            drinkId = recipe.id
                        )

                        if (ricetteFavList.contains(nuovaRicettaFav)) {
                            ricetteFavList.remove(nuovaRicettaFav)
                            repository.decrementLike(recipe.id)
                        } else {
                            ricetteFavList.add(nuovaRicettaFav)
                            repository.incrementLike(recipe.id)

                        }

                        userRef.update("ricette preferite", ricetteFavList.map {
                            mapOf("origin" to it.origin, "drinkId" to it.drinkId)
                        }).await()
                    }
                } catch (e: Exception) {
                    Log.e("toggleFavorite", "Errore durante l'aggiornamento delle ricette preferite", e)
                }
            }
        }
    }

    fun getDrinkDetails(drinkId: Long): LiveData<Drink> {
        return liveData {
            val drink = repository.getDrinkRecipe(drinkId)
            emitSource(drink)
        }
    }

    fun toggleFavorite(drink: Drink) {
        viewModelScope.launch {
            if (user != null) {
                try {

                    val userData = userRef.get().await()

                    if (userData.exists()) {
                        val ricettePreferite = userData.get("ricette preferite") as? List<Map<String, String>> ?: emptyList()

                        val ricetteFavList = ricettePreferite.map {
                            RicetteFav(
                                origin = it["origin"] as String,
                                drinkId = it["drinkId"] as String
                            )
                        }.toMutableList()

                        val nuovaRicettaFav = RicetteFav(
                            origin = "pred",
                            drinkId = drink.id.toString()
                        )

                        if (ricetteFavList.contains(nuovaRicettaFav)) {
                            ricetteFavList.remove(nuovaRicettaFav)
                        } else {
                            ricetteFavList.add(nuovaRicettaFav)
                        }

                        userRef.update("ricette preferite", ricetteFavList.map {
                            mapOf("origin" to it.origin, "drinkId" to it.drinkId)
                        }).await()
                    }
                } catch (e: Exception) {
                    Log.e("toggleFavorite", "Errore durante l'aggiornamento delle ricette preferite", e)
                }
            }
        }
    }

    fun getRecipeDrink(recipe: Recipe): LiveData<Recipe> {
        return liveData {
            val drink = repository.getCustomRecipe(recipe)
            emitSource(drink)
        }
    }
}
