package com.mosis.partyplaces.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mosis.partyplaces.R
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel


class LoadingFragment : Fragment() {
    private val loggedUser: LoggedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingTV = requireView().findViewById<TextView>(R.id.loading_fragment_textView)
        loadingTV.text = "Loading..."

        // If User is in shared preferences
        loggedUser.checkSharedPreferences(requireActivity(), fun() {
            findNavController().setGraph(R.navigation.home_graph, savedInstanceState)
        }, fun(){
            findNavController().setGraph(R.navigation.welcome_graph, savedInstanceState)
        })
    }
}