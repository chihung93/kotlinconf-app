package org.jetbrains.kotlinconf.backend

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.response.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import org.jetbrains.kotlinconf.*
import java.util.concurrent.*

@Volatile
private var feedData: FeedData? = null

@Serializable
internal class TwitterToken(
    val token_type: String,
    val access_token: String
)

fun Application.launchTwitterJob(interval: Long = 1) {
    log.info("Synchronizing with twitter each $interval minutes")
    GlobalScope.launch {
        var currentToken: String? = null
        var fail = false
        while (true) {
            try {
                if (currentToken == null || fail) {
                    currentToken = fetchTwitterAuthToken()
                    fail = false
                }
                log.trace("Synchronizing to Twitter…")
                fetchTwitterData(currentToken)
                log.trace("Finished loading data from Twitter.")
                delay(TimeUnit.MINUTES.toMillis(interval))
            } catch (cause: Throwable) {
                val response = (cause as ClientRequestException).response
                val result = response.readText()
                fail = true
            }
        }
    }
}

suspend fun fetchTwitterAuthToken(): String = client.submitForm<TwitterToken>(
    "https://api.twitter.com/oauth2/token",
    Parameters.build {
        append("grant_type", "client_credentials")
    }
) {
    header(
        HttpHeaders.Authorization,
        "Basic WDM5clRIMlVrVXpRcW5hcWtRbEdRZ3NrTTpJOFhJWEpjdlNKeDBiZDB2T05MNWd3N3dZN3dWcFRyU05FeFowQkRzZWhaMDg2UkxQQg=="
    )
}.access_token

suspend fun fetchTwitterData(authToken: String, query: String = "#kotlinconf") {
    val url = "https://api.twitter.com/1.1/search/tweets.json"
    feedData = client.get<FeedData>(url) {
        url {
            parameter("q", query)
            parameter("result_type", "recent")
        }
        header(HttpHeaders.Authorization, "Bearer $authToken")
    }
}

fun getFeedData(): FeedData = feedData ?: throw ServiceUnavailable()
