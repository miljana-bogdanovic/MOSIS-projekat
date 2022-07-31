package com.mosis.partyplaces.data

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*


object DatabaseUtilities {

    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val realTimeDB = Firebase.database

    fun savePhoto(
        photoUri : Uri,
        uploadPathString : String,
        successCallback: (UploadTask.TaskSnapshot) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        storage.reference
            .child(uploadPathString)
            .putFile(photoUri)
            .addOnSuccessListener {
                Log.d("Save-Photo", "Success: '$uploadPathString'")
                successCallback(it)
            }
            .addOnFailureListener {
                Log.e("Save-Photo", "Failed: '$uploadPathString'")
                Log.e("Save-Photo", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun deletePhoto(
        photoUriPath : String,
        successCallback: (Void?) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}){
        storage.reference
            .child(photoUriPath)
            .delete()
            .addOnSuccessListener {
                Log.d("Delete-Photo", "Success: '$photoUriPath'")
                successCallback(it)
            }
            .addOnFailureListener{
                Log.e("Delete-Photo", "Failed: '$photoUriPath'")
                Log.e("Delete-Photo", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun loadUser(
        uuid : String,
        successCallback: (User) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        firestore
            .collection("users")
            .whereEqualTo("id", uuid)
            .get()
            .addOnSuccessListener { doc->
                if(!doc.isEmpty && doc.documents.size > 0) {
                    Log.d("Load-User", "Success: $uuid")
                    successCallback(doc.documents[0].toObject()!!)
                }
                else{
                    Log.e("Load-User", "Failed: '$uuid'")
                    failureCallback(Exception("Couldn't find the user '$uuid'"))
                }
            }
            .addOnFailureListener {
                Log.e("Load-User", "Failed: $uuid")
                Log.e("Load-User", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun loadUserWithPhoto(
        uuid: String,
        successCallback: (User) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        loadUser(
            uuid,
            { u ->
                Log.d("Load-User-With-Photo", "Success: Load User '$uuid'")
                downloadPhoto(
                    u.profilePhotoDownloadPath,
                    { photoUri ->
                        u.profilePhotoUriString = photoUri.toString()
                        Log.d("Load-User-With-Photo", "Success: Download Photo '${ u.profilePhotoDownloadPath }'")
                        successCallback(u)
                    },
                    {
                        Log.e("Load-User-With-Photo", "Failed: Download Photo '${ u.profilePhotoDownloadPath }'")
                        Log.e("Load-User-With-Photo", it.stackTraceToString())
                        failureCallback(Exception("Couldn't download the photo ${ u.profilePhotoDownloadPath }"))
                    })
            },
            {
                Log.e("Load-User-With-Photo", "Failed: Load User '$uuid'")
                Log.e("Load-User-With-Photo", it.stackTraceToString())
                failureCallback(it)
            }
        )
    }

    fun downloadPhoto(
        sourcePath : String,
        successCallback: (Uri) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ) {
        if(cachedPhotos.containsKey(sourcePath)) {
            successCallback(cachedPhotos[sourcePath]!!)
            return
        }

        val f = File.createTempFile("images", ".jpg")
        storage.reference
            .child(sourcePath)
            .getFile(f)
            .addOnSuccessListener{
                cachedPhotos[sourcePath] = f.toUri()
                Log.d("Download-Photo", "Success: '$sourcePath'")
                successCallback(f.toUri())
            }
            .addOnFailureListener {
                Log.e("Download-Photo", "Failed: '$sourcePath'")
                Log.e("Download-Photo", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun saveUserWithPhoto(
        user : User,
        successCallback: (User) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        user.profilePhotoDownloadPath = "users/${ user.email }/photos/profile_picture.jpg"
        savePhoto(
            Uri.parse(user.profilePhotoUriString),
            user.profilePhotoDownloadPath,
            {
                Log.d("Save-User-With-Photo", "Success: Saving Photo '${ user.profilePhotoDownloadPath }'")
                saveUser(
                    user,
                    {
                        Log.d("Save-User-With-Photo", "Success: Saving User '${ user.id }'")
                        successCallback(user)
                    },
                    {
                        Log.e("Save-User-With-Photo", "Failed: Saving User '${ user.id }'")
                        Log.e("Save-User-With-Photo", it.stackTraceToString())
                        failureCallback(it)
                    }
                )
            },
            {
                Log.e("Save-User-With-Photo", "Failed: Saving Photo '${ user.profilePhotoDownloadPath }'")
                Log.e("Save-User-With-Photo", it.stackTraceToString())
                failureCallback(it)
            }
        )
    }

    fun saveUser(
        user: User,
        successCallback: (User) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        firestore.collection("users")
            .document(user.id)
            .set(user)
            .addOnSuccessListener { _ ->
                Log.d("Save-User", "Success: '${ user.id }'")
                successCallback(user)
            }
            .addOnFailureListener {
                Log.e("Save-User", "Failed: '${ user.id }'")
                Log.e("Save-User", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun updateUser(
        userid : String,
        update : MutableMap<String, Any>,
        successCallback: (Void?) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        firestore.collection("users")
            .document(userid)
            .update(update)
            .addOnSuccessListener{
                Log.d("Update-User", "Success: '$userid'")
                successCallback(it)
            }
            .addOnFailureListener {
                Log.e("User-Update", "Failed '$userid'")
                Log.e("User-Update", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun saveParty(
        p : Party,
        successCallback: (Party) -> Unit = {},
        failureCallback: ((Exception) -> Unit) = {}
    ){
        p.id = UUID.randomUUID().toString()
        firestore.collection("parties")
            .document(p.id)
            .set(p)
            .addOnSuccessListener { _ ->
                Log.d("Save-Party", "Success: '${ p.id }'")
                successCallback(p)
            }
            .addOnFailureListener{
                Log.e("Save-Party", "Failed: ${ p.name }-${ p.theme }")
                Log.e("Save-Party", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun savePartyWithPhoto(
        p : Party,
        successCallback: (Party) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        p.photoDownloadPath =
            """parties/
                photos/
                party_
                ${ SimpleDateFormat("dd-MM-yyyy-hh-mm-ss-SSS").format(Date())}_
                ${ UUID.randomUUID() }
                .jpg"""
        savePhoto(
            Uri.parse(p.photoUriString),
            p.photoDownloadPath,
            {
                Log.d("Save-Party-With-Photo", "Success: Saving Photo '${ p.photoDownloadPath }'")
                saveParty(
                    p,
                    {
                        Log.d("Save-Party-With-Photo", "Success: Saving Party '${ p.id }'")
                        successCallback(p)
                    },
                    {
                        Log.e("Save-Party-With-Photo", "Failed: Saving Party '${ p.name }-${ p.theme }'")
                        Log.e("Save-Party-With-Photo", it.stackTraceToString())
                        successCallback(p)
                    })
            },
            {
                Log.e("Save-Party-With-Photo", "Failed: Saving Photo ${ p.photoDownloadPath }")
                Log.e("Save-Party-With-Photo", it.stackTraceToString())
                failureCallback(it)
            }
        )
    }
    fun updateParty(
        uid:String,
        update: MutableMap<String, Any>,
        successCallback: (Void?) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        firestore.collection("parties")
            .document(uid)
            .update(update)
            .addOnSuccessListener {
                Log.d("Update-Party", "Success: '$uid'")
                successCallback(it)
            }
            .addOnFailureListener{
                Log.e("Update-Party", "Failed: '$uid'")
                Log.e("Update-Party", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun deletePartyWithPhoto(
        uid : String,
        photoUriPath: String,
        successCallback: (Void?) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        deleteParty(
            uid,
            {
                Log.d("Delete-Party-With-Photo", "Success: Delete Party '$uid'")
                deletePhoto(
                    photoUriPath,
                    {
                        Log.d("Delete-Party-With-Photo", "Success: Delete Photo '$photoUriPath'")
                        successCallback(it)
                    },
                    {
                        Log.e("Delete-Party-With-Photo", "Failed: Delete Photo '$photoUriPath'")
                        Log.e("Delete-Party-With-Photo", it.stackTraceToString())
                        failureCallback(it)
                    })
            },
            {
                Log.e("Delete-Party-With-Photo", "Failed: Delete Party '$uid'")
                Log.e("Delete-Party-With-Photo", it.stackTraceToString())
                failureCallback(it)
            }
        )
    }

    fun deleteParty(
        uid : String,
        successCallback: (Void?) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        firestore.collection("parties")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("Delete-Party", "Success: '$uid'")
                successCallback(it)
            }
            .addOnFailureListener{
                Log.e("Delete-Party", "Failed: '$uid'")
                Log.e("Delete-Party", it.stackTraceToString())
                failureCallback(it)
            }
    }

    fun getParties(
        userId : String,
        successCallback: (MutableList<Party>) -> Unit = {},
        failureCallback: (Exception) -> Unit = {}
    ){
        firestore.collection("parties")
            .whereEqualTo("organizer.id", userId)
            .orderBy("day", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { qs ->
                Log.d("Get-Parties", "Success: '$userId' -> ${ qs.size() }")
                successCallback(qs.toObjects(Party::class.java))
            }
            .addOnFailureListener { e->
                Log.e("Get-Parties", "Failed: '$userId'")
                Log.e("Get-Parties", e.stackTraceToString())
                failureCallback(e)
            }
    }

    private val cachedPhotos : MutableMap<String, Uri> = mutableMapOf()
}