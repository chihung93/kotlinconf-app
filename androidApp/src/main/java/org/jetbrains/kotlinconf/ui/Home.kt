package org.jetbrains.kotlinconf.ui

import android.os.*
import android.view.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.*
import com.google.android.youtube.player.*
import io.ktor.utils.io.core.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.view_dont_miss_card.view.*
import kotlinx.android.synthetic.main.view_session_live_card.view.*
import kotlinx.android.synthetic.main.view_tweet_card.view.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.BuildConfig.*
import org.jetbrains.kotlinconf.R
import org.jetbrains.kotlinconf.presentation.*

class HomeController : Fragment() {
    private val liveCards by lazy { LiveCardsAdapter() }
    private val reminders by lazy { RemaindersAdapter() }
    private val feed by lazy { FeedAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ConferenceService.liveSessions.watch {
            liveCards.data = it
            liveCards.notifyDataSetChanged()
        }

        ConferenceService.upcomingFavorites.watch {
            reminders.data = it
            reminders.notifyDataSetChanged()
        }

        ConferenceService.feed.watch {
            feed.data = it.statuses
            feed.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false).apply {
        setupLiveCards()
        setupRemainders()
        setupTwitter()

        showActivity<WelcomeActivity>()
    }

    private fun View.setupLiveCards() {
        live_cards_container.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false).apply {
                isNestedScrollingEnabled = true
            }
            adapter = liveCards
        }
    }

    private fun View.setupRemainders() {
        dont_miss_block.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reminders
        }
    }

    private fun View.setupTwitter() {
        tweet_feed_list.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false).apply {
                isNestedScrollingEnabled = true
            }
            adapter = feed
        }
    }

    inner class LiveCardsAdapter(
        var data: List<SessionCard> = emptyList()
    ) : RecyclerView.Adapter<LiveCardHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveCardHolder {
            val view = layoutInflater.inflate(
                R.layout.view_session_live_card, parent, false
            ).apply {
                live_video_view.initialize(YOUTUBE_API_KEY, LiveCardHolder)
            }

            return LiveCardHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: LiveCardHolder, position: Int) {
            holder.setupCard(data[position])
        }
    }

    inner class RemaindersAdapter(
        var data: List<SessionCard> = emptyList()
    ) : RecyclerView.Adapter<RemainderHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemainderHolder {
            val view = layoutInflater.inflate(R.layout.view_dont_miss_card, parent, false)
            return RemainderHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: RemainderHolder, position: Int) {
            holder.setupCard(data[position])
        }
    }

    inner class FeedAdapter(
        var data: List<FeedPost> = emptyList()
    ) : RecyclerView.Adapter<TweetHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetHolder {
            val view = layoutInflater.inflate(R.layout.view_tweet_card, parent, false)
            return TweetHolder(view)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: TweetHolder, position: Int) {
            holder.showPost(data[position])
        }
    }
}

class LiveCardHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private var favoriteSubscription: Closeable? = null

    fun setupCard(sessionCard: SessionCard) {
        favoriteSubscription?.close()

        with(view) {
            setOnTouchListener { view, event ->
                val action = event.action

                view.live_card.setPressedColor(
                    event,
                    R.color.dark_grey_card,
                    R.color.dark_grey_card_pressed
                )
                if (action == MotionEvent.ACTION_UP) {
                    showActivity<SessionActivity> {
                        putExtra("session", sessionCard.session.id)
                    }
                }

                true
            }

            sessionCard.roomVideo?.let {
                live_video_view.tag = it
            }
            live_session_title.text = sessionCard.session.title
            live_session_speakers.text = sessionCard.speakers.joinToString { it.fullName }
            live_location.text = sessionCard.location.name
            live_favorite.setOnClickListener {
                ConferenceService.markFavorite(sessionCard.session.id)
            }

            favoriteSubscription = sessionCard.isFavorite.watch {
                val image = if (it) {
                    R.drawable.favorite_orange
                } else {
                    R.drawable.favorite_white_empty
                }

                live_favorite.setImageResource(image)
            }
        }
    }

    companion object ThumbnailListener : YouTubeThumbnailView.OnInitializedListener {
        override fun onInitializationFailure(
            view: YouTubeThumbnailView,
            loader: YouTubeInitializationResult
        ) {
        }

        override fun onInitializationSuccess(
            view: YouTubeThumbnailView,
            loader: YouTubeThumbnailLoader
        ) {
            val tag = view.tag as? String ?: return
            loader.setVideo(tag)
        }
    }
}

class RemainderHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private var favoriteSubscription: Closeable? = null

    fun setupCard(sessionCard: SessionCard) {
        favoriteSubscription?.close()

        with(view) {
            setOnTouchListener { view, event ->
                view.setPressedColor(event, R.color.dark_grey_card, R.color.dark_grey_card_pressed)

                if (event.action == MotionEvent.ACTION_DOWN) {
                    showActivity<SessionActivity> {
                        putExtra("session", sessionCard.session.id)
                    }
                }
                true
            }

            card_session_title.text = sessionCard.session.title
            card_session_speakers.text = sessionCard.speakers.joinToString { it.fullName }
            card_location_label.text = sessionCard.location.name

            card_favorite_button.setOnTouchListener { view, event ->


                ConferenceService.markFavorite(sessionCard.session.id)
                true
            }

            favoriteSubscription = sessionCard.isFavorite.watch {
                val image = if (it) {
                    R.drawable.favorite_orange
                } else {
                    R.drawable.favorite_white_empty
                }
                card_favorite_button.setImageResource(image)
            }
        }
    }
}

class TweetHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun showPost(post: FeedPost) {
        with(view) {
            Glide.with(view)
                .load(post.user.profile_image_url_https)
                .into(tweet_avatar)

            tweet_account.text = "@${post.user.screen_name}"
            tweet_name.text = post.user.name
            tweet_text.text = post.text
            tweet_time.text = post.created_at
        }
    }
}
