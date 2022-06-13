package com.mosis.partyplaces.ui

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.User

class LoadingFragment : Fragment() {

    private val loggedUser: LoggedUserViewModel by activityViewModels()
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val g = findNavController().navInflater.inflate(R.navigation.nav_graph)
        val lu = requireActivity().getSharedPreferences("LoggedUser", MODE_PRIVATE).getString("value", "")
        if(lu!!.isNotBlank())
            validateUser(Gson().fromJson(lu!!, User::class.java), g, savedInstanceState)
        else {
            g.setStartDestination(R.id.WelcomeFragment)
            findNavController().setGraph(g, savedInstanceState)
        }
    }

    private fun validateUser(user: User, graph: NavGraph, savedInstanceState: Bundle?){
        db.collection("users")
            .whereEqualTo("username", user.username)
            .get()
            .addOnSuccessListener {
                if(it.documents.isNotEmpty() && it.documents[0].data!!["password"] == user.password) {
                    loggedUser.login(requireActivity(), it.documents[0].toObject(User::class.java) as User) {
                        graph.setStartDestination(R.id.HomeFragment)
                        findNavController().setGraph(graph, savedInstanceState)
                    }
                }
                else {
                    Toast.makeText(requireContext(), "Credentials changed!", Toast.LENGTH_SHORT).show()
                    loggedUser.logout(requireActivity())
                    {
                        graph.setStartDestination(R.id.WelcomeFragment)
                        findNavController().setGraph(graph, savedInstanceState)
                    }
                }
            }
    }
}