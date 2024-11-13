package com.example.provaprogetto.ui.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.repository.LocalRepository
import com.example.provaprogetto.repository.Repository
import com.example.provaprogetto.room.LocalDatabase
import com.example.provaprogetto.room.LocalPersonal
import kotlinx.coroutines.launch

class NewPersonalViewModel (application: Application) : AndroidViewModel(application) {


        private val personalDao = LocalDatabase.getDatabase(application).personalDao()
        val localRepository = LocalRepository(personalDao)
        val repository = Repository()

        private val _personal = MutableLiveData<List<LocalPersonal>>()
        val personal: LiveData<List<LocalPersonal>> get() = _personal


        private val _predefinedIngredients = MutableLiveData<List<String>>()
        val predefinedIngredients: LiveData<List<String>> = _predefinedIngredients


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

}
