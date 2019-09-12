package org.jetbrains.kotlinconf.ui

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.kotlinconf.*

class SpeakersController : Fragment() {
    private val speakers by lazy { SpeakersAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ConferenceService.speakers.watch {
            speakers.speakers = it
            speakers.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_speakers, container, false).apply {
        setupSpeakers()
    }

    private fun View.setupSpeakers() {
        findViewById<RecyclerView>(R.id.speakers_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = speakers

            addItemDecoration(SpeakerViewDecoration())
        }
    }

    inner class SpeakersAdapter(
        var speakers: List<SpeakerData> = emptyList()
    ) : RecyclerView.Adapter<SpeakerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeakerViewHolder {
            val holder = layoutInflater.inflate(R.layout.speaker_card_view, parent, false)
            return SpeakerViewHolder(holder)
        }

        override fun getItemCount(): Int = speakers.size

        override fun onBindViewHolder(holder: SpeakerViewHolder, position: Int) {
            val speaker = speakers[position]
            holder.update(speaker)
        }
    }
}

class SpeakerViewHolder(private val speakerView: View) : RecyclerView.ViewHolder(speakerView) {
    fun update(speaker: SpeakerData) {
        val picture = speaker.profilePicture

        speakerView.apply {
            setOnClickListener {
                setBackgroundColor(color(R.color.selected_white))
                speakerView.showActivity<SpeakerActivity>()
                setBackgroundColor(color(R.color.white))
            }

            val photo = findViewById<ImageView>(R.id.speaker_photo)
            val name = findViewById<TextView>(R.id.speaker_name)
            val description = findViewById<TextView>(R.id.speaker_description)

            name.text = speaker.fullName.toUpperCase()
            description.text = speaker.tagLine

            if (picture == null) {
                photo.setImageBitmap(null)
                return
            }

            ConferenceService.findPicture(picture) {
                photo.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
            }
        }
    }
}

class SpeakerViewDecoration(private val spacing: Int = 8) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = spacing.dp
        outRect.top = spacing.dp
    }
}
