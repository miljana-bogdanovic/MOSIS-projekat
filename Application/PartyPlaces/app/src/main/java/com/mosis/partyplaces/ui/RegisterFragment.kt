package com.mosis.partyplaces.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.User
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class RegisterFragment : Fragment(), OnFailureListener {

    private val db = Firebase.firestore
    private lateinit var photoIV: ImageView
    private lateinit var firstNameET: EditText
    private lateinit var lastNameET: EditText
    private lateinit var emailET: EditText
    private lateinit var usernameET: EditText
    private lateinit var passwordET: EditText
    private lateinit var addImageButton: Button
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var imageURI: Uri
    private val loggedUser:LoggedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoIV = requireView().findViewById<ImageView>(R.id.register_imageView_profile_photo)
        firstNameET = requireView().findViewById<EditText>(R.id.register_editText_first_name)
        lastNameET = requireView().findViewById<EditText>(R.id.register_editText_last_name)
        emailET = requireView().findViewById<EditText>(R.id.register_editText_email)
        usernameET = requireView().findViewById<EditText>(R.id.register_editText_username)
        passwordET = requireView().findViewById<EditText>(R.id.register_editText_password)

        addImageButton = requireView().findViewById<Button>(R.id.register_button_add_image)
        registerButton = requireView().findViewById<Button>(R.id.register_button_register)
        loginButton = requireView().findViewById<Button>(R.id.register_button_login)

        registerButton.apply{
            isEnabled = false
            setOnClickListener{
                val u = User(firstNameET.text.toString(), lastNameET.text.toString(), emailET.text.toString(), usernameET.text.toString(), passwordET.text.toString(), imageURI.toString())
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

    private fun createAccount(u:User, savedInstanceState: Bundle?){
        val fileStore = Firebase.storage.reference
        val ref = fileStore.child("images/${u.username}/${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(Date())}.jpg")
        ref.putFile(Uri.parse(u.imageUri!!)).addOnCompleteListener { ts ->
            if (!ts.isSuccessful)
                Toast.makeText(requireContext(), "Error uploading the image!", Toast.LENGTH_SHORT)
                    .show()
            else {
                u.downloadUri = ts.result.metadata!!.path
                db.collection("users")
                    .add(u.toHashMap())
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Account created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().getSharedPreferences("LoggedUser", MODE_PRIVATE).edit()
                            .apply {
                                putString("value", Gson().toJson(u, User::class.java).toString())
                                commit()
                            }
                        loggedUser.user = u
                        val g = findNavController().navInflater.inflate(R.navigation.nav_graph)
                        g.setStartDestination(R.id.HomeFragment)
                        findNavController().setGraph(g, savedInstanceState)
                    }
                    .addOnFailureListener(this)
            }
        }
    }

    override fun onFailure(p0: Exception) {
        Toast.makeText(requireContext(), "Error occurred ${p0.toString()}", Toast.LENGTH_LONG).show()
    }
}