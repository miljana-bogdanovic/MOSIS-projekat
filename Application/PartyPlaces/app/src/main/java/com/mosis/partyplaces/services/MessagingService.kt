package com.mosis.partyplaces.services

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Handler(Looper.getMainLooper()).post(Runnable {
            Toast.makeText(application.applicationContext, message.notification?.body, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }
}