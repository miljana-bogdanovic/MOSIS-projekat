package com.mosis.partyplaces.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.User

class LoginFragment : Fragment() {
    private val db = Firebase.firestore
    private val loggedUser: LoggedUserViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailET = requireView().findViewById<EditText>(R.id.login_editText_email)
        val passwordET = requireView().findViewById<EditText>(R.id.login_editText_password)
        val loginButton = requireView().findViewById<Button>(R.id.login_button_login)
        val registerButton = requireView().findViewById<Button>(R.id.login_button_register)

        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Login")
        progressDialog.setMessage("Checking your credentials...")

        loginButton.apply {
            isEnabled = false
            setOnClickListener {
                progressDialog.show()
                // Hide keyboard
                (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0);
                login(emailET.text.toString(), passwordET.text.toString(), savedInstanceState)
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

        emailET.addTextChangedListener(listener)
        passwordET.addTextChangedListener(listener)
    }

    private fun login(username: String, password: String, savedInstanceState: Bundle?) {
        Log.d("Sing in", username + password)
        auth.signInWithEmailAndPassword(username, password)
            .addOnSuccessListener { res ->
                authenticationSuccess(res, savedInstanceState)
        }
        .addOnFailureListener {
            Toast.makeText(activity, "Error occurred!", Toast.LENGTH_SHORT).show()
            progressDialog.hide()
        }
    }

    private fun authenticationSuccess(res : AuthResult, savedInstanceState: Bundle?) {
        if (res.user != null) {
            Log.d("LOGGGGGGGGGGGGGGGGGGGGG", res.toString())
            db.collection("users")
                .whereEqualTo("uuid", res.user!!.uid)
                .get()
                .addOnSuccessListener { res2 ->
                    databaseUserResultSuccess(res2, savedInstanceState)
                }
                .addOnFailureListener {
                    onFailure(it)
                }
            return
        }
        onFailure(Exception("Wrong credentials!"), "Wrong credentials!")
    }

    private fun databaseUserResultSuccess(res : QuerySnapshot, savedInstanceState: Bundle?){
        if (res.documents.isNotEmpty()) {
            Log.d("LOGGGGGG", res.documents[0].toString())
            loggedUser.login(res.documents[0].toObject(User::class.java)!!)
            {
                progressDialog.hide()
                findNavController().setGraph(R.navigation.home_graph)
                (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            }
            return
        }

        onFailure(Exception("Account doesn't exist in the database!"), "Seems like this user account was deleted!")
    }
    private fun onFailure(ex : Exception, msg : String = "") {
        progressDialog.hide()
        Log.d("Login", ex.toString())
        if (msg.isNotBlank())
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}