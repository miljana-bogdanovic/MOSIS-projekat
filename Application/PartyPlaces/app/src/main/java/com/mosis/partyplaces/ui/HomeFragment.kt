package com.mosis.partyplaces.ui

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.mosis.partyplaces.R

class HomeFragment : Fragment() {

    private val loggedUser: LoggedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireView().findViewById<TextView>(R.id.home_textView).text = "Hello ${loggedUser.user!!.firstName}!"
        requireView().findViewById<ImageView>(R.id.home_imageView).setImageURI(Uri.parse(loggedUser.user!!.imageUri))
    }
}