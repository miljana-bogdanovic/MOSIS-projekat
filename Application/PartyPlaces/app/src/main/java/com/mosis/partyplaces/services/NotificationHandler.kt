package com.mosis.partyplaces.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.mosis.partyplaces.activities.NotificationActivity

class NotificationHandler(private val context: Context, private val message : RemoteMessage) : Runnable {

    override fun run() {

    }
}