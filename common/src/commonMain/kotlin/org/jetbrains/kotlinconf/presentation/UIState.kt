package org.jetbrains.kotlinconf.presentation

import io.ktor.util.date.*
import kotlinx.coroutines.flow.*
import org.jetbrains.kotlinconf.*

/**
 * Session displayed in single schedule group.
 */
class SessionGroup(
    val month: String,
    val day: Int,
    val title: String,
    val startsAt: GMTDate,
    val sessions: List<SessionCard>,
    val daySection: Boolean = false,
    val lunchSection: Boolean = false
)

/**
 * All data to display session cards.
 */
class SessionCard(
    val session: SessionData,
    val date: String,
    val time: String,
    val location: RoomData,
    val roomVideo: String?,
    val speakers: List<SpeakerData>,
    val isFavorite: CFlow<Boolean>,
    val ratingData: CFlow<RatingData?>,
    val isLive: CFlow<Boolean>
)
