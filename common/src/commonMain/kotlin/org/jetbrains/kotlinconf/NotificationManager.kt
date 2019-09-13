package org.jetbrains.kotlinconf

import io.ktor.util.date.*

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class NotificationManager() {
    suspend fun isEnabled(): Boolean

    suspend fun requestPermission(): Boolean

    suspend fun schedule(title: String, text: String, date: GMTDate): String

    fun cancel(id: String)
}