package com.example.provaprogetto.ui.profile

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user: FirebaseUser? = auth.currentUser
    private val db = Firebase.firestore

    private val _userData = MutableLiveData<Map<String, Any>>()
    val userData: LiveData<Map<String, Any>> get() = _userData

    init {
        loadUserData()
    }

    private fun loadUserData() {
        user?.let {
            val userRef = db.collection("users").document(it.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        _userData.value = document.data
                    }
                }
        }
    }

    fun changePassword() {
        user?.email?.let { email ->
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this.getApplication(),
                            "Email di reimpostazione password inviata a $email",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this.getApplication(),
                            "Errore durante l'invio dell'email di reimpostazione",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } ?: run {
            Toast.makeText(
                this.getApplication(),
                "L'utente non ha un'email associata.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()

    }

}
