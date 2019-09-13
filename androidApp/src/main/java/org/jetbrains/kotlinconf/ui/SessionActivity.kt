package org.jetbrains.kotlinconf.ui

import android.os.*
import android.view.*
import androidx.appcompat.app.*
import androidx.recyclerview.widget.*
import com.google.android.youtube.player.*
import io.ktor.utils.io.core.*
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.android.synthetic.main.view_session_speakers_item.view.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.BuildConfig.*

class SessionActivity : AppCompatActivity() {
    private var favoriteWatcher: Closeable? = null
    private var ratingWatcher: Closeable? = null
    private var liveWatcher: Closeable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_session)
        val sessionId = intent.getStringExtra("session")
        showSession(sessionId)
    }

    override fun onDestroy() {
        super.onDestroy()

        favoriteWatcher?.close()
        ratingWatcher?.close()
        liveWatcher?.close()
    }

    private fun showSession(id: String) {
        val card = ConferenceService.sessionCard(id)
        val session = card.session

        session_title.text = session.title.toUpperCase()

        session_speaker_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<SpeakerNameViewHolder>() {
                private val speakers = card.speakers

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): SpeakerNameViewHolder {
                    val view = layoutInflater.inflate(
                        R.layout.view_session_speakers_item, parent, false
                    )
                    return SpeakerNameViewHolder(view)
                }

                override fun getItemCount(): Int {
                    return speakers.size
                }

                override fun onBindViewHolder(holder: SpeakerNameViewHolder, position: Int) {
                    val speaker = speakers[position]
                    holder.view.session_speaker.apply {
                        text = speaker.fullName

                        setOnClickListener {
                            showActivity<SpeakerActivity> {
                                putExtra("speaker", speaker.id)
                            }
                        }
                    }
                }
            }
        }

        session_location_text.text = card.location.name
        session_description.text = session.descriptionText
        session_time_label.text = "${card.date} ${card.time}"

        favoriteWatcher = card.isFavorite.watch {
            val image = if (it) {
                R.drawable.favorite_white
            } else {
                R.drawable.favorite_white_empty
            }

            session_favorite.setImageResource(image)
        }

        ratingWatcher = card.ratingData.watch { rating ->
            session_vote_good.setImageResource(if (rating == RatingData.GOOD) R.drawable.good_orange else R.drawable.good_empty)
            session_vote_ok.setImageResource(if (rating == RatingData.OK) R.drawable.ok_orange else R.drawable.ok_empty)
            session_vote_bad.setImageResource(if (rating == RatingData.BAD) R.drawable.bad_orange else R.drawable.bad_empty)

            val voteResource = when (rating) {
                RatingData.GOOD -> R.drawable.good_white
                RatingData.OK -> R.drawable.ok_white
                RatingData.BAD -> R.drawable.bad_white
                else -> R.drawable.good_white_empty
            }

            session_vote.setImageResource(voteResource)
        }

        val sessionId = card.session.id

        fun vote(rating: RatingData) {
            session_vote_popup.visibility = View.INVISIBLE
            ConferenceService.vote(sessionId, rating)
        }

        session_vote.setOnClickListener { session_vote_popup.visibility = View.VISIBLE }
        session_vote_good.setOnClickListener { vote(RatingData.GOOD) }
        session_vote_ok.setOnClickListener { vote(RatingData.OK) }
        session_vote_bad.setOnClickListener { vote(RatingData.BAD) }

        session_favorite.setOnClickListener { ConferenceService.markFavorite(sessionId) }

        val videoView = fragmentManager
            .findFragmentById(R.id.session_video_view) as LiveVideoFragment

        liveWatcher = card.isLive.watch {
            val videoId = card.roomVideo
            if (it && videoId != null) {
                session_video_box.visibility = View.VISIBLE
                videoView.showVideo(videoId)
            } else {
                session_video_box.visibility = View.GONE
            }
        }
    }

}

class LiveVideoFragment : YouTubePlayerFragment(), YouTubePlayer.OnInitializedListener {
    private var player: YouTubePlayer? = null
    private var videoId: String? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        initialize(YOUTUBE_API_KEY, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        player = null
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider,
        newPlayer: YouTubePlayer,
        restored: Boolean
    ) {
        player = newPlayer
        videoId?.let { newPlayer.loadVideo(it) }
    }

    fun showVideo(id: String) {
        videoId = id
        player?.loadVideo(id)
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        result: YouTubeInitializationResult
    ) {
    }
}

private class SpeakerNameViewHolder(val view: View) : RecyclerView.ViewHolder(view)