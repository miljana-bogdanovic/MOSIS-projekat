package com.mosis.partyplaces.ui

import android.content.Context.MODE_PRIVATE
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.User

class LoginFragment : Fragment() {
    private val db = Firebase.firestore
    private val loggedUser: LoggedUserViewModel by activityViewModels()

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
                login(usernameET.text.toString(), passwordET.text.toString(), savedInstanceState)
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

    private fun login(username: String, password: String, savedInstanceState: Bundle?) {
        db.collection("users")
            .whereEqualTo("username", username)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener {
                if(it.documents.isNotEmpty())
                {
                    loggedUser.user = it.documents[0].toObject(User::class.java)
                    requireActivity().getSharedPreferences("LoggedUser", MODE_PRIVATE).edit().apply{
                        putString("value", Gson().toJson(loggedUser.user, User::class.java).toString())
                        commit()
                    }
                    val g = findNavController().navInflater.inflate(R.navigation.nav_graph)
                    g.setStartDestination(R.id.HomeFragment)
                    findNavController().setGraph(g, savedInstanceState)
                }
                else
                    Toast.makeText(requireContext(), "Wrong credentials!", Toast.LENGTH_SHORT).show()
            }
    }
}