package com.mosis.partyplaces.data

import androidx.room.Entity
import com.google.firebase.firestore.GeoPoint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user")
data class User(
    @SerializedName("firstName")
    @Expose
    override var firstName : String = "",
    @SerializedName("lastName")
    @Expose
    override var lastName : String = "",
    @SerializedName("email")
    @Expose
    override var email : String = "",
    @SerializedName("username")
    @Expose
    override var username : String = "",
    @SerializedName("password")
    @Expose
    var password : String = "",
    @SerializedName("profilePhotoDownloadPath")
    @Expose
    override var profilePhotoDownloadPath : String = "",
    @SerializedName("profilePhotoUriString")
    @Expose
    override var profilePhotoUriString : String = "",
    @SerializedName("location")
    @Expose
    var location : GeoPoint = GeoPoint(.0,.0),
    @SerializedName("id")
    @Expose
    override var id : String = "",
    @SerializedName("parties")
    @Expose
    var parties : MutableList<String> = mutableListOf(),
    @SerializedName("friends")
    @Expose
    var friends : MutableList<String> = mutableListOf(),
    @SerializedName("score")
    @Expose
    override var score : Long = 0,
    @SerializedName("friendNo")
    @Expose
    override var friendNo : Long = 0,
    @SerializedName("partyNo")
    @Expose
    override var partyNo : Long = 0,
    @SerializedName("rank")
    @Expose
    override var rank : Long = -1
    ) : JSONConvertable, UserLite(){
}
