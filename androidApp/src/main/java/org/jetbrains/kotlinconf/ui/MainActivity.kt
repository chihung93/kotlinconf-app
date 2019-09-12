package org.jetbrains.kotlinconf.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.kotlinconf.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setupNavigationBar()
    }

    private fun setupNavigationBar() {
        val view: BottomNavigationView = findViewById(R.id.nav_view)
        val controller = findNavController(R.id.nav_host_fragment)
        view.setupWithNavController(controller)
    }
}