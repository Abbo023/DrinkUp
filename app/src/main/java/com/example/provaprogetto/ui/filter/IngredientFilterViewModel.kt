package com.example.provaprogetto.ui.filter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.repository.Repository
import kotlinx.coroutines.launch

class IngredientFilterViewModel(application: Application) : AndroidViewModel(application) {
    private val _ingredientsList = MutableLiveData<MutableList<String>>(mutableListOf())
    val ingredientsList: LiveData<MutableList<String>> = _ingredientsList

    private val _predefinedIngredients = MutableLiveData<List<String>>()
    val predefinedIngredients: LiveData<List<String>> = _predefinedIngredients

    private val repository = Repository()

    init {
        fetchPredefinedIngredients()
    }

    private fun fetchPredefinedIngredients() {
        viewModelScope.launch {
            val ingredients = repository.getIngredients()
            val ingredientsWithPlaceholder = mutableListOf(" ")
            ingredientsWithPlaceholder.addAll(ingredients)
            _predefinedIngredients.value = ingredientsWithPlaceholder
        }
    }

    fun addIngredient(ingredient: String) {
        val currentIngredients = _ingredientsList.value ?: mutableListOf()
        if (!currentIngredients.contains(ingredient)) {
            currentIngredients.add(ingredient)
            _ingredientsList.value = currentIngredients
        }
    }

    fun removeIngredient(ingredient: String) {
        val currentIngredients = _ingredientsList.value ?: mutableListOf()
        currentIngredients.remove(ingredient)
        _ingredientsList.value = currentIngredients
    }

    fun clearIngredients() {
        _ingredientsList.value = mutableListOf()
    }
}


