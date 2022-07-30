package com.mosis.partyplaces.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.DatabaseUtilities
import com.mosis.partyplaces.databinding.FragmentProfileBinding
import com.mosis.partyplaces.ui.adapters.PartyListAdapter
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import java.lang.reflect.Type

class ProfileFragment : Fragment() {

    private val loggedUser: LoggedUserViewModel by activityViewModels()
    private lateinit var _binding: FragmentProfileBinding
    private var partyListAdapter : PartyListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding.profileProgressBarProfilePicture.isVisible = true
        _binding.profileTextViewFirstName.text = loggedUser.user!!.firstName
        _binding.profileTextViewLastName.text = loggedUser.user!!.lastName
        _binding.profileTextViewUsername.text = "@" + loggedUser.user!!.username
        _binding.profileTextViewEmail.text = loggedUser.user!!.email
        _binding.profileImageViewProfilePhoto.setImageURI(Uri.parse(loggedUser!!.user!!.profilePhotoUriString))
        _binding.profileTextViewFriendsNo.text = loggedUser.user!!.friendNo.toString()
        _binding.profileTextViewPartiesNo.text = loggedUser.user!!.partyNo.toString()
        _binding.profileTextViewRankNo.text = loggedUser.user!!.rank.toString()

        DatabaseUtilities.getParties(
            loggedUser.user!!.id,
            { parties ->
                partyListAdapter = PartyListAdapter(requireContext(), parties)
                _binding.parties.adapter = partyListAdapter
                if(parties.size < 1){
                    val tv = TextView(requireContext())
                    tv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    tv.gravity = Gravity.CENTER
                    tv.text = getString(R.string.no_parties)
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40f)
                    _binding.partyListLayout.addView(tv)
                }
            },
            { e ->
                Toast.makeText(requireContext(), "Couldn't load parties", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }
}