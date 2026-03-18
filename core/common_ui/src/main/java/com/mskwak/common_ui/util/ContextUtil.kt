package com.mskwak.common_ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.core.net.toUri

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun Context.openPlayStore() {
    val packageName = packageName
    val intent = runCatching {
        Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
    }.getOrElse {
        Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri())
    }
    startActivity(intent)
}
