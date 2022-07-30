package com.mosis.partyplaces.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.DatabaseUtilities
import com.mosis.partyplaces.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private val db = Firebase.firestore
    private val loggedUser: LoggedUserViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var _binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Login")
        progressDialog.setMessage("Checking your credentials...")

        _binding.buttonLogin.apply {
            setOnClickListener {
                // Hide keyboard
                (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
                if(validateData()) {
                    progressDialog.show()
                    login(
                        _binding.editTextEmail.text.toString(),
                        _binding.editTextPassword.text.toString(),
                        savedInstanceState
                    )
                }
            }
        }

        _binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegisterFragment)
        }

        _binding.editTextEmail.addTextChangedListener { textChanged(_binding.textInputEmail, it) }
        _binding.editTextPassword.addTextChangedListener { textChanged(_binding.textInputPassword, it) }
    }

    private fun validateData() : Boolean {

        var t = true
        if(_binding.editTextEmail.text.isBlank()){
            t = false
            _binding.textInputEmail.error = getString(R.string.field_cannot_be_empty)
        }
        if(_binding.editTextPassword.text.isBlank()){
            t = false
            _binding.textInputPassword.error = getString(R.string.field_cannot_be_empty)
        }
        return t
    }

    private fun textChanged(view:TextInputLayout, ed:Editable?){
        if(ed != null && ed.isNotBlank()){
            view.error = null
        }
    }

    private fun login(username: String, password: String, savedInstanceState: Bundle?) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnSuccessListener { res ->
                DatabaseUtilities.loadUserWithPhoto(res.user!!.uid){
                    loggedUser.login(it)
                    {
                        progressDialog.hide()
                        findNavController().setGraph(R.navigation.home_graph, savedInstanceState)
                    }
                }
        }
        .addOnFailureListener {
            Toast.makeText(activity, "Error occurred!", Toast.LENGTH_SHORT).show()
            progressDialog.hide()
        }
    }
    private fun onFailure(ex : Exception, msg : String = "") {
        progressDialog.hide()
        Log.d("Login", ex.toString())
        if (msg.isNotBlank())
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}