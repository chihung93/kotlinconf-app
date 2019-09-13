package org.jetbrains.kotlinconf.ui

import android.os.*
import android.view.*
import androidx.fragment.app.*
import com.google.android.material.bottomsheet.*
import com.google.android.material.tabs.*
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.*
import com.mapbox.mapboxsdk.maps.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import org.jetbrains.kotlinconf.*

class MapController : Fragment() {
    private val floors = listOf(
        Style.Builder().fromUri("mapbox://styles/denisvoronov1/cjzikqjgb41rf1cnnb11cv0xw"),
        Style.Builder().fromUri("mapbox://styles/denisvoronov1/cjzsessm40k341clcoer2tn9v")
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
    }

    private fun View.showFloor(index: Int) {
        map_mapview.getMapAsync {
            it.setStyle(floors[index])
        }
    }
}

class LocationDetailsFragment : BottomSheetDialogFragment() {

}