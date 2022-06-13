package com.mosis.partyplaces.viewmodels

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.mosis.partyplaces.data.User
import java.io.File

class LoggedUserViewModel(user: User? = null) : ViewModel() {

    private val _user = MutableLiveData<User?>(user)

    fun getMutable() = _user

    var user
        get() = _user.value
        set(value) { _user.value = value }

    fun logout(a:Activity, callback: () -> Unit){
        user = null
        a.getSharedPreferences("LoggedUser", MODE_PRIVATE).edit().clear().commit()
        callback()
    }

    fun login(a:Activity, u:User, callback: () -> Unit){
        val source = Firebase.storage.reference.child(u.downloadUri!!)
        val img = File.createTempFile("temp_profile_${u.username}", "jpg")
        source.getFile(img)
            .addOnSuccessListener { fileTask ->
                if(fileTask.error == null) {
                    u.imageUri = img.toUri().toString()
                    user = u
                    a.getSharedPreferences("LoggedUser", MODE_PRIVATE).edit().apply {
                        putString("value", Gson().toJson(u, User::class.java).toString())
                        commit()
                    }
                    callback()
                }
                else
                    Toast.makeText(a, "Error downloading profile image!", Toast.LENGTH_SHORT).show()
            }
    }
}