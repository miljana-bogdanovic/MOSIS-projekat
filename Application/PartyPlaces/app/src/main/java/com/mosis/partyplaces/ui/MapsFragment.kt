package com.mosis.partyplaces.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.mosis.partyplaces.R
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.mosis.partyplaces.viewmodels.MapsViewModel

class MapsFragment : Fragment()  {
    private val loggedUser: LoggedUserViewModel by activityViewModels()
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private val mapsViewModel: MapsViewModel by viewModels()

    private lateinit var map:GoogleMap

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap
        map.clear()
        mapsViewModel.setMap(map,requireContext(),findNavController())

        val zoomLevel = 17f

        if(isLocationPermissionGranted(requireContext())) {
            enableMyLocation()
            if(fusedLocationClient != null){
                fusedLocationClient?.lastLocation?.addOnCompleteListener {
                    if(it.result != null) {
                        val latlng = LatLng(
                            it.result.latitude,
                            it.result.longitude
                        )
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                latlng, zoomLevel
                            )
                        )
                        lastLocation = it.result
                        loggedUser.user!!.location = GeoPoint(latlng.latitude, latlng.longitude)
                    }
                }
            }else{
                setupLocationTrackingWithLocation()
            }
        }
        else{
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    private fun setupLocationTracking(){
        if(isLocationPermissionGranted(requireContext())){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        }else{
            requestPermissionLauncherFLC.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    @SuppressLint("MissingPermission")
    private fun setupLocationTrackingWithLocation(){
        if(isLocationPermissionGranted(requireContext())){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient?.lastLocation?.addOnCompleteListener {
                if(it.result!=null) {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.result.latitude,
                                it.result.longitude
                            ), 17f
                        )
                    )
                    lastLocation=it.result
                }
            }
        }else{
            requestPermissionLauncherFLC.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private val requestPermissionLauncherFLC = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
            isGranted: Boolean->
        if(isGranted){
            setupLocationTracking()
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isLocationPermissionGranted(requireContext())) {
            map.isMyLocationEnabled = true
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
            isGranted: Boolean->
        if(isGranted){
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        setupLocationTracking()
        mapFragment?.getMapAsync(callback)

        val partiesBtn = requireView().findViewById<Button>(R.id.buttonParties)
        val friendsBtn = requireView().findViewById<Button>(R.id.buttonFriends)
        val usersBtn = requireView().findViewById<Button>(R.id.buttonUsers)

        partiesBtn.apply {
            setOnClickListener {
                // show Parties
                partiesBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.purple_500, null))
            }
        }

        friendsBtn.apply {
            setOnClickListener {
                // show friends
                friendsBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.green, null))
            }
        }

        usersBtn.apply {
            setOnClickListener {
                // show users
                usersBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.blue, null))
            }
        }

    }

    private fun isLocationPermissionGranted(context: Context):Boolean{
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

}