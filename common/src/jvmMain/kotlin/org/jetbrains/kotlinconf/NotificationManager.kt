package org.jetbrains.kotlinconf

import io.ktor.util.date.*

actual class NotificationManager {
    actual suspend fun requestPermission(): Boolean {
        return false
    }

    actual suspend fun schedule(
        title: String,
        text: String,
        date: GMTDate
    ): String {
        error("")
    }

    actual fun cancel(id: String) {
    }

    actual suspend fun isEnabled(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}