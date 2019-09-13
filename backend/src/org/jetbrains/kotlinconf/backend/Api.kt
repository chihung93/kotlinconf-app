package org.jetbrains.kotlinconf.backend

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.date.*
import io.ktor.util.pipeline.*
import org.jetbrains.kotlinconf.*
import java.time.*
import java.time.format.*
import java.util.*

private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

internal fun Routing.api(database: Database, production: Boolean, sessionizeUrl: String) {
    apiKeynote(production)
    apiUsers(database)
    apiAll(database)
    apiSession()
    apiVote(database, production)
    apiFavorite(database, production)
    apiSynchronize(sessionizeUrl)
    apiTwitter()
}

/*
GET http://localhost:8080/keynote?datetimeoverride=2017-10-24T10:00-07:00
 */
private fun Routing.apiKeynote(production: Boolean) {
    get("keynote") {
        val nowTime = simulatedTime(production)
        if (nowTime.isAfter(keynoteEndDateTime))
            call.respond(HttpStatusCode.OK)
        else {
            call.respond(comeBackLater)
        }
    }
}

/*
POST http://localhost:8080/user
1238476512873162837
 */
private fun Routing.apiUsers(database: Database) {
    route("users") {
        post {
            val userUUID = call.receive<String>()
            val ip = call.request.origin.remoteHost
            val timestamp = LocalDateTime.now(Clock.systemUTC())
            val created = database.createUser(userUUID, ip, timestamp)
            if (created)
                call.respond(HttpStatusCode.Created)
            else
                call.respond(HttpStatusCode.Conflict)
        }
        get("count") {
            call.respondText(database.usersCount().toString())
        }
    }
}

/*
GET http://localhost:8080/favorites
Accept: application/json
Authorization: Bearer 1238476512873162837
*/
private fun Routing.apiFavorite(database: Database, production: Boolean) {
    route("favorites") {
        get {
            val principal = call.validatePrincipal(database) ?: throw Unauthorized()
            val favorites = database.getFavorites(principal.token)
            call.respond(favorites)
        }
        if (!production) {
            get("all") {
                val favorites = database.getAllFavorites()
                call.respond(favorites)
            }
        }
        post {
            val principal = call.validatePrincipal(database) ?: throw Unauthorized()
            val sessionId = call.receive<String>()
            database.createFavorite(principal.token, sessionId)
            call.respond(HttpStatusCode.Created)
        }
        delete {
            val principal = call.validatePrincipal(database) ?: throw Unauthorized()
            val sessionId = call.receive<String>()
            database.deleteFavorite(principal.token, sessionId)
            call.respond(HttpStatusCode.OK)
        }
    }
}

/*
GET http://localhost:8080/votes
Accept: application/json
Authorization: Bearer 1238476512873162837
*/
private fun Routing.apiVote(database: Database, production: Boolean) {
    route("votes") {
        get {
            val principal = call.validatePrincipal(database) ?: throw Unauthorized()
            val votes = database.getVotes(principal.token)
            call.respond(votes)
        }
        get("all") {
            val votes = database.getAllVotes()
            call.respond(votes)
        }
        get("summary/{sessionId}") {
            val id = call.parameters["sessionId"] ?: throw BadRequest()
            val votesSummary = database.getVotesSummary(id)
            call.respond(votesSummary)
        }
        post {
            val principal = call.validatePrincipal(database) ?: throw Unauthorized()
            val vote = call.receive<VoteData>()
            val sessionId = vote.sessionId
            val rating = vote.rating!!.value

            val session = getSessionizeData().sessions.firstOrNull { it.id == sessionId } ?: throw NotFound()
            val nowTime = simulatedTime(production)

            fun GMTDate.toLocalTime(): LocalDateTime = LocalDateTime.ofInstant(toJvmDate().toInstant(), keynoteTimeZone)

            val startVotesAt = session.startsAt.toLocalTime()
            val endVotesAt = session.endsAt.toLocalTime().plusMinutes(15)!!
            val votingPeriodStarted = startVotesAt.let { ZonedDateTime.of(it, keynoteTimeZone).isBefore(nowTime) }
            val votingPeriodEnded = endVotesAt.let { ZonedDateTime.of(it, keynoteTimeZone).isBefore(nowTime) }

//            if (!votingPeriodStarted) {
//                return@post call.respond(comeBackLater)
//            }
//
//            if (votingPeriodEnded) {
//                return@post call.respond(tooLate)
//            }

            val timestamp = LocalDateTime.now(Clock.systemUTC())
            val status = if (database.changeVote(principal.token, sessionId, rating, timestamp)) {
                HttpStatusCode.Created
            } else {
                HttpStatusCode.OK
            }

            call.respond(status)
        }
        delete {
            val principal = call.validatePrincipal(database) ?: throw Unauthorized()
            val vote = call.receive<VoteData>()
            val sessionId = vote.sessionId
            database.deleteVote(principal.token, sessionId)
            call.respond(HttpStatusCode.OK)
        }
    }
}

/*
GET http://localhost:8080/all
Accept: application/json
Authorization: Bearer 1238476512873162837
*/
private fun Routing.apiAll(database: Database) {
    get("all") {
        val data = getSessionizeData()
        val principal = call.validatePrincipal(database)
        val responseData = if (principal != null) {
            val votes = database.getVotes(principal.token)
            val favorites = database.getFavorites(principal.token)
            ConferenceData(data, favorites, votes, LIVE_INFO)
        } else ConferenceData(data, liveVideos = LIVE_INFO)

        call.respond(responseData)
    }
}

private fun Routing.apiSession() {
    route("sessions") {
        get {
            val data = getSessionizeData()
            call.respond(data.sessions)
        }
        get("{sessionId}") {
            val data = getSessionizeData()
            val id = call.parameters["sessionId"] ?: throw BadRequest()
            val session = data.sessions.singleOrNull { it.id == id } ?: throw NotFound()
            call.respond(session)
        }
    }
}

/*
GET http://localhost:8080/sessionizeSync
*/
private fun Routing.apiSynchronize(sessionizeUrl: String) {
    get("sessionizeSync") {
        synchronizeWithSessionize(sessionizeUrl)
        call.respond(HttpStatusCode.OK)
    }
}

private fun Routing.apiTwitter() {
    get("feed") {
        call.respond(getFeedData())
    }
}

private suspend fun ApplicationCall.validatePrincipal(database: Database): KotlinConfPrincipal? {
    val principal = principal<KotlinConfPrincipal>() ?: return null
    if (!database.validateUser(principal.token)) return null
    return principal
}

private fun PipelineContext<Unit, ApplicationCall>.simulatedTime(production: Boolean): ZonedDateTime {
    val now = ZonedDateTime.now(keynoteTimeZone)
    return if (production)
        now
    else
        call.parameters["datetimeoverride"]?.let { ZonedDateTime.parse(it) } ?: now
}
