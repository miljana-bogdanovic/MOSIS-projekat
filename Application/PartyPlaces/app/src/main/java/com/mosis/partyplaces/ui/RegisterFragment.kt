package com.mosis.partyplaces.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.DatabaseUtilities
import com.mosis.partyplaces.data.User
import com.mosis.partyplaces.databinding.FragmentRegisterBinding
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random.Default.nextInt

class RegisterFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var imageURI: Uri? = null
    private val loggedUser : LoggedUserViewModel by activityViewModels()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var _binding : FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle(R.string.register_fragment_label)
        progressDialog.setMessage("Creating your account...")

        _binding.registerButtonRegister.apply{
            setOnClickListener{
                (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
                if(validateData()) {
                    progressDialog.show()
                    register(
                        User(
                        _binding.registerEditTextFirstName.text.toString(),
                        _binding.registerEditTextLastName.text.toString(),
                        _binding.registerEditTextEmail.text.toString(),
                        _binding.registerEditTextUsername.text.toString(),
                        _binding.registerEditTextPassword.text.toString(),
                        "", if(imageURI != null) imageURI.toString() else ""
                        ),
                        savedInstanceState)
                }
            }
        }

        _binding.registerButtonLogin.setOnClickListener{
            findNavController().navigate(R.id.action_RegisterFragment_to_LoginFragment)
        }

        _binding.registerImageViewProfilePhoto.setOnClickListener{
            (activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle(R.string.pick_image_input_method)
                    .setItems(arrayOf("Camera", "Gallery"))
                        { dialog, which ->
                            val i = Intent()
                            when (which) {
                                0 -> i.action = MediaStore.ACTION_IMAGE_CAPTURE
                                1 -> {
                                    i.action = Intent.ACTION_GET_CONTENT
                                    i.type = "image/*"
                                }
                            }
                            getResult.launch(i)
                        }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")).show()
        }

        _binding.registerEditTextFirstName.addTextChangedListener { textChanged(_binding.textInputFirstName, it) }
        _binding.registerEditTextLastName.addTextChangedListener { textChanged(_binding.textInputLastName, it) }
        _binding.registerEditTextEmail.addTextChangedListener { textChanged(_binding.textInputEmail, it) }
        _binding.registerEditTextUsername.addTextChangedListener { textChanged(_binding.textInputUsername, it) }
        _binding.registerEditTextPassword.addTextChangedListener { textChanged(_binding.textInputPassword, it) }
    }

    private fun textChanged(view: TextInputLayout, txt:Editable?){
        if(txt != null && txt.isNotEmpty()){
            view.error = null
        }
    }

    private fun validateData() : Boolean {
        var t = true

        if(_binding.registerEditTextFirstName.text.isEmpty()){
            t = false
            _binding.textInputFirstName.error = getString(R.string.field_cannot_be_empty)
        }

        if(_binding.registerEditTextLastName.text.isEmpty()){
            t = false
            _binding.textInputLastName.error = getString(R.string.field_cannot_be_empty)
        }

        if(_binding.registerEditTextEmail.text.isEmpty()){
            t = false
            _binding.textInputEmail.error = getString(R.string.field_cannot_be_empty)
        }

        if(_binding.registerEditTextUsername.text.isEmpty()){
            t = false
            _binding.textInputUsername.error = getString(R.string.field_cannot_be_empty)
        }

        if(_binding.registerEditTextPassword.text.isEmpty()){
            t = false
            _binding.textInputPassword.error = getString(R.string.field_cannot_be_empty)
        }

        return t
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            if (it.data!!.data != null)
                imageURI = it.data!!.data!!
            else if (it.data!!.extras?.get("data") != null) {
                val bmp = it.data!!.extras!!.get("data") as Bitmap
                val f = File.createTempFile("JPEG_${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(Date())}_", "jpg")
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(f))
                imageURI = f.toUri()
            }
            else {
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            _binding.registerImageViewProfilePhoto.setImageURI(imageURI)
            _binding.registerButtonAddImage.isVisible = false
        }
    }

    private fun register(user: User, savedInstanceState: Bundle?){
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnSuccessListener {
                authenticationSuccess(user, it, savedInstanceState)
            }
            .addOnFailureListener{
                onFailure(it)
            }
    }

    private fun authenticationSuccess(user : User, res : AuthResult, savedInstanceState: Bundle?) {
        if(res.user != null) {
            user.id = res.user!!.uid
            user.password = ""
            createAccount(user, savedInstanceState)
        }
        else {
            progressDialog.hide()
            Toast.makeText(activity, "Email is taken!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAccount(user:User, savedInstanceState: Bundle?){
        if(user.profilePhotoUriString.isNotEmpty()) {
            DatabaseUtilities.saveUserWithPhoto(
                user,
                { u ->
                    loggedUser.login(u)
                    {
                        progressDialog.hide()
                        findNavController().setGraph(R.navigation.home_graph)
                    }
                },
                { e -> onFailure(e) }
            )
        }
        else {
            user.profilePhotoDownloadPath = "images/defaults/profiles/profile${nextInt(0, 10)}.jpg"
            DatabaseUtilities.downloadPhoto(
                user.profilePhotoDownloadPath,
                { uri ->
                    user.profilePhotoUriString = uri.toString()
                    DatabaseUtilities.saveUser(
                        user,
                        { u ->
                            loggedUser.login(u) {
                                progressDialog.hide()
                                findNavController().setGraph(R.navigation.home_graph)
                            }
                        },
                        { e ->
                            onFailure(e)
                        })
                },
                { e -> onFailure(e) }
            )
        }
    }

    private fun onFailure(p: Exception){
        Log.e("Register-Error", p.stackTraceToString())
        progressDialog.hide()
        Toast.makeText(requireContext(), "Error occurred!", Toast.LENGTH_LONG).show()
    }
}