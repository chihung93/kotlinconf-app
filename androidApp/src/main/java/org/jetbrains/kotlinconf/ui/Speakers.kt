package org.jetbrains.kotlinconf.ui

import android.graphics.*
import android.os.*
import android.view.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.*
import kotlinx.android.synthetic.main.fragment_speakers.view.*
import kotlinx.android.synthetic.main.view_speakers_list_item.view.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.R

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
        speakers_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = speakers

            addItemDecoration(SpeakerViewDecoration())
        }
    }

    private inner class SpeakersAdapter(
        var speakers: List<SpeakerData> = emptyList()
    ) : RecyclerView.Adapter<SpeakerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeakerViewHolder {
            val holder = layoutInflater.inflate(R.layout.view_speakers_list_item, parent, false)
            return SpeakerViewHolder(holder)
        }

        override fun getItemCount(): Int = speakers.size

        override fun onBindViewHolder(holder: SpeakerViewHolder, position: Int) {
            val speaker = speakers[position]
            holder.update(speaker)
        }
    }
}

private class SpeakerViewHolder(private val speakerView: View) : RecyclerView.ViewHolder(speakerView) {
    fun update(speaker: SpeakerData) {
        val picture = speaker.profilePicture

        speakerView.apply {
            setOnClickListener {
                setBackgroundColor(color(R.color.selected_white))
                speakerView.showActivity<SpeakerActivity> {
                    putExtra("speaker", speaker.id)
                }
                setBackgroundColor(color(R.color.white))
            }

            speaker_name.text = speaker.fullName.toUpperCase()
            speaker_description.text = speaker.tagLine

            Glide.with(this)
                .load(picture)
                .into(speaker_photo)
        }
    }
}

private class SpeakerViewDecoration(
    private val spacing: Int = 8
) : RecyclerView.ItemDecoration() {
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
