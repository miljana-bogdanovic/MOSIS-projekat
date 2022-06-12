package com.mosis.partyplaces.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mosis.partyplaces.R

class LoginFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameET = requireView().findViewById<EditText>(R.id.login_editText_username)
        val passwordET = requireView().findViewById<EditText>(R.id.login_editText_password)
        val loginButton = requireView().findViewById<Button>(R.id.login_button_login)
        val registerButton = requireView().findViewById<Button>(R.id.login_button_register)

        loginButton.apply {
            isEnabled = false
            setOnClickListener {
                login(usernameET.text.toString(), passwordET.text.toString())
            }
        }

        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegisterFragment)
        }

        val listener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                loginButton.isEnabled =
                    s.toString().isNotBlank() and passwordET.text.toString().isNotBlank()
            }
        }

        usernameET.addTextChangedListener(listener)
        passwordET.addTextChangedListener(listener)
    }

    private fun login(username: String, password: String) {
        Toast.makeText(requireContext(), "Login $username $password !", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_LoginFragment_to_HomeFragment)
    }
}