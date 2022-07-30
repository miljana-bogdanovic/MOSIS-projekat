package com.mosis.partyplaces.data

import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user_lite")
open class UserLite(
    @SerializedName("id_lite")
    @Expose
    open var id : String = "",
    @SerializedName("firstName_lite")
    @Expose
    open var firstName : String = "",
    @SerializedName("lastName_lite")
    @Expose
    open var lastName : String = "",
    @SerializedName("email_lite")
    @Expose
    open var email : String = "",
    @SerializedName("username_lite")
    @Expose
    open var username : String = "",
    @SerializedName("profilePhotoDownloadPath_lite")
    @Expose
    open var profilePhotoDownloadPath : String = "",
    @SerializedName("profilePhotoUri_lite")
    @Expose
    open var profilePhotoUriString : String = "",
    @SerializedName("score_lite")
    @Expose
    open var score : Long = 0,
    @SerializedName("friendNo_lite")
    @Expose
    open var friendNo : Long = 0,
    @SerializedName("partyNo_lite")
    @Expose
    open var partyNo : Long = 0,
    @SerializedName("rank_lite")
    @Expose
    open var rank : Long = -1) {
}