package org.jetbrains.kotlinconf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.os.Build.VERSION_CODES.N
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

fun Context.getResourceId(@AttrRes attribute: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attribute, typedValue, true)
    return typedValue.resourceId
}

@ColorInt
fun View.color(@ColorRes attribute: Int): Int = ContextCompat.getColor(context, attribute)

inline fun <reified T : Activity> View.showActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(context, T::class.java).apply(block)
    context.startActivity(intent)
}

fun Context.getHtmlText(resId: Int): Spanned {
    return if (android.os.Build.VERSION.SDK_INT >= N) {
        Html.fromHtml(getText(resId).toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(getText(resId).toString())
    }
}

val Context.connectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

val Context.isConnected: Boolean?
    get() = connectivityManager?.activeNetworkInfo?.isConnected

val Context.isAirplaneModeOn: Boolean
    @RequiresApi(JELLY_BEAN_MR1)
    get() = try {
        Settings.System.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    } catch (error: Throwable) {
        false
    }

val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

val Int.px: Int get() = (this / Resources.getSystem().displayMetrics.density).roundToInt()

