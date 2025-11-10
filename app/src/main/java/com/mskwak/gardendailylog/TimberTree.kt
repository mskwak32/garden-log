package com.mskwak.gardendailylog

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber


internal class TimberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        return String.format(
            "Class:%s: Line: %s, Method: %s",
            super.createStackElementTag(element),
            element.lineNumber,
            element.methodName
        )
    }
}

internal class TimberReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.ERROR) {
            Firebase.crashlytics.run {
                log(message)
                recordException(t ?: Exception(message))
                setCustomKey("log_priority", priority)
            }

        }
    }
}