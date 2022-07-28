package com.mosis.partyplaces.data

import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user")
data class User(
    @SerializedName("firstName")
    @Expose
    var firstName: String = "",
    @SerializedName("lastName")
    @Expose
    var lastName: String = "",
    @SerializedName("email")
    @Expose
    var email: String = "",
    @SerializedName("username")
    @Expose
    var username: String = "",
    @SerializedName("password")
    @Expose
    var password: String = "",
    @SerializedName("imageUri")
    @Expose
    var imageUri: String? = null,
    @SerializedName("downloadUri")
    @Expose
    var downloadUri: String? = null,
    @SerializedName("lat")
    @Expose
    var lat:Double = 0.0,
    @SerializedName("lon")
    @Expose
    var lon:Double = 0.0,
    @SerializedName("uuid")
    @Expose
    var uuid:String? = null) : JSONConvertable {

    fun toHashMap():Map<String, *> {
        return mapOf(
            "uuid" to uuid,
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
