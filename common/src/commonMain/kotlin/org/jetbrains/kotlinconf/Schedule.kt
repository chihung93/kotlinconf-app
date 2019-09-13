package org.jetbrains.kotlinconf

import org.jetbrains.kotlinconf.presentation.*

internal fun List<SessionData>.groupByDay(): List<SessionGroup> = groupBy { it.startsAt }
    .map { (startsAt, sessions) ->
        val monthName = startsAt.month.value
        val day = startsAt.dayOfMonth
        val endsAt = sessions.first().endsAt
        val time = "${startsAt.time()}-${endsAt.time()}"

        val cards = sessions.map { ConferenceService.sessionCard(it.id) }
        SessionGroup(monthName, day, time, startsAt, cards)
    }.sortedBy { it.startsAt.timestamp }

internal fun List<SessionGroup>.addDayStart(): List<SessionGroup> {
    if (isEmpty()) {
        return this
    }

    val result = mutableListOf<SessionGroup>()
    var lastDay: Int? = null
    for (group in this) {
        if (group.day != lastDay) {
            result += group.makeDayHeader()
            lastDay = group.day
        }

        result += group
    }

    return result
}

internal fun List<SessionGroup>.addLunches(): List<SessionGroup> {
    val lunchString = "Lunch Lunch Lunch Lunch Lunch Lunch Lunch Lunch".toUpperCase()

    val result = mutableListOf<SessionGroup>()
    var skip = false
    for (group in this) {
        if (group.daySection || skip) {
            skip = !skip
            result.add(group)
            continue
        }

        result += group.makeLunchHeader(lunchString)
        result.add(group)
    }

    return result
}

internal fun SessionGroup.makeDayHeader(): SessionGroup {
    val title = "${month.toUpperCase()} $day ".repeat(100)
    return SessionGroup(
        month, day, title, startsAt, emptyList(), daySection = true
    )
}

internal fun SessionGroup.makeLunchHeader(title: String): SessionGroup = SessionGroup(
    month, day, title, startsAt, emptyList(), lunchSection = true
)
