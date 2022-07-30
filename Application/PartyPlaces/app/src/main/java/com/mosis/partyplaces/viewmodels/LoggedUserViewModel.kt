package com.mosis.partyplaces.viewmodels

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mosis.partyplaces.data.User
import com.mosis.partyplaces.data.toObject

class LoggedUserViewModel(user: User? = null) : ViewModel() {

    private val _user = MutableLiveData<User?>(user)
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun getMutable() = _user

    var user
        get() = _user.value
        set(value) { _user.value = value }

    fun logout(callback: () -> Unit){
        logout()
        callback()
    }

    fun logout() {
        user = null
    }

    fun login(u:User){
        user = u
    }

    fun  login(u:User, callback: () -> Unit){
        login(u)
        callback()
    }

    fun checkSharedPreferences(a : Activity, valueExistsCallback : () -> Unit, valueDoesntExistCallback : () -> Unit) {
        val sp = a.getSharedPreferences("Logged-User", MODE_PRIVATE)
        val json = sp.getString("User", "")
        Log.d("USER-USER-USER", json.toString())
        if(json!!.isNotBlank()) {
            login(json.toObject()){
                valueExistsCallback()
                validateUser(valueDoesntExistCallback)
            }
            return
        }
        valueDoesntExistCallback()
    }

    fun validateUser(invalidUserCallback : () -> Unit){
        db.collection("users")
            .whereEqualTo("id", user!!.id)
            .get()
            .addOnSuccessListener { res2 ->
                databaseResult(res2, invalidUserCallback)
            }
    }

    private fun databaseResult(res : QuerySnapshot, invalidUserCallback: () -> Unit){
        // Morace da se menja
        /*if(res.documents.isEmpty()) //|| res.documents[0].toObject<User>() != user)
            invalidUserCallback()*/
    }
}