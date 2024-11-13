package com.example.provaprogetto.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PersonalDao {

    @Query("SELECT * FROM personal WHERE autore = 'you'")
    fun getYourRecipes(): LiveData<List<LocalPersonal>>

    @Query("SELECT * FROM personal WHERE autore = 'pred'")
    fun getPredRecipes(): LiveData<List<LocalPersonal>>

    @Query("SELECT * FROM personal WHERE autore NOT IN ('you', 'pred')")
    fun getOthersRecipies(): LiveData<List<LocalPersonal>>

    @Query("SELECT * FROM personal")
    suspend fun getAllPersonal(): List<LocalPersonal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonal(personal: LocalPersonal)

    @Delete
    suspend fun deletePersonal(personal: LocalPersonal)

    @Query("SELECT * FROM personal WHERE id = :personalId LIMIT 1")
    suspend fun getPersonalById(personalId: String): LocalPersonal

    @Query("SELECT EXISTS (SELECT 1 FROM personal WHERE id = :personalId LIMIT 1)")
    suspend fun isFavorite(personalId: String): Boolean

    @Query("SELECT * FROM personal WHERE id = :id LIMIT 1")
    suspend fun getPersonalRecipe(id: Long): LocalPersonal?

    @Query("SELECT isUpload FROM personal WHERE id = :id LIMIT 1")
    suspend fun isUpload(id: String): Boolean

}