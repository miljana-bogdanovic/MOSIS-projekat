package com.mosis.partyplaces.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.mosis.partyplaces.data.User
import kotlin.math.min


class MapsViewModel : ViewModel(), GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var friendsList : List<User>
    //private val storage = Firebase.storage
    private lateinit var navController: NavController

    fun setMap(map:GoogleMap, context: Context, navController: NavController){
        this.map=map
        this.navController=navController
        map.setMaxZoomPreference(18f)
        map.setMinZoomPreference(14f)

        this.friendsList = getFriends()
        friendsList.onEach { f -> f.profilePhotoUriString?.let { setupMarker(f.location.latitude, f.location.longitude, it, context)  } }
        map.setOnMarkerClickListener(this)
    }

    fun getFriends(): List<User> {
        // citanje iz baze
        return listOf(
            User("Miljana",
                "Bogdanovic",
                "miljana1@gmail.com",
                "miljana1",
                "miljana1",
                "",
                "",
                GeoPoint(43.321660, 21.895459)
        ),
            User("Miljana",
                "Bogdanovic",
                "miljana2@gmail.com",
                "miljana2",
                "miljana2",
                "", "",
                GeoPoint(43.321443, 21.895848)))
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        Log.i("INFO", "Click on marker")
        return true
    }

    private fun setupMarker(lat: Double, long: Double, imageUriString: String, context: Context) {
        var bitmap: Bitmap? = null
        val contentResolver: ContentResolver = context.contentResolver
        try {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(imageUriString))
            } else {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(contentResolver, Uri.parse(imageUriString))
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bitmap = bitmap?.let { getCroppedBitmap(it) }

        map.addMarker(
            MarkerOptions().position(
                LatLng(
                    lat, long
                )
            )
                .icon(
                    bitmap?.let { BitmapDescriptorFactory.fromBitmap(bitmap) }
                )
        )
    }

    private fun getCroppedBitmap(bitmap: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        val radius = min( bitmap.height, bitmap.width )
        canvas.drawCircle(
            (radius / 2).toFloat(), (radius / 2).toFloat(),
            (radius / 2).toFloat(), paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output
    }

}