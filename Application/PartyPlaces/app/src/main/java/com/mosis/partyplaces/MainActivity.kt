package com.mosis.partyplaces

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mosis.partyplaces.databinding.ActivityMainBinding
import com.mosis.partyplaces.viewmodels.LoggedUserViewModel
import java.util.*


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

        loggedUser.getMutable().observe(this) { u ->
            if(u != null) {
                _menu?.findItem(R.id.action_sign_out)?.isVisible = true
                _menu?.findItem(R.id.action_notifications)?.isVisible = true
            }
        }

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
                        R.id.friendsFragment -> navController.navigate(R.id.action_Friends_To_Maps)
                        R.id.rankFragment -> navController.navigate(R.id.action_Rank_To_Maps)
                        R.id.profileFragment -> navController.navigate(R.id.action_Profile_To_Maps)
                    }
                    true
                }
                R.id.ic_friends -> {
                    when (navController.currentDestination?.id) {
                        R.id.MapsFragment -> navController.navigate(R.id.action_Maps_to_Friends)
                        R.id.rankFragment -> navController.navigate(R.id.action_Rank_to_Friends)
                        R.id.profileFragment -> navController.navigate(R.id.action_Profile_to_Friends)
                    }
                    true
                }
                R.id.ic_rank -> {
                    when (navController.currentDestination?.id) {
                        R.id.MapsFragment -> navController.navigate(R.id.action_Maps_to_Rank)
                        R.id.friendsFragment -> navController.navigate(R.id.action_Friends_to_Rank)
                        R.id.profileFragment -> navController.navigate(R.id.action_Profile_To_Rank)
                    }
                    true
                }
                R.id.ic_profile -> {
                    when (navController.currentDestination?.id) {
                        R.id.MapsFragment -> navController.navigate(R.id.action_Maps_to_Profile)
                        R.id.friendsFragment -> navController.navigate(R.id.action_Friends_to_Profile)
                        R.id.rankFragment -> navController.navigate(R.id.action_Rank_to_Profile)
                    }
                    true
                }
                R.id.ic_event -> {
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }

        }

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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                Toast.makeText(this, "Notifications!", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_sign_out -> {
                loggedUser.logout(this)
                {
                    var graph = navController.navInflater.inflate(R.navigation.nav_graph)
                    graph.setStartDestination(R.id.LoadingFragment)
                    navController.setGraph(graph, null)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}