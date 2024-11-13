package com.example.provaprogetto.ui.pred

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.drink.Drink
import com.example.provaprogetto.repository.Repository
import com.example.provaprogetto.drink.RicetteFav
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PredFragmentViewModel(application: Application) : AndroidViewModel(application) {
    val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val db = Firebase.firestore
    private var userRef = user.let { db.collection("users").document(it!!.uid) }

    private val repository: Repository = Repository()
    private val _drinks: MutableLiveData<List<Drink>> = MutableLiveData()
    val drinks: LiveData<List<Drink>> = _drinks
    var currentIngredients: List<String> = emptyList()
    var currentDrinkName: String? = null
    private val _isFiltered = MutableLiveData<Boolean>()
    val isFiltered: LiveData<Boolean> get() = _isFiltered

    fun fetchDrinkList() {
        viewModelScope.launch {
            _drinks.value = repository.getDrinkList().value
            _isFiltered.postValue(false)
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

    fun searchDrinksByIngredientsAndName(
        ingredients: List<String>,
        drinkName: String? = null
    ) {
        if (ingredients.isNotEmpty()) {
            viewModelScope.launch {
                val allMatchingDrinks = mutableListOf<Set<Long>>()

                for (ingredient in ingredients) {
                    val response = repository.searchDrinkByIngredient(ingredient).value
                    response?.let {
                        allMatchingDrinks.add(it.map { drink -> drink.id }.toSet())
                    }
                }

                val intersectedDrinkIds = if (allMatchingDrinks.isNotEmpty()) {
                    allMatchingDrinks.reduce { commonIds, idSet ->
                        commonIds.intersect(idSet)
                    }
                } else {
                    emptySet()
                }
                val intersectedDrinks =  mutableListOf<Drink>()
                for(id in intersectedDrinkIds){
                    val response = repository.getDrinkRecipe(id).value
                    response?.let {
                      intersectedDrinks.add(it)
                    }
                }




                val finalResult = if (!drinkName.isNullOrEmpty()) {
                    intersectedDrinks.filter { drink ->
                        drink.name?.lowercase()?.contains(drinkName.lowercase()) == true
                    }
                } else {
                    intersectedDrinks
                }

                Log.d("PredFragmentViewModel", "Final Result: $finalResult")

                _drinks.postValue(finalResult)
                _isFiltered.postValue(true)

            }
        }
        else if (!drinkName.isNullOrEmpty()) {
            viewModelScope.launch {
                val response = repository.searchDrink(drinkName).value
                response?.let {
                    _drinks.postValue(it)
                    _isFiltered.postValue(true)
                }
            }
        }
        else {
            fetchDrinkList()
        }
        currentIngredients = ingredients
        currentDrinkName = drinkName
    }

    fun restoreFilterState() {
        Log.d("PredFragmentViewModel", "Ripristino filtro: Ingredienti: $currentIngredients, Nome drink: $currentDrinkName")
        if (currentIngredients.isNotEmpty() || !currentDrinkName.isNullOrEmpty()){
            Log.d("PredFragmentViewModel", "Ripristino filtro2: Ingredienti: $currentIngredients, Nome drink: $currentDrinkName")
            searchDrinksByIngredientsAndName(currentIngredients, currentDrinkName)}
        else {
            fetchDrinkList()
            _isFiltered.postValue(false)
        }
    }
}
