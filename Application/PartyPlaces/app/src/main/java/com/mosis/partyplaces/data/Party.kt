package com.mosis.partyplaces.data

import com.google.firebase.firestore.GeoPoint
import java.sql.Time
import java.util.Date

data class Party(var name : String,
                 var theme : String,
                 var day : Date,
                 var start : Time,
                 var end : Time,
                 var location : GeoPoint,
                 var organizer : User,
                 var uid : String = "",
                 var guests : MutableMap<String, User> = mutableMapOf()){
}