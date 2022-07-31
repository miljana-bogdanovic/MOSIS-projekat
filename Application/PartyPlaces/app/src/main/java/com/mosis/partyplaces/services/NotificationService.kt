package com.mosis.partyplaces.services

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.mosis.partyplaces.activities.NotificationActivity


class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Handler(Looper.getMainLooper()).post(Runnable {
            Toast.makeText(
                applicationContext,
                message.notification?.body,
                Toast.LENGTH_LONG
            ).show()

            /*val myIntent = Intent(applicationContext, NotificationActivity::class.java)
            val bundle = Bundle()
            bundle.putString("notification", Gson().toJson(message))
            myIntent.putExtras(bundle)
            applicationContext.startActivity(myIntent)*/
        })
    }

}