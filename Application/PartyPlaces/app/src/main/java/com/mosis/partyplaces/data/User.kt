package com.mosis.partyplaces.data

import android.location.Location
import androidx.room.Entity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
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
    @SerializedName("location")
    @Expose
    var location : GeoPoint = GeoPoint(.0,.0),
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
            "location" to mapOf(
                "longitude" to location.longitude,
                "latitude" to location.latitude
            )
        )
    }
}
