package com.mosis.partyplaces.data

import android.net.Uri
import androidx.room.Entity
import com.google.firebase.firestore.GeoPoint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user")
data class User(
    @SerializedName("firstName")
    @Expose
    var firstName : String = "",
    @SerializedName("lastName")
    @Expose
    var lastName : String = "",
    @SerializedName("email")
    @Expose
    var email : String = "",
    @SerializedName("username")
    @Expose
    var username : String = "",
    @SerializedName("password")
    @Expose
    var password : String = "",
    @SerializedName("profilePhotoDownloadUri")
    @Expose
    var profilePhotoDownloadPath : String = "",
    @SerializedName("profilePhotoUri")
    @Expose
    var profilePhotoUriString : String = "",
    @SerializedName("location")
    @Expose
    var location : GeoPoint = GeoPoint(.0,.0),
    @SerializedName("uuid")
    @Expose
    var uuid : String = "",
    @SerializedName("rank")
    @Expose
    var rank : Int = -1,
    @SerializedName("parties")
    @Expose
    var parties : MutableList<String> = mutableListOf(),
    @SerializedName("friends")
    @Expose
    var friends : MutableList<String> = mutableListOf()) : JSONConvertable{
}
