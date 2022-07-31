package com.mosis.partyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.view.isVisible
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.DatabaseUtilities
import com.mosis.partyplaces.data.Party
import com.mosis.partyplaces.data.toObject
import com.mosis.partyplaces.databinding.ActivityMainBinding
import java.text.SimpleDateFormat

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(savedInstanceState == null) {
            finish()
            return
        }

        val e = savedInstanceState.getString("notification")
        if (e == null){
            finish()
            return
        }

        val message = Gson().fromJson(e, RemoteMessage::class.java)

        val payload = message.data
        when(payload.getOrDefault("notification-type", "")) {
            getString(R.string.notification_new_party) -> {
                val p = payload.getOrDefault("body", "")
                val party = p.toObject<Party>()
                setContentView(R.layout.notification_new_party)

                findViewById<Button>(R.id.details).setOnClickListener {
                    Toast.makeText(this, "Details", Toast.LENGTH_SHORT).show()
                }

                findViewById<TextView>(R.id.textView_username).text = party.organizer.username
                DatabaseUtilities.downloadPhoto(party.organizer.profilePhotoDownloadPath,{uri->
                    findViewById<ImageView>(R.id.profile_imageView_profile_photo).setImageURI(uri)
                    findViewById<ProgressBar>(R.id.profile_progressBar_profile_picture).isVisible = false
                })

                findViewById<TextView>(R.id.name).text = party.name
                findViewById<TextView>(R.id.location).text = party.address
                findViewById<TextView>(R.id.theme).text = party.theme
                findViewById<TextView>(R.id.date).text = SimpleDateFormat("dd.MM.yyyy").format(party.day)
                findViewById<TextView>(R.id.score).text = party.score.toString()
                findViewById<TextView>(R.id.guestNo).text = party.guestNo.toString()
            }
            getString(R.string.notification_friend_request_sent) -> {

            }
            getString(R.string.notification_friend_request_accepted) -> {

            }
            getString(R.string.notification_comming_to_party) -> {

            }
            getString(R.string.notification_guest_arrived) -> {

            }
            else -> {
                val content = message.notification?.body
                if (content != null)
                    Toast.makeText(this, message.notification!!.body, Toast.LENGTH_SHORT).show()
            }
        }
    }
}