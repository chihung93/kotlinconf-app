package org.jetbrains.kotlinconf.ui.details

import android.view.*
import androidx.annotation.*
import androidx.core.view.*
import androidx.recyclerview.widget.*
import io.ktor.utils.io.core.*
import kotlinx.android.synthetic.main.view_schedule_header_large.view.*
import kotlinx.android.synthetic.main.view_schedule_header_small.view.*
import kotlinx.android.synthetic.main.view_schedule_session_card.view.*
import kotlinx.android.synthetic.main.view_tweet_card.view.tweet_name
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.presentation.*
import org.jetbrains.kotlinconf.ui.*

class SessionCardHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private var liveWatcher: Closeable? = null
    private var favoriteWatcher: Closeable? = null
    private var ratingWatcher: Closeable? = null

    fun show(item: ScheduleItem) {
        when (item) {
            is ScheduleItem.LargeHeader -> showTimeHeader(item.group)
            is ScheduleItem.SmallHeader -> showSmallHeader(item.text, item.color)
            is ScheduleItem.Card -> showCard(item.card)
        }
    }

    private fun showCard(card: SessionCard) {
        val sessionId = card.session.id
        view.apply {
            liveWatcher?.close()
            favoriteWatcher?.close()
            ratingWatcher?.close()

            tweet_name.text = card.session.title
            card_session_speakers.text = card.speakers.joinToString { it.fullName }
            card_location_label.text = card.location.name
            card_live_label.text = "Live now"

            liveWatcher = card.isLive.watch {
                card_live_icon.isVisible = it
                card_live_label.isVisible = it
            }

            favoriteWatcher = card.isFavorite.watch {
                val image = if (it) {
                    R.drawable.favorite_black
                } else {
                    R.drawable.favorite_empty
                }
                card_favorite_button.setImageResource(image)
            }

            ratingWatcher = card.ratingData.watch { rating ->
                card_vote_good.setImageResource(if (rating == RatingData.GOOD) R.drawable.good_orange else R.drawable.good_empty)
                card_vote_ok.setImageResource(if (rating == RatingData.OK) R.drawable.ok_orange else R.drawable.ok_empty)
                card_vote_bad.setImageResource(if (rating == RatingData.BAD) R.drawable.bad_orange else R.drawable.bad_empty)

                val voteResource = when (rating) {
                    RatingData.GOOD -> R.drawable.good_orange
                    RatingData.OK -> R.drawable.ok_orange
                    RatingData.BAD -> R.drawable.bad_orange
                    else -> R.drawable.good_empty
                }

                card_vote_button.setImageResource(voteResource)
            }

            fun vote(rating: RatingData) {
                card_vote_popup.visibility = View.INVISIBLE
                ConferenceService.vote(sessionId, rating)
            }

            card_vote_button.setOnClickListener { card_vote_popup.visibility = View.VISIBLE }
            card_vote_good.setOnClickListener { vote(RatingData.GOOD) }
            card_vote_ok.setOnClickListener { vote(RatingData.OK) }
            card_vote_bad.setOnClickListener { vote(RatingData.BAD) }

            card_favorite_button.setOnClickListener {
                ConferenceService.markFavorite(
                    sessionId
                )
            }

            setOnTouchListener { view, event ->
                view.session_card.setPressedColor(event, R.color.white, R.color.selected_white)

                if (event.action == MotionEvent.ACTION_UP) {
                    showActivity<SessionActivity> {
                        putExtra("session", sessionId)
                    }
                }

                true
            }
        }
    }

    private fun showTimeHeader(group: SessionGroup) {
        with(view) {
            schedule_header_time.text = group.title
            schedule_header_day.text = "${group.day}\n${group.month}"
        }
    }

    private fun showSmallHeader(text: String, @ColorRes color: Int) {
        with(view) {
            schedule_header_text.apply {
                this.text = text.repeat(10)
                setTextColor(color(color))
            }
        }
    }
}

sealed class ScheduleItem(val type: Int) {
    class LargeHeader(val group: SessionGroup) : ScheduleItem(TYPE_LARGE)
    class SmallHeader(val text: String, @ColorRes val color: Int) : ScheduleItem(TYPE_SMALL)
    class Card(val card: SessionCard) : ScheduleItem(TYPE_CARD)

    companion object {
        val TYPE_LARGE = 0
        val TYPE_SMALL = 1
        val TYPE_CARD = 2
    }
}