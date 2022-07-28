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
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.User
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
    private lateinit var photoIV: ImageView
    private lateinit var firstNameET: EditText
    private lateinit var lastNameET: EditText
    private lateinit var emailET: EditText
    private lateinit var usernameET: EditText
    private lateinit var passwordET: EditText
    private lateinit var addImageButton: Button
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private var imageURI: Uri? = null
    private val loggedUser:LoggedUserViewModel by activityViewModels()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoIV = requireView().findViewById(R.id.register_imageView_profile_photo)
        firstNameET = requireView().findViewById(R.id.register_editText_first_name)
        lastNameET = requireView().findViewById(R.id.register_editText_last_name)
        emailET = requireView().findViewById(R.id.register_editText_email)
        usernameET = requireView().findViewById(R.id.register_editText_username)
        passwordET = requireView().findViewById(R.id.register_editText_password)

        addImageButton = requireView().findViewById(R.id.register_button_add_image)
        registerButton = requireView().findViewById(R.id.register_button_register)
        loginButton = requireView().findViewById(R.id.register_button_login)

        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle(R.string.register_fragment_label)
        progressDialog.setMessage("Creating your account...")

        registerButton.apply{
            isEnabled = false
            setOnClickListener{
                progressDialog.show()
                (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0);
                val u = User(firstNameET.text.toString(),
                    lastNameET.text.toString(),
                    emailET.text.toString(),
                    usernameET.text.toString(),
                    passwordET.text.toString(),
                    imageURI?.toString())
                register(u, savedInstanceState)
            }
        }

        loginButton.setOnClickListener{
            findNavController().navigate(R.id.action_RegisterFragment_to_LoginFragment)
        }

        val listener = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                registerButton.isEnabled = firstNameET.text.isNotBlank() && lastNameET.text.isNotBlank() && emailET.text.isNotBlank() && usernameET.text.isNotBlank() && passwordET.text.isNotBlank()
            }
        }

        firstNameET.addTextChangedListener(listener)
        lastNameET.addTextChangedListener(listener)
        emailET.addTextChangedListener(listener)
        usernameET.addTextChangedListener(listener)
        passwordET.addTextChangedListener(listener)

        photoIV.setOnClickListener{
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
            photoIV.setImageURI(imageURI)
            addImageButton.isVisible = false
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
            user.uuid = res.user!!.uid
            user.password = ""
            createAccount(user, savedInstanceState)
        }
        else {
            progressDialog.hide()
            Toast.makeText(activity, "Email is taken!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAccount(user:User, savedInstanceState: Bundle?){
        if(user.imageUri != null) {
            val fileStore: StorageReference = Firebase.storage.reference
            val ref: StorageReference = fileStore.child(
                "images/${user.username}/${
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(Date())
                }.jpg"
            )
            ref.putFile(Uri.parse(user.imageUri!!))
                .addOnCompleteListener {
                    imageUploadSuccessful(it, ref, user, savedInstanceState)
                }
                .addOnFailureListener {
                    onFailure(it)
                }
        }
        else {
            Firebase.storage
                .reference
                .child("images/defaults/profile_picture${nextInt(0, 10)}.jpg")
                .downloadUrl
                .addOnSuccessListener{ uri ->
                    imageDownloadURLSuccess(uri, user,savedInstanceState)
                }
                .addOnFailureListener{
                    onFailure(it)
                }
        }
    }

    private fun imageUploadSuccessful(ts: Task<UploadTask.TaskSnapshot>, ref : StorageReference, user: User, savedInstanceState: Bundle?) : Unit {
        if (!ts.isSuccessful) {
            progressDialog.hide()
            Toast.makeText(requireContext(), "Error uploading the image!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        ref.downloadUrl
            .addOnSuccessListener {
                imageDownloadURLSuccess(it, user, savedInstanceState)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    private fun imageDownloadURLSuccess(url:Uri, user:User, savedInstanceState: Bundle?) : Unit {
        user.downloadUri = url.toString()
        db.collection("users")
            .add(user.toHashMap())
            .addOnSuccessListener {
                userInsertedInDatabaseSuccess(it, user, savedInstanceState)
            }
            .addOnFailureListener{
                onFailure(it)
            }
    }

    private fun userInsertedInDatabaseSuccess(doc : DocumentReference, user : User, savedInstanceState : Bundle?) : Unit{
        loggedUser.login(user)
        {
            progressDialog.hide()
            Toast.makeText(
                requireContext(),
                "Account created successfully!",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().setGraph(R.navigation.home_graph, savedInstanceState)
        }
    }

    private fun onFailure(p0: Exception) {
        Log.d("Register-Error", p0.toString())
        progressDialog.hide()
        Toast.makeText(requireContext(), "Error occurred!", Toast.LENGTH_LONG).show()
    }
}