package com.mosis.partyplaces.data

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.File


object DatabaseUtilities {

    private val realTimeDB = Firebase.firestore
    private val storage = Firebase.storage

    fun loadUser(uuid : String, successCallback: (User) -> Unit) {
        if(cachedUsers.containsKey(uuid)){
            successCallback(cachedUsers[uuid]!!)
            return
        }

        realTimeDB
            .collection("users")
            .whereEqualTo("uuid", uuid)
            .get()
            .addOnSuccessListener { doc->
                if(!doc.isEmpty && doc.documents.size > 0) {
                    cachedUsers[uuid] = doc.documents[0].toObject()!!
                    successCallback(cachedUsers[uuid]!!)
                }
            }
            .addOnFailureListener {
                Log.e("Error", it.stackTraceToString())
            }
    }

    fun loadUserWithPhoto(uuid: String, successCallback: (User) -> Unit){
        loadUser(uuid){ u ->
            downloadPhoto(u.profilePhotoDownloadPath) { photoUri ->
                u.profilePhotoUriString = photoUri.toString()
                successCallback(u)
            }
        }
    }

    fun downloadPhoto(sourcePath : String, successCallback: (Uri) -> Unit) {
        if(cachedPhotos.containsKey(sourcePath)) {
            successCallback(cachedPhotos[sourcePath]!!)
            return
        }

        var f = File.createTempFile("images", ".jpg")
        storage.reference
            .child(sourcePath)
            .getFile(f)
            .addOnSuccessListener{
                cachedPhotos[sourcePath] = f.toUri()
                successCallback(f.toUri())
            }
            .addOnFailureListener {
                Log.e("Error", it.stackTraceToString())
            }
    }

    fun savePhoto(photoUri : Uri, uploadPathString : String, successCallback: (UploadTask.TaskSnapshot) -> Unit){
        storage.reference
            .child(uploadPathString)
            .putFile(photoUri)
            .addOnSuccessListener {
                successCallback(it)
            }
            .addOnFailureListener{
                Log.e("Error", it.stackTraceToString())
            }
    }

    fun saveUserWithPhoto(user : User, successCallback: (User) -> Unit){
        user.profilePhotoDownloadPath = "users/${ user.email }/photos/profile_picture.jpg"
        savePhoto(Uri.parse(user.profilePhotoUriString), user.profilePhotoDownloadPath){
            saveUser(user){
                successCallback(it)
            }
        }
    }

    fun saveUser(user: User, successCallback: (User) -> Unit){
        realTimeDB.collection("users")
            .add(user)
            .addOnSuccessListener { docRef ->
                if(docRef != null)
                    successCallback(user)
            }
            .addOnFailureListener {
                Log.e("Error", it.stackTraceToString())
            }
    }

    private val cachedPhotos : MutableMap<String, Uri> = mutableMapOf()
    private val cachedUsers : MutableMap<String, User> = mutableMapOf()
}