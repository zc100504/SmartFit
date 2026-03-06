package com.example.smartfit.util

import android.util.Log

object Logger {
    fun d(tag: String, msg: String) = Log.d(tag, msg)
    fun e(tag: String, msg: String) = Log.e(tag, msg)
}
