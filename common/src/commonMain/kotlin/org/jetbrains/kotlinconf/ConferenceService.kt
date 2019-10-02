package org.jetbrains.kotlinconf

import io.ktor.util.date.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import org.jetbrains.kotlinconf.presentation.*
import org.jetbrains.kotlinconf.storage.*
import kotlin.collections.set
import kotlin.coroutines.*
import kotlin.native.concurrent.*

/**
 * [ConferenceService] handles data and builds model.
 */
@ThreadLocal
@UseExperimental(ExperimentalCoroutinesApi::class)
object ConferenceService : CoroutineScope {
    private val exceptionHandler = object : CoroutineExceptionHandler {
        override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

        override fun handleException(context: CoroutineContext, exception: Throwable) {
            _errors.offer(exception)
        }
    }

    private val _errors = ConflatedBroadcastChannel<Throwable>()
    val errors = _errors.asFlow().wrap()

    override val coroutineContext: CoroutineContext = dispatcher() + SupervisorJob() + exceptionHandler

    private val storage: ApplicationStorage = ApplicationStorage()
    private var userId: String? = null
    private var titleScreenShown: Boolean = false

    /**
     * Cached.
     */
    private var cards: MutableMap<String, SessionCard> = mutableMapOf()

    private val notificationManager = NotificationManager()
    /**
     * ------------------------------
     * Observables.
     * ------------------------------
     */

    /**
     * Public conference information.
     */
    private val _publicData by storage.live { SessionizeData() }
    val publicData = _publicData.asFlow().wrap()

    /**
     * Favorites list.
     */
    private val _favorites by storage.live { mutableSetOf<String>() }
    val favorites = _favorites.asFlow().wrap()

    private val _votes by storage.live { mutableMapOf<String, RatingData>() }
    private val _feed = ConflatedBroadcastChannel<FeedData>(FeedData())

    /**
     * Votes list.
     */
    val votes = _votes.asFlow().wrap()

    val feed = _feed.asFlow().wrap()

    private var _videos: List<LiveVideo> = emptyList()
    /**
     * Live sessions.
     */
    private val _liveSessions = ConflatedBroadcastChannel<Set<String>>(mutableSetOf())
    private val _upcomingFavorites = ConflatedBroadcastChannel<Set<String>>(mutableSetOf())

    val liveSessions = _liveSessions.asFlow().map {
        it.toList().map { id -> sessionCard(id) }
    }.wrap()

    val upcomingFavorites = _upcomingFavorites.asFlow().map {
        it.toList().map { id -> sessionCard(id) }
    }.wrap()

    val schedule = publicData.map {
        it.sessions.groupByDay()
            .addDayStart()
    }.wrap()

    val favoriteSchedule = favorites.map {
        it.map { id -> session(id) }
            .groupByDay()
            .addDayStart()
    }.wrap()

    val speakers = publicData.map { it.speakers }.wrap()

    val sessions = publicData.map {
        it.sessions.sortedBy { it.title }.map { sessionCard(it.id) }
    }.wrap()

    init {
        launch {
            userId?.let { Api.sign(it) }

            if (_publicData.value.sessions.isEmpty()) {
                refresh()
            }
        }

        launch {
            while (true) {
                scheduledUpdate()
                delay(60 * 1000)
            }
        }
    }

    /**
     * ------------------------------
     * Representation.
     * ------------------------------
     */
    /**
     * Check if session is favorite.
     */
    fun sessionIsFavorite(sessionId: String): Boolean = sessionId in _favorites.value

    /**
     * Get session rating.
     */
    fun sessionRating(sessionId: String): RatingData? = _votes.value[sessionId]

    /**
     * Get speakers from session.
     */
    fun sessionSpeakers(sessionId: String): List<SpeakerData> {
        val speakerIds = session(sessionId).speakers
        return speakerIds.map { speaker(it) }
    }

    /**
     * Get sessions for speaker.
     */
    fun speakerSessions(speakerId: String): List<SessionCard> {
        val sessionIds = speaker(speakerId).sessions
        return sessionIds.map { sessionCard(it) }
    }

    /**
     * Find speaker by id.
     */
    fun speaker(id: String): SpeakerData =
        _publicData.value.speakers.find { it.id == id } ?: error("Internal error. Speaker with id $id not found.")

    /**
     * Find session by id.
     */
    fun session(id: String): SessionData =
        _publicData.value.sessions.find { it.id == id } ?: error("Internal error. Session with id $id not found.")

    /**
     * Find room by id.
     */
    fun room(id: Int): RoomData =
        _publicData.value.rooms.find { it.id == id } ?: error("Internal error. Room with id $id not found.")

    /**
     * Get session card.
     */
    fun sessionCard(id: String): SessionCard {
        cards[id]?.let { return it }

        val session = session(id)
        val roomId = session.roomId ?: error("No room id in session: ${session.id}")

        val location = room(roomId)
        val speakers = sessionSpeakers(id)
        val isFavorite = favorites.map { id in it }.wrap()
        val ratingData = votes.map { it[id] }.wrap()
        val isLive = _liveSessions.asFlow().map { id in it }.wrap()

        val result = SessionCard(
            session,
            session.startsAt.dayAndMonth(),
            "${session.startsAt.time()}-${session.endsAt.time()}",
            location,
            _videos.find { it.room == location.id }?.videoId,
            speakers,
            isFavorite,
            ratingData,
            isLive
        )

        cards[id] = result
        return result
    }

    /**
     * ------------------------------
     * User actions.
     * ------------------------------
     */
    /**
     * Accept privacy policy clicked.
     */
    fun acceptPrivacyPolicy() {
        if (userId != null) return

        val id = generateUserId().also {
            userId = it
        }

        launch {
            Api.sign(id)
        }
    }

    fun requestNotificationPermissions() {
        launch {
            notificationManager.requestPermission()
        }
    }

    /**
     * Vote for session.
     */
    fun vote(sessionId: String, rating: RatingData) {
        launch {
            val userId = userId ?: throw Unauthorized()

            val votes = _votes.value
            val oldRating = votes[sessionId]
            val newRating = if (rating == oldRating) null else rating

            try {
                updateVote(sessionId, newRating)

                if (newRating != null) {
                    val vote = VoteData(sessionId, rating)
                    Api.postVote(userId, vote)
                } else {
                    Api.deleteVote(userId, sessionId)
                }
            } catch (cause: Throwable) {
                updateVote(sessionId, oldRating)
                throw cause
            }

        }
    }

    /**
     * Mark session as favorite.
     */
    fun markFavorite(sessionId: String) {
        launch {
            val userId = userId ?: throw Unauthorized()

            val isFavorite = !(sessionId in _favorites.value)

            try {
                updateFavorite(sessionId, isFavorite)
                if (isFavorite) {
                    Api.postFavorite(userId, sessionId)
                    notificationManager.schedule("", "", GMTDate() + 5000)
                } else {
                    Api.deleteFavorite(userId, sessionId)
                }
            } catch (cause: Throwable) {
                updateFavorite(sessionId, !isFavorite)
            }
        }
    }


    /**
     * Reload data model from server.
     */
    fun refresh() {
        launch {
            Api.getAll(userId).apply {
                _videos = liveVideos
                _publicData.offer(allData)
                _favorites.offer(favorites.toMutableSet())
                val votesMap = mutableMapOf<String, RatingData>().apply {
                    putAll(votes.mapNotNull { vote -> vote.rating?.let { vote.sessionId to it } })
                }
                _votes.offer(votesMap)

                scheduledUpdate()
            }
        }
    }

    private suspend fun scheduledUpdate() {
        if (_publicData.value.sessions.isEmpty()) {
            refresh()
        }

        updateLive()
        updateUpcoming()
        updateFeed()
    }

    /**
     * TODO: introduce timezone.
     */
    private fun updateLive() {
        val sessions = _publicData.value.sessions
        if (sessions.isEmpty()) {
            _liveSessions.offer(emptySet())
            return
        }

        val timeInfo = GMTDate()
        val timezoneOffset = 2 * 60 * 60 * 1000L

        val now = GMTDate(
            timeInfo.seconds, timeInfo.minutes, timeInfo.hours,
            dayOfMonth = 5,
            month = Month.DECEMBER,
            year = 2019
        ) + timezoneOffset

        val result = sessions
            .filter { it.startsAt <= now && now <= it.endsAt }
            .map { it.id }
            .toSet()

        _liveSessions.offer(result)
    }

    private fun updateUpcoming() {
        val favorites = _favorites.value.toList()
        if (favorites.isEmpty()) {
            _upcomingFavorites.offer(emptySet())
            return
        }

        val today = GMTDate(
            0, 0, 0,
            dayOfMonth = 3,
            month = Month.DECEMBER,
            year = 2019
        ).dayOfYear

        val cards = favorites
            .map { sessionCard(it) }
            .filter { it.session.startsAt.dayOfYear == today }
            .map { it.session.id }
            .toSet()

        _upcomingFavorites.offer(cards)
    }

    private fun updateVote(sessionId: String, rating: RatingData?) {
        val votes = _votes.value

        if (rating == null) {
            votes.remove(sessionId)
        } else {
            votes[sessionId] = rating
        }

        _votes.offer(votes)
    }

    private fun updateFavorite(sessionId: String, isFavorite: Boolean) {
        val favorites = _favorites.value
        if (isFavorite) check(sessionId !in favorites)
        if (!isFavorite) check(sessionId in favorites)

        if (!isFavorite) {
            favorites.remove(sessionId)
        } else {
            favorites.add(sessionId)

            val session = session(sessionId)
            val startsAt = session.startsAt
            val now = GMTDate()

            val notificationTime = GMTDate(
                startsAt.seconds, startsAt.minutes, startsAt.hours,
                now.dayOfMonth, now.month, now.hours
            )

            launch {
                notificationManager.schedule(
                    session.title,
                    session.startsAt.toString(),
                    notificationTime
                )
            }

        }

        _favorites.offer(favorites)
    }

    private suspend fun updateFeed() {
        _feed.offer(Api.getFeed())
    }
}
