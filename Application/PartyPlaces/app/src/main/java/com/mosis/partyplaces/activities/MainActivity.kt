package com.mosis.partyplaces.activities

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.mosis.partyplaces.R
import com.mosis.partyplaces.data.DatabaseUtilities
import com.mosis.partyplaces.databinding.ActivityMainBinding
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navBar: BottomNavigationView
    private val loggedUser: LoggedUserViewModel by viewModels()
    private var _menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        navBar = findViewById(R.id.bottom_navigation)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.LoginFragment || destination.id == R.id.WelcomeFragment || destination.id == R.id.RegisterFragment) {
                navBar.visibility = View.GONE
            } else {
                navBar.visibility = View.VISIBLE
            }
        }

        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_map -> {
                    when (navController.currentDestination?.id) {
                        R.id.FriendsFragment -> navController.navigate(R.id.action_Friends_To_Maps)
                        R.id.RankFragment -> navController.navigate(R.id.action_Rank_To_Maps)
                        R.id.ProfileFragment -> navController.navigate(R.id.action_Profile_To_Maps)
                        R.id.CreatePartyFragment -> navController.navigate(R.id.action_CreateParty_To_Maps)
                    }
                    true
                }
                R.id.ic_friends -> {
                    when (navController.currentDestination?.id) {
                        R.id.MapsFragment -> navController.navigate(R.id.action_Maps_To_Friends)
                        R.id.RankFragment -> navController.navigate(R.id.action_Rank_To_Friends)
                        R.id.ProfileFragment -> navController.navigate(R.id.action_Profile_To_Friends)
                        R.id.CreatePartyFragment -> navController.navigate(R.id.action_CreateParty_To_Friends)
                    }
                    true
                }
                R.id.ic_rank -> {
                    when (navController.currentDestination?.id) {
                        R.id.MapsFragment -> navController.navigate(R.id.action_Maps_To_Rank)
                        R.id.FriendsFragment -> navController.navigate(R.id.action_Friends_To_Rank)
                        R.id.ProfileFragment -> navController.navigate(R.id.action_Profile_To_Rank)
                        R.id.CreatePartyFragment -> navController.navigate(R.id.action_CreateParty_To_Rank)
                    }
                    true
                }
                R.id.ic_profile -> {
                    when (navController.currentDestination?.id) {
                        R.id.MapsFragment -> navController.navigate(R.id.action_Maps_To_Profile)
                        R.id.FriendsFragment -> navController.navigate(R.id.action_Friends_To_Profile)
                        R.id.RankFragment -> navController.navigate(R.id.action_Rank_To_Profile)
                        R.id.CreatePartyFragment -> navController.navigate(R.id.action_CreateParty_To_Profile)
                    }
                    true
                }
                R.id.ic_event -> {
                    when (navController.currentDestination?.id) {
                        R.id.MapsFragment -> navController.navigate(R.id.action_Maps_To_CreateParty)
                        R.id.FriendsFragment -> navController.navigate(R.id.action_Friends_To_CreateParty)
                        R.id.RankFragment -> navController.navigate(R.id.action_Rank_To_CreateParty)
                        R.id.ProfileFragment -> navController.navigate(R.id.action_Profile_To_CreateParty)
                    }
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }

        loggedUser.getMutable().observe(this) { u ->
            if (u == null) {
                _menu?.findItem(R.id.action_sign_out)?.isVisible = false
                _menu?.findItem(R.id.action_notifications)?.isVisible = false
                getSharedPreferences("Logged-User", MODE_PRIVATE).edit().clear().commit()
                return@observe
            }

            _menu?.findItem(R.id.action_sign_out)?.isVisible = true
            _menu?.findItem(R.id.action_notifications)?.isVisible = true

            getSharedPreferences("Logged-User", MODE_PRIVATE).edit()
                .putString("User", u.toJSON())
                .commit()
        }

        setUpNotifications()
    }

    private fun setUpNotifications(){
        Firebase.messaging.subscribeToTopic("new-party-yRJFxREp4wchWX75tsIlWtdxSqs1")
    }

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        _menu = menu
        loggedUser.user = loggedUser.user
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                Toast.makeText(this, "Notifications!", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_sign_out -> {
                loggedUser.logout()
                {
                    navController.setGraph(R.navigation.welcome_graph)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}