package org.jetbrains.kotlinconf.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import org.jetbrains.kotlinconf.R

class MapController : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false).apply {
        setupMap()
    }

    private fun View.setupMap() {
        val map = findViewById<MapView>(R.id.map_mapview)
        map.getMapAsync {
            it.setStyle(Style.Builder().fromUri("mapbox://styles/denisvoronov1/cjzikqjgb41rf1cnnb11cv0xw"))
        }
    }
}