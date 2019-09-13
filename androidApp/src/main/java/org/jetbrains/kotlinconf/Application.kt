package org.jetbrains.kotlinconf

import android.app.*
import android.widget.*

class KotlinConfApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ConferenceService.errors.watch {
            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
        }
    }
}
