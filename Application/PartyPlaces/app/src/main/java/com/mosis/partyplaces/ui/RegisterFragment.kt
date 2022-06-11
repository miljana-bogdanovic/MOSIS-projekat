package com.mosis.partyplaces.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.mosis.partyplaces.R

class RegisterFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firstNameET = requireView().findViewById<EditText>(R.id.register_editText_first_name)
        val lastNameET = requireView().findViewById<EditText>(R.id.register_editText_last_name)
        val emailET = requireView().findViewById<EditText>(R.id.register_editText_email)
        val usernameET = requireView().findViewById<EditText>(R.id.register_editText_username)
        val passwordET = requireView().findViewById<EditText>(R.id.register_editText_password)

        val registerButton = requireView().findViewById<Button>(R.id.register_button_register)
        registerButton.apply{
            isEnabled = false
            setOnClickListener{
                register(firstNameET.text.toString(), lastNameET.text.toString(), emailET.text.toString(), usernameET.text.toString(), passwordET.text.toString())
            }
        }

        val loginButton = requireView().findViewById<Button>(R.id.register_button_login)
        loginButton.setOnClickListener{
            findNavController().navigate(R.id.action_RegisterFragment_to_LoginFragment)
        }

        val listener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                registerButton.isEnabled = firstNameET.text.isNotBlank() && lastNameET.text.isNotBlank() && emailET.text.isNotBlank() && usernameET.text.isNotBlank() && passwordET.text.isNotBlank()
            }

        }

        firstNameET.addTextChangedListener(listener)
        lastNameET.addTextChangedListener(listener)
        emailET.addTextChangedListener(listener)
        usernameET.addTextChangedListener(listener)
        passwordET.addTextChangedListener(listener)
    }

    private fun register(firstName: String, lastName:String, email:String, username: String, password: String){
        Toast.makeText(requireContext(), "Register $firstName $lastName $email $username $password", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_RegisterFragment_to_HomeFragment)
    }
}