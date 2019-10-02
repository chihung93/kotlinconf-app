package org.jetbrains.kotlinconf.ui

import android.graphics.*
import android.os.*
import android.view.*
import androidx.fragment.app.*
import com.google.android.material.bottomsheet.*
import com.google.android.material.tabs.*
import com.mapbox.mapboxsdk.maps.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import org.jetbrains.kotlinconf.*

class MapController : Fragment() {
    private val floors = listOf(
        Style.Builder().fromUri("mapbox://styles/denisvoronov1/cjzikqjgb41rf1cnnb11cv0xw"),
        Style.Builder().fromUri("mapbox://styles/denisvoronov1/cjzsessm40k341clcoer2tn9v")
    )

    private val rooms = mapOf(
        "Hall A1" to 7972,
        "Aud. A11" to 7972,
        "Room 16 -  Keynote" to 7972,
        "Room 16 -  Keynote" to 7972

    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false).apply {
        setupTabs()
        setupMap()
    }

    private fun View.setupTabs() {
        map_tabs.addOnTabSelectedListener(object :
            TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showFloor(tab.position)
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {}
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
        })
    }

    private fun View.setupMap() {
        showFloor(0)

        map_mapview.getMapAsync { map ->
            map.addOnMapClickListener { position ->
                val point = map.projection.toScreenLocation(position)
                val touchZone = RectF(point.x - 10, point.y - 10, point.x + 10, point.y + 10)
                val features = map
                    .queryRenderedFeatures(touchZone)
                    .mapNotNull { it.getStringProperty("name") }

                if (features.isEmpty()) {
                    return@addOnMapClickListener false
                }

                return@addOnMapClickListener true
            }
        }
    }

    private fun View.showFloor(index: Int) {
        map_mapview.getMapAsync {
            it.setStyle(floors[index])
        }
    }
}

class LocationDetailsFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
}