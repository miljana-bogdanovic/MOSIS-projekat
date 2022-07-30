package com.mosis.partyplaces.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.GeoPoint
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.Party
import com.mosis.partyplaces.data.UserLite
import com.mosis.partyplaces.databinding.FragmentCreatePartyBinding
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.mosis.partyplaces.viewmodels.PartiesViewModel
import java.io.File
import java.io.FileOutputStream
import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class CreatePartyFragment : Fragment() {

    private var imageURI: Uri? = null
    private val loggedUser: LoggedUserViewModel by activityViewModels()
    private val parties: PartiesViewModel by activityViewModels()
    private lateinit var _binding: FragmentCreatePartyBinding
    private lateinit var chosenLocation: GeoPoint
    private lateinit var pDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreatePartyBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding.editTextDate.setOnClickListener { dateClicked(it) }
        _binding.editTextStartTime.setOnClickListener {
            timeClicked(it, "start"){ tp ->
                _binding.editTextStartTime.setText("${ tp.hour.toString().padStart(2, '0') }:${ tp.minute.toString().padStart(2, '0') }h")
            }
        }
        _binding.editTextEndTime.setOnClickListener {
            timeClicked(it, "end"){ tp ->
                _binding.editTextEndTime.setText("${ tp.hour.toString().padStart(2, '0') }:${ tp.minute.toString().padStart(2, '0') }h")
            }
        }
        _binding.editTextLocation.setOnClickListener { locationClicked(it) }

        _binding.editTextName.addTextChangedListener { textChanged(_binding.textInputLayoutName, it) }
        _binding.editTextTheme.addTextChangedListener { textChanged(_binding.textInputLayoutTheme, it) }
        _binding.editTextDate.addTextChangedListener { textChanged(_binding.textInputLayoutDate, it) }
        _binding.editTextStartTime.addTextChangedListener { textChanged(_binding.textInputLayoutStartTime, it) }
        _binding.editTextEndTime.addTextChangedListener { textChanged(_binding.textInputLayoutEndTime, it) }
        _binding.editTextLocation.addTextChangedListener { textChanged(_binding.textInputLayoutLocation, it) }

        chosenLocation = loggedUser.user!!.location

        val results = Geocoder(requireContext())
            .getFromLocation(chosenLocation.latitude, chosenLocation.longitude, 1)
        val firstResult = results[0]
        _binding.editTextLocation.setText(firstResult.getAddressLine(0))

        _binding.buttonCreateParty.setOnClickListener{
            createANewParty()
        }

        _binding.buttonCancel.setOnClickListener{
            clearContents()
        }

        pDialog = Dialog(requireContext(), android.R.style.ThemeOverlay_Material_Dialog_Alert)
        pDialog.setContentView(R.layout.dialog_progress)
        pDialog.findViewById<TextView>(R.id.label).text = "Creating your party"
        pDialog.findViewById<TextView>(R.id.description).text = "Preparing decorations"

        _binding.imageViewPartyPhoto.setOnClickListener{
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
            _binding.imageViewPartyPhoto.setImageURI(imageURI)
            _binding.buttonAddImage.isVisible = false
        }
    }

    private fun createANewParty(){
        if(validateFields()) {
            pDialog.show()
            parties.newParty(
                Party(
                    _binding.editTextName.text.toString(),
                    _binding.editTextTheme.text.toString(),
                    SimpleDateFormat("dd/MM/yyyy").parse(_binding.editTextDate.text.toString())!!,
                    _binding.editTextStartTime.text.toString(),
                    _binding.editTextEndTime.text.toString(),
                    chosenLocation,
                    _binding.editTextLocation.text.toString(),
                    loggedUser.user!! as UserLite,
                    if(imageURI != null) imageURI.toString() else ""

                ),
                { p ->
                    pDialog.hide()
                    clearContents()
                    Toast.makeText(requireActivity(), "Created '${p.name}' party!", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_CreateParty_To_Maps)
                },
                { e->
                    pDialog.hide()
                    Toast.makeText(requireActivity(), "Couldn't create the party!", Toast.LENGTH_SHORT).show()
                    Log.e("Create-New-Party", e.stackTraceToString())
                })
        }
    }

    private fun textChanged(v:TextInputLayout, it:Editable?){
        if(it != null && it!!.isNotBlank())
            v.error = null
    }

    private fun validateFields():Boolean{
        var t = true
        if(_binding.editTextName.text.isEmpty()){
            t = false
            _binding.textInputLayoutName.error = getString(R.string.create_party_name_empty_message)
        }
        if(_binding.editTextTheme.text.isEmpty()){
            t = false
            _binding.textInputLayoutTheme.error = getString(R.string.create_party_theme_empty_message)
        }
        if(_binding.editTextDate.text.isEmpty()){
            t = false
            _binding.textInputLayoutDate.error = getString(R.string.create_party_date_empty_message)
        }
        if(_binding.editTextStartTime.text.isEmpty()){
            t = false
            _binding.textInputLayoutStartTime.error = getString(R.string.create_party_start_time_empty_message)
        }
        if(_binding.editTextEndTime.text.isEmpty()){
            t = false
            _binding.textInputLayoutEndTime.error = getString(R.string.create_party_end_time_empty_message)
        }
        if(_binding.editTextLocation.text.isEmpty()){
            t = false
            _binding.textInputLayoutLocation.error = getString(R.string.create_party_location_empty_message)
        }
        return t
    }

    private fun clearContents(){
        _binding.editTextName.setText("")
        _binding.editTextTheme.setText("")
        _binding.editTextDate.setText("")
        _binding.editTextStartTime.setText("")
        _binding.editTextEndTime.setText("")
        _binding.editTextLocation.setText("")
    }

    private fun dateClicked(v:View){
        val constraints = CalendarConstraints.Builder()
        constraints.setValidator(DateValidatorPointForward.now())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("What day is the party?")
                .setCalendarConstraints(constraints.build())
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        datePicker.addOnPositiveButtonClickListener {
            _binding.editTextDate.setText(SimpleDateFormat("dd/MM/yyyy").format(Date(it)).toString())
        }

        datePicker.show(requireActivity().supportFragmentManager, "")
    }

    private fun timeClicked(v : View, label: String, handler: (MaterialTimePicker) -> Unit){
        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("When does the party ${ label }?")
                .build()

        timePicker.addOnPositiveButtonClickListener{
            handler(timePicker)
        }

        timePicker.show(requireActivity().supportFragmentManager, "")
    }

    private fun locationClicked(v:View){
        val dialog = Dialog(requireContext(), android.R.style.ThemeOverlay_Material_Dialog_Alert)
        dialog.setContentView(R.layout.map_select_location_dialog)

        val chosenAddress = dialog.findViewById<TextView>(R.id.chosen_location)
        val mMapView = dialog.findViewById<MapView>(R.id.mapView)
        MapsInitializer.initialize(requireActivity())
        mMapView.onCreate(dialog.onSaveInstanceState())
        mMapView.onResume()

        chosenAddress.text = _binding.editTextLocation.text.toString()

        mMapView.getMapAsync { googleMap ->
            val position = LatLng(loggedUser.user!!.location.latitude, loggedUser.user!!.location.longitude)
            var marker = googleMap.addMarker(MarkerOptions()
                .position(position)
                .title(chosenAddress.text.toString()))
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f), 2000, null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(position))

            googleMap.setOnMapClickListener {
                marker?.remove()
                val results = Geocoder(requireContext())
                    .getFromLocation(it.latitude, it.longitude, 1)
                val firstResult = results[0]
                chosenAddress.text = firstResult.getAddressLine(0)
                chosenLocation = GeoPoint(it.latitude, it.longitude)
                marker = googleMap.addMarker(MarkerOptions().position(it).title(chosenAddress.text.toString()))
            }
        }

        dialog.findViewById<Button>(R.id.map_select_button).setOnClickListener {
            dialog.dismiss()
            _binding.editTextLocation.setText(chosenAddress.text.toString())
        }

        dialog.findViewById<Button>(R.id.map_cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}