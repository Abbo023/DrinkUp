package com.example.provaprogetto.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.provaprogetto.drink.Drink
import com.example.provaprogetto.drink.Recipe
import com.example.provaprogetto.network.TheCocktailDBApi
import com.example.provaprogetto.drink.RicetteFav
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Repository {

    private val drinkList: MutableLiveData<List<Drink>> = MutableLiveData()
    private val drink: MutableLiveData<Drink> = MutableLiveData()
    val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    val db = Firebase.firestore
    private val userRef = user?.let { db.collection("users").document(it.uid) }

    private val recipeList: MutableLiveData<List<Recipe>> = MutableLiveData()

    suspend fun getDrinkList(): MutableLiveData<List<Drink>> {
        try {
            withContext(Dispatchers.IO) {
                val response = TheCocktailDBApi.retrofitService.getAlcoholicDrinks()
                    drinkList.postValue(response.list )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return drinkList
    }

    suspend fun getDrinkRecipe(id: Long): MutableLiveData<Drink> {
        try {
            withContext(Dispatchers.IO) {
                val response = TheCocktailDBApi.retrofitService.getDrink(id)
                drink.postValue(response.list?.firstOrNull() ?: Drink())

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return drink
    }

    suspend fun searchDrink(name: String): MutableLiveData<List<Drink>> {
        val result = MutableLiveData<List<Drink>>()
        try {
            withContext(Dispatchers.IO) {
                val response = TheCocktailDBApi.retrofitService.searchDrinkByName(name)

                    result.postValue(response.list)


            }
        } catch (e: Exception) {
            e.printStackTrace()
            result.postValue(emptyList())
        }
        return result
    }

     suspend fun searchDrinkByIngredient(ingredient: String): MutableLiveData<List<Drink>> {
        val result = MutableLiveData<List<Drink>>()
        try {
            withContext(Dispatchers.IO) {
                val response = TheCocktailDBApi.retrofitService.searchDrinkByIngredient(ingredient)
                result.postValue(response.list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result.postValue(emptyList())
        }
        return result
    }

    private suspend fun searchRecipe(name: String): Recipe? {
        var result: Recipe? = null
        try {
            withContext(Dispatchers.IO) {
                for (recipe in getCustomRecipes()) {
                    if (recipe.id == name) {
                        result = recipe
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
        return result
    }
    suspend fun deleteRecipe(recipeId: String) {
        if (user != null) {
            try {

                if(userRef != null) {
                    val userData = userRef.get().await()


                    if (userData.exists()) {
                        val ricettePreferite =
                            userData.get("ricette preferite") as? List<Map<String, Any>>
                                ?: emptyList()

                        val ricetteFavList = ricettePreferite.map {
                            RicetteFav(
                                origin = it["origin"] as String,
                                drinkId = it["drinkId"] as String
                            )
                        }.toMutableList()

                        val iterator = ricetteFavList.iterator()
                        while (iterator.hasNext()) {
                            val ricetta = iterator.next()
                            if (ricetta.origin == "firebase" && ricetta.drinkId == recipeId) {
                                iterator.remove()
                                break
                            }
                        }

                        userRef.update("ricette preferite", ricetteFavList.map {
                            mapOf("origin" to it.origin, "drinkId" to it.drinkId)
                        }).await()
                    }
                }
            } catch (e: Exception) {
                Log.e("deleteDrink", "Errore durante la rimozione del drink", e)
            }
        }
    }



    suspend fun deleteDrink(drinkId: String) {
        if (user != null) {
            try {

                if(userRef != null) {
                    val userData = userRef.get().await()

                    if (userData.exists()) {
                        val ricettePreferite =
                            userData.get("ricette preferite") as? List<Map<String, Any>>
                                ?: emptyList()

                        val ricetteFavList = ricettePreferite.map {
                            RicetteFav(
                                origin = it["origin"] as String,
                                drinkId = it["drinkId"] as String
                            )
                        }.toMutableList()

                        val iterator = ricetteFavList.iterator()
                        while (iterator.hasNext()) {
                            val ricetta = iterator.next()
                            if (ricetta.origin == "pred" && ricetta.drinkId == drinkId) {
                                iterator.remove()
                                break
                            }
                        }

                        userRef.update("ricette preferite", ricetteFavList.map {
                            mapOf("origin" to it.origin, "drinkId" to it.drinkId)
                        }).await()
                    }
                }
            } catch (e: Exception) {
                Log.e("deleteDrink", "Errore durante la rimozione del drink", e)
            }
        }
    }

    suspend fun isFavoriteDrink(drinkId: String): Boolean {
        if (user != null) {
            try {

                if(userRef != null) {
                    val userData = userRef.get().await()

                    if (userData.exists()) {
                        val ricettePreferite =
                            userData.get("ricette preferite") as? List<Map<String, Any>>
                                ?: emptyList()

                        val ricetteFavList = ricettePreferite.map {
                            RicetteFav(
                                origin = it["origin"] as String,
                                drinkId = it["drinkId"] as String
                            )
                        }.toMutableList()

                        val iterator = ricetteFavList.iterator()
                        while (iterator.hasNext()) {
                            val ricetta = iterator.next()
                            if (ricetta.origin == "pred" && ricetta.drinkId == drinkId) {
                                return true
                            }
                        }
                        return false
                    }
                }
            } catch (e: Exception) {
                Log.e("isFavoriteDrink", "Errore durante la ricerca di drink preferiti", e)
            }

        }
        return false
    }

    suspend fun isFavoriteRecipe(recipeId: String): Boolean {
        if (user != null) {
            Log.d("isFavoriteRecipe", "User is not null")
            try {
                if(userRef != null) {
                    val userData = userRef.get().await()

                    if (userData.exists()) {
                        Log.d("isFavoriteRecipe", "User data exists: ${userData.data}")

                        val ricettePreferite =
                            userData.get("ricette preferite") as? List<Map<String, Any>>
                                ?: emptyList()

                        val ricetteFavList = ricettePreferite.map {
                            RicetteFav(
                                origin = it["origin"] as String,
                                drinkId = it["drinkId"] as String
                            )
                        }.toMutableList()

                        val iterator = ricetteFavList.iterator()
                        while (iterator.hasNext()) {
                            Log.d("isFavoriteRecipe", "Checking recipe")
                            val ricetta = iterator.next()
                            Log.d("isFavoriteRecipe", "Checking recipe: $ricetta")
                            if (ricetta.origin == "firebase" && ricetta.drinkId == recipeId) {
                                Log.d("isFavoriteRecipe", "Recipe is favorite: $ricetta")
                                return true
                            }
                            Log.d("isFavoriteRecipe", "Recipe is not favorite: $ricetta")
                        }
                        Log.d("isFavoriteRecipe", "Recipe is not found")
                        return false
                    }
                }
            } catch (e: Exception) {
                Log.e("isFavoriteDrink", "Errore durante la ricerca di drink preferiti", e)
            }

        }
        Log.d("isFavoriteRecipe", "User is null")
        return false

    }
    private suspend fun searchDrinksId(): List<Long> {
        if (user != null) {
            try {
                if(userRef != null) {
                    val userData = userRef.get().await()

                    if (userData.exists()) {

                        val ricettePreferite =
                            userData.get("ricette preferite") as? List<Map<String, Any>>
                                ?: emptyList()

                        val drinkIds = ricettePreferite.filter { it["origin"] == "pred" }
                            .mapNotNull { it["drinkId"]?.toString()?.toLongOrNull() }

                        return drinkIds
                    }
                }
            } catch (e: Exception) {
                Log.e("searchDrinksId", "Errore durante il recupero dei drinkId", e)
            }
        }
        return emptyList()
    }

    private suspend fun searchRecipesId(): List<String> {
        if (user != null) {
            try {

                if(userRef != null) {
                    val userData = userRef.get().await()

                    if (userData.exists()) {

                        val ricettePreferite =
                            userData.get("ricette preferite") as? List<Map<String, Any>>
                                ?: emptyList()

                        val drinkIds = ricettePreferite.filter { it["origin"] != "pred" }
                            .mapNotNull { it["drinkId"]?.toString() }

                        return drinkIds
                    }
                }
            } catch (e: Exception) {
                Log.e("searchDrinksId", "Errore durante il recupero dei drinkId", e)
            }
        }
        return emptyList()
    }




    suspend fun getFavDrinkList(): MutableLiveData<List<Drink>> {
        val drinkIds = searchDrinksId()
        val tempDrinkList =
            mutableListOf<Drink>()

        for (id in drinkIds) {
            try {
                val drinkResponse = TheCocktailDBApi.retrofitService.getDrink(id)
                val drink = drinkResponse.list.firstOrNull()
                if (drink != null) {
                    tempDrinkList.add(drink)
                } else {
                    Log.w("LocalRepository", "Nessun drink con ID: $id")
                }
            } catch (e: Exception) {
                Log.e("LocalRepository", "Errore nel recupero del drink con ID: $id", e)
            }
        }

        drinkList.value = tempDrinkList
        return drinkList
    }

    suspend fun getFavRecipeList(): MutableLiveData<List<Recipe>> {
        val recipeIds = searchRecipesId()
        val tempRecipeList =
            mutableListOf<Recipe?>()

        for (id in recipeIds) {
            try {
                val recipe = searchRecipe(id)
                if (recipe != null) {
                    tempRecipeList.add(recipe)
                    Log.d("LocalRepository", "$recipe")
                }
                else {
                    deleteRecipe(id)
                }
            } catch (e: Exception) {
                Log.e("LocalRepository", "Errore nel recupero del drink con ID: $id", e)
            }
        }

        recipeList.value =
            tempRecipeList as List<Recipe>
        return recipeList
    }


    suspend fun getIngredients(): List<String> {
        val response = TheCocktailDBApi.retrofitService.getIngredients()
        return response.ingredients.map { it.strIngredient1.toString() }
    }





    suspend fun getCustomRecipes(): List<Recipe> {
        return try {
            db.collection("recipes")
                .get()
                .await()
                .toObjects(Recipe::class.java)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TAG", "nessuna ricetta")
            emptyList()
        }
    }


    private val customRecipe = MutableLiveData<Recipe>()


    fun getCustomRecipe(recipe: Recipe): LiveData<Recipe> {
        customRecipe.value = recipe
        return customRecipe
    }

     fun incrementLike(recipeId: String) {
        try {
            val documentRef = db.collection("recipes").document(recipeId)
            documentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val recipe = documentSnapshot.toObject(Recipe::class.java)

                        if (recipe != null) {
                            recipe.likes += 1

                            documentRef.update("likes", recipe.likes)
                                .addOnSuccessListener {
                                    Log.d(
                                        "LocalRepository",
                                        "Successfully incremented like for recipe with ID: $recipeId"
                                    )
                                    Log.d("LocalRepository", "New like count: ${recipe.likes}")
                                    Log.d("LocalRepository", "Recipe: $recipe")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LocalRepository", "Failed to update likes for recipe with ID: $recipeId", e)
                                }
                        }
                    } else {
                        Log.e("LocalRepository", "No recipe found with ID: $recipeId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocalRepository", "Failed to fetch recipe with ID: $recipeId", e)
                }
        } catch (e: Exception) {
            Log.e("LocalRepository", "Error incrementing like for recipe with ID: $recipeId", e)
        }
    }

    fun decrementLike(recipeId: String) {
        try {
            val documentRef = db.collection("recipes").document(recipeId)
            documentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val recipe = documentSnapshot.toObject(Recipe::class.java)

                        if (recipe != null) {
                            recipe.likes -= 1

                            documentRef.update("likes", recipe.likes)
                                .addOnSuccessListener {
                                    Log.d(
                                        "LocalRepository",
                                        "Successfully incremented like for recipe with ID: $recipeId"
                                    )
                                    Log.d("LocalRepository", "New like count: ${recipe.likes}")
                                    Log.d("LocalRepository", "Recipe: $recipe")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LocalRepository", "Failed to update likes for recipe with ID: $recipeId", e)
                                }
                        }
                    } else {
                        Log.e("LocalRepository", "No recipe found with ID: $recipeId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocalRepository", "Failed to fetch recipe with ID: $recipeId", e)
                }
        } catch (e: Exception) {
            Log.e("LocalRepository", "Error incrementing like for recipe with ID: $recipeId", e)
        }
    }

}
