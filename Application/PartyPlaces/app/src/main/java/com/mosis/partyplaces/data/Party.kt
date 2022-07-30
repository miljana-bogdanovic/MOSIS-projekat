package com.mosis.partyplaces.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.sql.Time
import java.util.Date

data class Party(
    @SerializedName("name")
    @Expose
    var name : String = "",
    @SerializedName("theme")
    @Expose
    var theme : String = "",
    @SerializedName("day")
    @Expose
    var day : Date = Date(),
    @SerializedName("start")
    @Expose
    var start : String = "00:00h",
    @SerializedName("end")
    @Expose
    var end : String = "00:00h",
    @SerializedName("location")
    @Expose
    var location : GeoPoint = GeoPoint(.0, .0),
    @SerializedName("address")
    @Expose
    var address : String = "",
    @SerializedName("organizer")
    @Expose
    var organizer : UserLite = UserLite(),
    @SerializedName("photoDownloadPath")
    @Expose
    var photoDownloadPath : String = "",
    @SerializedName("photoUriString")
    @Expose
    var photoUriString : String = "",
    @SerializedName("id")
    @Expose
    var id : String = "",
    @SerializedName("guestNo")
    @Expose
    var guestNo : Long = 0,
    @SerializedName("score")
    @Expose
    var score : Double = .0,
    @Transient
    var guests : MutableMap<String, UserLite> = mutableMapOf()){
}