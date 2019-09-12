package org.jetbrains.kotlinconf.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.io.core.Closeable
import org.jetbrains.kotlinconf.ConferenceService
import org.jetbrains.kotlinconf.R
import org.jetbrains.kotlinconf.color
import org.jetbrains.kotlinconf.presentation.SessionCard
import org.jetbrains.kotlinconf.presentation.SessionGroup
import org.jetbrains.kotlinconf.showActivity

class ScheduleController : Fragment() {
    private val schedule by lazy { ScheduleAdapter() }
    private val favorites by lazy { ScheduleAdapter() }
    private val search by lazy { ScheduleAdapter() }

    private lateinit var listView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ConferenceService.schedule.watch {
            schedule.data = it
            schedule.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_schedule, container, false).apply {
        setupSchedule()
        setupTabs()
    }

    private fun View.setupTabs() {
        findViewById<TabLayout>(R.id.schedule_tabs).addOnTabSelectedListener(object :
            TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> listView.adapter = schedule
                    else -> listView.adapter = favorites
                }
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) {}
        })

        findViewById<ImageButton>(R.id.search_button).setOnClickListener {
            listView.adapter = search
        }
    }

    private fun View.setupSchedule() {
        listView = findViewById<RecyclerView>(R.id.schedule_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = schedule

            addItemDecoration(SessionCardDecoration())
        }
    }

    inner class ScheduleAdapter(var data: List<SessionGroup> = emptyList()) :
        RecyclerView.Adapter<SessionCardHolder>() {
        private val TYPE_DAY = 0
        private val TYPE_TIME = 1
        private val TYPE_CARD = 2

        private val sessions get() = data.flatMap { it.sessions }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionCardHolder {
            val holder = when (viewType) {
                TYPE_DAY -> TODO()
                TYPE_TIME -> TODO()
                else -> layoutInflater.inflate(R.layout.schedule_card_view, parent, false)
            }

            return SessionCardHolder(holder)
        }

        override fun getItemCount(): Int = sessions.size

        override fun onBindViewHolder(holder: SessionCardHolder, position: Int) {
            holder.showCard(sessions[position])
        }

        override fun getItemViewType(position: Int): Int {
            return TYPE_CARD
        }
    }
}

class SessionCardHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    var liveWatcher: Closeable? = null

    fun showCard(card: SessionCard) {
        view.apply {
            liveWatcher?.close()

            val session = findViewById<TextView>(R.id.card_session_name)
            val speakers = findViewById<TextView>(R.id.card_session_speakers)
            val location = findViewById<TextView>(R.id.card_location_label)
            val liveIcon = findViewById<ImageView>(R.id.card_live_icon)
            val liveLabel = findViewById<TextView>(R.id.card_live_label)
            val voteButton = findViewById<ImageButton>(R.id.card_vote_button)
            val votePopup = findViewById<View>(R.id.card_vote_popup)

            session.text = card.session.title
            speakers.text = card.speakers.joinToString { it.fullName }
            location.text = card.location.name
            liveLabel.text = "Live now"

            liveWatcher = card.isLive.watch {
                liveIcon.isVisible = it
                liveLabel.isVisible = it
            }

            voteButton.setOnClickListener {
                votePopup.visibility = View.VISIBLE
            }

            setOnClickListener {
                setBackgroundColor(color(R.color.selected_white))
                showActivity<SessionActivity>()
                setBackgroundColor(color(R.color.white))
            }

        }
    }

    companion object {
    }
}

class SessionCardDecoration : RecyclerView.ItemDecoration()
