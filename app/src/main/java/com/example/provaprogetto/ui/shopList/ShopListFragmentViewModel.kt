package com.example.provaprogetto.ui.shopList

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.drink.ShopListIngredients
import com.example.provaprogetto.repository.Repository
import kotlinx.coroutines.launch

class ShopListFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val _shoppingList = MutableLiveData<MutableList<ShopListIngredients>>(mutableListOf())
    val shoppingList: LiveData<MutableList<ShopListIngredients>> = _shoppingList

    private val _predefinedIngredients = MutableLiveData<List<String>>()
    val predefinedIngredients: LiveData<List<String>> = _predefinedIngredients

    private val repository = Repository()

    init {
        fetchPredefinedIngredients()
        loadShoppingList()
    }

    private fun fetchPredefinedIngredients() {
        viewModelScope.launch {
            val ingredients = repository.getIngredients()
            val ingredientsWithPlaceholder = mutableListOf<String>()
            ingredientsWithPlaceholder.add("Seleziona un ingrediente")
            ingredientsWithPlaceholder.addAll(ingredients)
            _predefinedIngredients.value = ingredientsWithPlaceholder
        }
    }

    fun addIngredient(ingredient: String) {
        val currentList = _shoppingList.value ?: mutableListOf()
        val existingItem = currentList.find { it.ingredient == ingredient }
        if (existingItem == null) {
            currentList.add(ShopListIngredients(ingredient, 750))
            _shoppingList.value = currentList
            saveShoppingList(currentList)
        }
        Log.d("ShopListViewModel", "Update lista della spesa: ${_shoppingList.value}")
    }

    fun updateQuantity(ingredient: String, quantity: Int) {
        val currentList = _shoppingList.value ?: mutableListOf()
        val item = currentList.find { it.ingredient == ingredient }
        item?.let {
            it.quantity = quantity
            _shoppingList.value = currentList
            saveShoppingList(currentList)
        }
        Log.d("ShopListViewModel", "Quantità modificata: ${item?.ingredient} ${item?.quantity}")
        Log.d("ShopListViewModel", "Update lista della spesa: ${_shoppingList.value}")

    }


    private fun saveShoppingList(ingredients: List<ShopListIngredients>) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "com.example.provaprogetto.SHOPPING_LIST", Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val stringSet = ingredients.map { "${it.ingredient}:${it.quantity}" }.toSet()
        editor.putStringSet("shopping_list", stringSet)
        editor.apply()
        Log.d("ShopListViewModel", "Lista della spesa salvata: $stringSet")
    }

    private fun loadShoppingList() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "com.example.provaprogetto.SHOPPING_LIST", Context.MODE_PRIVATE
        )
        val savedIngredients = sharedPreferences.getStringSet("shopping_list", setOf())

        val shoppingListItems = savedIngredients?.map { ingredientString ->
            val parts = ingredientString.split(":")
            val ingredient = parts[0]
            val quantity = parts.getOrNull(1)?.toIntOrNull() ?: 750
            ShopListIngredients(ingredient, quantity)
        } ?: emptyList()

        _shoppingList.value = shoppingListItems.toMutableList()
        Log.d("ShopListViewModel", "Lista della spesa caricata: ${_shoppingList.value}")
    }


    fun removeIngredient(ingredient: ShopListIngredients) {
        val currentList = _shoppingList.value ?: mutableListOf()
        currentList.remove(ingredient)
        _shoppingList.value = currentList
        saveShoppingList(currentList)
        Log.d("ShopListViewModel", "Ingrediente rimosso: $ingredient")
        Log.d("ShopListViewModel", "Update lista della spesa: ${_shoppingList.value}")
    }
}
