package com.example.provaprogetto.ui.fav

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.drink.Drink
import com.example.provaprogetto.drink.Recipe
import com.example.provaprogetto.repository.Repository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository()

    private val _drinks = MutableLiveData<List<Any>>()
    val drinks: LiveData<List<Any>> get() = _drinks

    private val _error = MutableLiveData<String>()

    private var recipeCache: List<Recipe>? = null
    var currentIngredients: List<String> = emptyList()
    var currentDrinkName: String? = null


    private val _isFiltered = MutableLiveData<Boolean>()
    val isFiltered: LiveData<Boolean> get() = _isFiltered



    fun fetchAllDrinks() {
        viewModelScope.launch {
            try {
                val combinedList = combine(
                    repository.getFavDrinkList().asFlow().map { it ?: emptyList() },
                    repository.getFavRecipeList().asFlow().map { it ?: emptyList() }
                ) { drinks, recipes ->
                    drinks + recipes
                }.first()

                _drinks.postValue(combinedList)
                Log.d("FavFragmentViewModel", "Update lista combinata: $combinedList")
            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Errore durante il caricamento dei drink e delle ricette.")
            }
        }
        _isFiltered.value = false
    }


    fun removeDrink(drink: Drink) {
        viewModelScope.launch {
            try {
                val drinkId = drink.id.toString()
                repository.deleteDrink(drinkId)
                _drinks.value = _drinks.value?.filterNot { it is Drink && it.id == drink.id }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                val recipeId = recipe.id
                repository.deleteRecipe(recipeId)
                _drinks.value = _drinks.value?.filterNot { it is Recipe && it.id == recipe.id }
                repository.decrementLike(recipe.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    fun searchDrinksByIngredientsAndName(
        ingredients: List<String>,
        drinkName: String? = null
    ) {

            if (ingredients.isNotEmpty()) {
                Log.d("FavFragmentViewModel", "Ingredienti: $ingredients, Nome drink: $drinkName")
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
                    val localFavDrinkIds = repository.getFavDrinkList().value?.map { it.id } ?: emptyList()

                    val filteredFavDrinks = finalResult.filter { apiDrink ->
                        localFavDrinkIds.contains(apiDrink.id)
                    }

                    recipeCache = repository.getFavRecipeList().value
                    Log.d("FavFragmentViewModel", "Ricette trovate: $recipeCache")

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
                    Log.d("FavFragmentViewModel", "Ricette filtrate: $filteredRecipes")
                    _drinks.postValue(filteredFavDrinks + filteredRecipes)
                    _isFiltered.value = true
                }
            } else if (!drinkName.isNullOrEmpty()) {
                Log.d("FavFragmentViewModel", "Nome drink: $drinkName")
                viewModelScope.launch {
                    val response = repository.searchDrink(drinkName).value
                    var filteredFavDrinks = emptyList<Drink>()
                    response?.let {
                        val localFavDrinkIds =
                            repository.getFavDrinkList().value?.map { it.id } ?: emptyList()

                        filteredFavDrinks = response.filter { apiDrink ->
                            localFavDrinkIds.contains(apiDrink.id)
                        }
                    }

                        recipeCache = repository.getFavRecipeList().value
                        Log.d("FavFragmentViewModel", "Ricette recuperate: $recipeCache")

                        val filteredRecipes = recipeCache?.filter { recipe ->
                            val nameMatches = drinkName.let {
                                recipe.name.contains(it, ignoreCase = true)
                            }
                            nameMatches
                        } ?: emptyList()
                        Log.d("FavFragmentViewModel", "Ricette filtrate: $filteredRecipes")
                        _drinks.postValue(filteredFavDrinks + filteredRecipes)
                        _isFiltered.value = true
                    }
                }
        else {
            Log.d("FavFragmentViewModel", "Campi vuoti")
            fetchAllDrinks()
        }

        currentIngredients = ingredients
        currentDrinkName = drinkName
    }




    fun restoreFilterState() {
        Log.d("PredFragmentViewModel", "Ripristino filtro: Ingredienti: $currentIngredients, Nome drink: $currentDrinkName")
        if (currentIngredients.isNotEmpty() || !currentDrinkName.isNullOrEmpty()){
            searchDrinksByIngredientsAndName(currentIngredients, currentDrinkName)}
        else {
            fetchAllDrinks()
        }
    }

}
