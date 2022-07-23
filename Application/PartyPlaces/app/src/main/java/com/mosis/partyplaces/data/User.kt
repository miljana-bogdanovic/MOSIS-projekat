package com.mosis.partyplaces.data

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference

data class User(var firstName: String = "", var lastName: String = "", var email: String = "", var username: String = "", var password: String = "", var imageUri: String? = null, var downloadUri: String? = null, var lat:Double = 0.0, var lon:Double = 0.0){

    fun toHashMap():Map<String, *> {
        return mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "username" to username,
            "password" to password,
            "downloadUri" to downloadUri,
            "lat" to lat,
            "lon" to lon
        )
    }
}
