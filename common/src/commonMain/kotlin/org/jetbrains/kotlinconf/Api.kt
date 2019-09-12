package org.jetbrains.kotlinconf

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.http.*
import io.ktor.utils.io.core.use
import kotlin.native.concurrent.ThreadLocal

/**
 * Adapter to handle backend API and manage auth information.
 */
@ThreadLocal
internal object Api {
    //    val endpoint = "https://konf-staging.kotlin-aws.intellij.net/"
//    val endpoint = "http://172.30.160.213:8080"
//    val endpoint = "http://10.0.2.2:8080"
    val endpoint = "http://0.0.0.0:8080"

    private val client = HttpClient {
        install(JsonFeature)

        HttpResponseValidator {
            validateResponse {
                when (it.status) {
                    COMEBACK_LATER_STATUS -> throw TooEarlyVote()
                    TOO_LATE_STATUS -> throw TooLateVote()
                    HttpStatusCode.Unauthorized -> throw Unauthorized()
                }

                if (!it.status.isSuccess()) {
                    when (it.call.request.url.encodedPath) {
                        "/favorites" -> throw CannotFavorite()
                        else -> error("Bad status: ${it.status}")
                    }
                }
            }
        }
    }

    /**
     * @return status of request.
     */
    suspend fun sign(userId: String): Boolean {

        return client.request<HttpResponse> {
            apiUrl("users")
            method = HttpMethod.Post
            body = userId
        }.use {
            it.status.isSuccess()
        }
    }

    /**
     * Get [ConferenceData] info.
     * Load favorites and votes info if [userId] provided.
     */
    suspend fun getAll(userId: String?): ConferenceData = client.get {
        apiUrl("all", userId)
    }

    /**
     * Update favorite information.
     */
    suspend fun postFavorite(userId: String, sessionId: String): Unit = client.post {
        apiUrl("favorites", userId)
        body = sessionId
    }

    /**
     * Remove item from favorites list.
     */
    suspend fun deleteFavorite(userId: String, sessionId: String): Unit = client.delete {
        apiUrl("favorites", userId)
        body = sessionId
    }

    /**
     * Vote for session.
     */
    suspend fun postVote(userId: String, vote: VoteData): Unit = client.post {
        apiUrl("votes", userId)
        json()
        body = vote
    }

    /**
     * Remove vote.
     */
    suspend fun deleteVote(userId: String, sessionId: String): Unit = client.delete {
        apiUrl("votes", userId)
        body = sessionId
    }

    suspend fun loadPicture(url: String): ByteArray = client.get(url)

    private fun HttpRequestBuilder.json() {
        contentType(ContentType.Application.Json)
    }

    private fun HttpRequestBuilder.apiUrl(path: String, userId: String? = null) {
        if (userId != null) {
            header(HttpHeaders.Authorization, "Bearer $userId")
        }
        header(HttpHeaders.CacheControl, "no-cache")
        url {
            takeFrom(endpoint)
            encodedPath = path
        }
    }

}
