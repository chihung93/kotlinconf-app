package org.jetbrains.kotlinconf.ui

import android.content.res.*
import android.os.*
import androidx.appcompat.app.*
import androidx.navigation.*
import androidx.navigation.ui.*
import com.mapbox.mapboxsdk.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setupNavigationBar()
    }

    private fun setupNavigationBar() {
        val controller = findNavController(R.id.nav_host_fragment)
        bottom_navigation.setupWithNavController(controller)

        Mapbox.getInstance(
            this, MAPBOX_ACCESS_TOKEN
        )
    }
}