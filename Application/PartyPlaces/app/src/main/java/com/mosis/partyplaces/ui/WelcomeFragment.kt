package com.mosis.partyplaces.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.mosis.partyplaces.R

class WelcomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = requireView().findViewById<Button>(R.id.welcome_button_login)
        loginButton.setOnClickListener{
            findNavController().navigate(R.id.action_WelcomeFragment_to_LoginFragment)
        }
        val registerButton = requireView().findViewById<Button>(R.id.welcome_button_register)
        registerButton.setOnClickListener{
            findNavController().navigate(R.id.action_WelcomeFragment_to_RegisterFragment)
        }
    }
}