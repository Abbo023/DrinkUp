package com.example.provaprogetto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.provaprogetto.room.LocalPersonal
import com.example.provaprogetto.room.PersonalDao

class LocalRepository(private val personalDao: PersonalDao) {


    suspend fun insertPersonal(personal: LocalPersonal) {
        personalDao.insertPersonal(personal)
    }

    suspend fun deletePersonal(personal: LocalPersonal) {
        personalDao.deletePersonal(personal)
    }

    suspend fun getPersonal(): List<LocalPersonal> {
        return personalDao.getAllPersonal()
    }

    private val personalRecipe = MutableLiveData<LocalPersonal>()

    fun getPersonalRecipe(personal: LocalPersonal): LiveData<LocalPersonal> {
        personalRecipe.value = personal
        return personalRecipe
    }

}


