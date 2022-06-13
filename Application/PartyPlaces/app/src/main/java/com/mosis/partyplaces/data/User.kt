package com.mosis.partyplaces.data

import androidx.lifecycle.ViewModel

data class User(var firstName: String = "", var lastName: String = "", var email: String = "", var username: String = "", var password: String = ""){

    fun toHashMap():Map<String, String> {
        return mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "username" to username,
            "password" to password
        )
    }
}
