package com.example.provaprogetto.ui.personal

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.provaprogetto.drink.Recipe
import com.example.provaprogetto.repository.LocalRepository
import com.example.provaprogetto.repository.Repository
import com.example.provaprogetto.room.LocalDatabase
import com.example.provaprogetto.room.LocalPersonal
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PersonalFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val personalDao = LocalDatabase.getDatabase(application).personalDao()
    private val localRepository = LocalRepository(personalDao)
    private val repository = Repository()
    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }
    private val _personal = MutableLiveData<List<LocalPersonal>>()
    val personal: LiveData<List<LocalPersonal>> get() = _personal

    private var personalCache: List<LocalPersonal> = emptyList()

    private val _updateTrigger = MutableLiveData<LocalPersonal>()
    val updateTrigger: LiveData<LocalPersonal> = _updateTrigger

    private val _processingState = MutableLiveData<ProcessingState>()
    val processingState: LiveData<ProcessingState> = _processingState

    data class ProcessingState(val personalId: String, val isProcessing: Boolean)

    var currentIngredients: List<String> = emptyList()
    var currentDrinkName: String? = null

    private val _isFiltered = MutableLiveData<Boolean>()
    val isFiltered: LiveData<Boolean> get() = _isFiltered


    fun fetchAllDrinks() {
        viewModelScope.launch {
            try {
                if (personalCache != localRepository.getPersonal()) {

                    val results = localRepository.getPersonal()
                    personalCache = results

                    _personal.postValue(personalCache!!)
                    Log.d("FavFragmentViewModel", "Ricette caricate con successo")
                    Log.d("FavFragmentViewModel", "$personalCache")
                } else {
                    _personal.postValue(personalCache!!)
                    Log.d("FavFragmentViewModel", "Drink e ricette recuperate con la cache")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _isFiltered.postValue(false)
    }



    fun getRecipePersonal(personal: LocalPersonal): LiveData<LocalPersonal> {
        return liveData {
            val pers = localRepository.getPersonalRecipe(personal)
            emitSource(pers)
        }
    }

    fun upload(localPersonal: LocalPersonal, context: Context, onUploadFinished: () -> Unit) {
        viewModelScope.launch {
            try {
                _processingState.postValue(ProcessingState(localPersonal.id.toString(), true))

                val updatedPersonal = if (!localPersonal.isUpload) {
                    uploadToFirebase(localPersonal, context)
                } else {
                    removeFromFirebase(localPersonal, context)
                }
                _updateTrigger.postValue(updatedPersonal)
            } catch (e: Exception) {
                Log.e("PersonalFragmentViewModel", "Errore durante upload/rimozione", e)
                Toast.makeText(
                    context,
                    "Si è verificato un errore durante l'operazione",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                _processingState.postValue(ProcessingState(localPersonal.id.toString(), false))
                onUploadFinished()
            }
        }
    }

    private suspend fun uploadToFirebase(localPersonal: LocalPersonal, context: Context): LocalPersonal {
        val imageUri = Uri.parse(localPersonal.imageUrl)
        val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")

        val uploadTask = imageRef.putFile(imageUri).await()
        val imageUrl = uploadTask.storage.downloadUrl.await().toString()

        val recipe = Recipe(
            id = "",
            name = localPersonal.name,
            ingredients = localPersonal.ingredients,
            instructions = localPersonal.instructions,
            imageUrl = imageUrl,
            autore = localPersonal.autore
        )

        val documentReference = repository.db.collection("recipes").add(recipe).await()
        val documentId = documentReference.id
        val updatedRecipe = recipe.copy(id = documentId)

        repository.db.collection("recipes").document(documentId).set(updatedRecipe).await()

        localPersonal.personalId = documentId
        localPersonal.isUpload = true
        localRepository.insertPersonal(localPersonal)

        Log.d("PersonalFragmentViewModel", "Ricette caricate con successo con ID: $documentId")
        Toast.makeText(context, "Upload effettuato con successo", Toast.LENGTH_SHORT).show()

        return localPersonal
    }

    private suspend fun removeFromFirebase(localPersonal: LocalPersonal, context: Context): LocalPersonal {
        if (localPersonal.personalId.isNotEmpty()) {
            try {
                val recipeDocument = repository.db.collection("recipes").document(localPersonal.personalId).get().await()

                if (recipeDocument.exists()) {
                    Log.d("PersonalFragmentViewModel", "La ricetta esiste su Firestore")
                    val recipe = recipeDocument.toObject(Recipe::class.java)
                    recipe?.let {
                        Log.d("PersonalFragmentViewModel", "Ricetta trovata su Firestore: $it")
                        if (it.imageUrl.isNotEmpty()) {
                            val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(it.imageUrl)
                            Log.d("PersonalFragmentViewModel", "Image URL: ${it.imageUrl}")
                            imageRef.delete().addOnSuccessListener {
                                Log.d("PersonalFragmentViewModel", "Immagine rimossa con successo dal Firebase Storage")
                                Toast.makeText(context, "Immagine rimossa con successo", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { exception ->
                                Log.e("PersonalFragmentViewModel", "Immagine rimossa con successo da Firebase Storage: ${exception.message}")
                                Toast.makeText(context, "Impossibile rimuovere l'immagine", Toast.LENGTH_SHORT).show()
                            }.await()
                        }
                    }

                    repository.db.collection("recipes").document(localPersonal.personalId).delete().await()

                    localPersonal.isUpload = false
                    localPersonal.personalId = ""
                    localRepository.insertPersonal(localPersonal)

                    Log.d("PersonalFragmentViewModel", "Ricetta rimossa con successo da Firebase")
                    Toast.makeText(context, "Ricetta rimossa con successo", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("PersonalFragmentViewModel", "Il documento della ricetta non esiste su Firestore")
                    Toast.makeText(context, "Ricetta non trovata nel database", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PersonalFragmentViewModel", "Errore durante la rimozione della ricetta da Firebase", e)
                Toast.makeText(context, "Errore durante la rimozione della ricetta", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("PersonalFragmentViewModel", "Impossibile rimuovere la ricetta: ID mancante")
            Toast.makeText(context, "Impossibile rimuovere la ricetta: ID mancante", Toast.LENGTH_SHORT).show()
        }

        return localPersonal
    }



    fun deletePersonal(personal: LocalPersonal) {
        viewModelScope.launch {
            if(personal.isUpload){
                removeFromFirebase(personal, getApplication())
            }
            localRepository.deletePersonal(personal)
        }
    }

    fun searchDrinksByIngredientsAndName(
        ingredients: List<String>,
        drinkName: String? = null
    ) {
        viewModelScope.launch {
            val filteredPersonal = personalCache.filter { personal ->
                val nameMatches = drinkName?.let {
                    personal.name.contains(it, ignoreCase = true)
                } ?: true

                val ingredientsMatch = if (ingredients.isNotEmpty()) {
                    ingredients.all { inputIngredient ->
                        personal.ingredients.any { recipeIngredient ->
                            recipeIngredient.strIngredient1?.contains(inputIngredient, ignoreCase = true)
                                ?: false
                        }
                    }
                } else {
                    true
                }

                nameMatches && ingredientsMatch
            }

            _personal.postValue(filteredPersonal)
            currentIngredients = ingredients
            currentDrinkName = drinkName

            if (filteredPersonal != personalCache) {
                _isFiltered.postValue(true)
            }
            else {
                _isFiltered.postValue(false)
            }


        }
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
