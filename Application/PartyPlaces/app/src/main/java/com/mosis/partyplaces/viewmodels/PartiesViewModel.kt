package com.mosis.partyplaces.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mosis.partyplaces.data.DatabaseUtilities
import com.mosis.partyplaces.data.Party
import kotlin.random.Random.Default.nextInt

class PartiesViewModel : ViewModel() {
    private val _parties = MutableLiveData<MutableMap<String, Party>>(mutableMapOf())

    fun getMutable() = _parties

    var parties
        get() = _parties.value
        set(value) { _parties.value = value }

    fun newParty(
        party : Party,
        successCallback: (Party) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        if(party.photoUriString.isNotEmpty()){
            DatabaseUtilities.savePartyWithPhoto(
                party,
                { p ->
                    parties!![p.id] = p
                    Log.d("New-Party", "Success: '${ p.id }'")
                    successCallback(p)
                },
                { ex ->
                    Log.e("New-Party", "Failed: '${ party.name }-${ party.theme }'")
                    Log.e("New-Party", ex.stackTraceToString())
                    failureCallback(ex)
                })
        }
        else {
            party.photoDownloadPath = "images/defaults/parties/party${ nextInt(10) }.jpg"
            DatabaseUtilities.downloadPhoto(
                party.photoDownloadPath,
                { uri ->
                    party.photoUriString = uri.toString()
                    Log.d("New-Party", "Success: Download Default Picture '${ party.photoDownloadPath }'")
                    DatabaseUtilities.saveParty(
                        party,
                        { p->
                            Log.d("New-Party", "Success: '${ party.id }'")
                            successCallback(p)
                        },
                        { e ->
                            Log.e("New-Party", "Failed: '${ party.name }-${ party.theme }'")
                            Log.e("New-Party", e.stackTraceToString())
                            failureCallback(e)
                        })
                },
                { e ->
                    Log.e("New-Party", "Failed: Download Default Picture '${ party.photoDownloadPath }'")
                    Log.e("New-Party", e.stackTraceToString())
                    failureCallback(e)
                }
            )
        }
    }
}