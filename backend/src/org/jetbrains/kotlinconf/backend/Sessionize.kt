package org.jetbrains.kotlinconf.backend

import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.jetbrains.kotlinconf.*
import java.time.*
import java.util.concurrent.*

@Volatile
private var sessionizeData: SessionizeData? = null

val comeBackLater = HttpStatusCode(477, "Come Back Later")
val tooLate = HttpStatusCode(478, "Too Late")
val keynoteTimeZone = ZoneId.of("Europe/Paris")!!
val keynoteEndDateTime = ZonedDateTime.of(
    2018, 10, 4, 10, 0, 0, 0, keynoteTimeZone
)!!

const val fakeSessionId = "007"

fun Application.launchSyncJob(sessionizeUrl: String, sessionizeInterval: Long) {
    log.info("Synchronizing each $sessionizeInterval minutes with $sessionizeUrl")
    GlobalScope.launch {
        while (true) {
            log.trace("Synchronizing to Sessionize…")
            synchronizeWithSessionize(sessionizeUrl)
            log.trace("Finished loading data from Sessionize.")
            delay(TimeUnit.MINUTES.toMillis(sessionizeInterval))
        }
    }
}

suspend fun synchronizeWithSessionize(sessionizeUrl: String) {
    sessionizeData = client.get<SessionizeData>(sessionizeUrl)
}

fun getSessionizeData(): SessionizeData = sessionizeData ?: throw ServiceUnavailable()