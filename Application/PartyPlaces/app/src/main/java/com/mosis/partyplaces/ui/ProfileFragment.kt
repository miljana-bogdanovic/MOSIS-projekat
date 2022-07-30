package com.mosis.partyplaces.ui

import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mosis.partyplaces.R
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.callbackFlow
import java.lang.Exception

class ProfileFragment : Fragment() {

    private val loggedUser: LoggedUserViewModel by activityViewModels()
    private lateinit var uuidTV: TextView
    private lateinit var firstNameTV: TextView
    private lateinit var lastNameTV: TextView
    private lateinit var usernameTV: TextView
    private lateinit var emailTV: TextView
    private lateinit var password: TextView
    private lateinit var photoIV: ImageView
    private lateinit var pBar:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pBar = requireView().findViewById(R.id.profile_progressBar_profile_picture)
        pBar.isVisible = true
        uuidTV = requireView().findViewById(R.id.profile_textView_uuid)
        firstNameTV = requireView().findViewById(R.id.profile_textView_first_name)
        lastNameTV = requireView().findViewById(R.id.profile_textView_last_name)
        usernameTV = requireView().findViewById(R.id.profile_textView_username)
        emailTV = requireView().findViewById(R.id.profile_textView_email)
        password = requireView().findViewById(R.id.profile_textView_password)
        photoIV = requireView().findViewById(R.id.profile_imageView_profile_photo)

        uuidTV.text = loggedUser.user!!.uuid
        firstNameTV.text = loggedUser.user!!.firstName
        lastNameTV.text = loggedUser.user!!.lastName
        usernameTV.text = loggedUser.user!!.username
        emailTV.text = loggedUser.user!!.email
        password.text = loggedUser.user!!.password
        photoIV.setImageURI(Uri.parse(loggedUser!!.user!!.profilePhotoUriString))
    }
}