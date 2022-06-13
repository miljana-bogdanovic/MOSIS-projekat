package com.mosis.partyplaces.ui

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.User
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.lang.Exception

class RegisterFragment : Fragment(), OnFailureListener {

    private val db = Firebase.firestore

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
                val u = User(firstNameET.text.toString(), lastNameET.text.toString(), emailET.text.toString(), usernameET.text.toString(), passwordET.text.toString())
                register(u, savedInstanceState)
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

    private fun register(u: User, savedInstanceState: Bundle?){
        Log.d("REGISTER", u.toString())
        db.collection("users")
            .whereEqualTo("email", u.email)
            .get()
            .addOnSuccessListener { qs ->
                if(qs.documents.isEmpty())
                {
                    db.collection("users")
                        .whereEqualTo("username", u.username)
                        .get()
                        .addOnSuccessListener { qs2 ->
                            if(qs2.documents.isEmpty())
                                createAccount(u, savedInstanceState)
                            else
                                Toast.makeText(requireContext(), "Username is already used!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener(this)
                }
                else
                    Toast.makeText(requireContext(), "Email is already used!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener(this)
    }

    private fun createAccount(u:User, savedInstanceState: Bundle?):Unit{
        db.collection("users")
            .add(u.toHashMap())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                requireActivity().getSharedPreferences("LoggedUser", MODE_PRIVATE).edit().apply {
                    putString("value", Gson().toJson(u, User::class.java).toString())
                    commit()
                }
                val g = findNavController().navInflater.inflate(R.navigation.nav_graph)
                g.setStartDestination(R.id.HomeFragment)
                findNavController().setGraph(g, savedInstanceState)
            }
            .addOnFailureListener(this)
    }

    override fun onFailure(p0: Exception) {
        Toast.makeText(requireContext(), "Error occurred ${p0.toString()}", Toast.LENGTH_LONG).show()
    }
}