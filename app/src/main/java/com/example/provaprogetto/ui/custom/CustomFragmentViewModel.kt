package com.example.provaprogetto.ui.custom

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.drink.Recipe
import com.example.provaprogetto.repository.Repository
import com.example.provaprogetto.drink.RicetteFav
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CustomFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val db = Firebase.firestore
    private val repository = Repository()

    private val _recipe = MutableLiveData<List<Recipe>>()
    val recipe: LiveData<List<Recipe>> get() = _recipe

    private var recipeCache: List<Recipe>? = null

    var currentIngredients: List<String> = emptyList()
    var currentDrinkName: String? = null

    private val _isFiltered = MutableLiveData<Boolean>()
    val isFiltered: LiveData<Boolean> get() = _isFiltered


    fun fetchAllDrinks() {
        viewModelScope.launch {
            try {
                if (recipeCache != repository.getCustomRecipes()) {

                    val results = repository.getCustomRecipes()
                    recipeCache = results

                    _recipe.postValue(recipeCache!!)
                    Log.d("FavFragmentViewModel", "Ricette recuperate e memorizzate")
                } else {
                    _recipe.postValue(recipeCache!!)
                    Log.d("FavFragmentViewModel", "Drink e ricette prese dalla cache")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _isFiltered.value = false
    }

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

    fun searchDrinksByIngredientsAndName(
        ingredients: List<String>,
        drinkName: String? = null
    ) {
        viewModelScope.launch {
            val filteredRecipes = recipeCache?.filter { recipe ->
                val nameMatches = drinkName?.let {
                    recipe.name.contains(it, ignoreCase = true)
                } ?: true

                val ingredientsMatch = if (ingredients.isNotEmpty()) {
                    ingredients.all { inputIngredient ->
                        recipe.ingredients.any { recipeIngredient ->
                            recipeIngredient.strIngredient1?.contains(inputIngredient, ignoreCase = true)
                                ?: false
                        }
                    }
                } else {
                    true
                }

                nameMatches && ingredientsMatch
            } ?: emptyList()


            _recipe.postValue(filteredRecipes)
            currentIngredients = ingredients
            currentDrinkName = drinkName

            if (recipeCache != filteredRecipes) {
                _isFiltered.postValue(true)
            }
            else {
                _isFiltered.postValue(false)
            }
        }
    }


    fun restoreFilterState() {
        Log.d("PredFragmentViewModel", "Filtro ripristinato: Ingredienti: $currentIngredients, Nome drink: $currentDrinkName")
        if (currentIngredients.isNotEmpty() || !currentDrinkName.isNullOrEmpty()){
            searchDrinksByIngredientsAndName(currentIngredients, currentDrinkName)}
        else {
            fetchAllDrinks()
        }
    }
}
