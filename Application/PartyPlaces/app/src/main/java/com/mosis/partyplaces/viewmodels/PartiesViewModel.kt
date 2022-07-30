package com.mosis.partyplaces.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.mosis.partyplaces.data.Party

class PartiesViewModel : ViewModel() {
    private val _parties = MutableLiveData<MutableMap<String, Party>>(mutableMapOf())
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun getMutable() = _parties

    var parties
        get() = _parties.value
        set(value) { _parties.value = value }

    fun newParty(p : Party){
        parties!![p.uid] = p
    }
}