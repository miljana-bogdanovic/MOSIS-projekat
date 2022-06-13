package com.mosis.partyplaces.viewmodels

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mosis.partyplaces.data.User

class LoggedUserViewModel(var user: User? = null) : ViewModel() {
}