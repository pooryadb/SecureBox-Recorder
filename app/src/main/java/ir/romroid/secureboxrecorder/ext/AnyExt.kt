package ir.romroid.secureboxrecorder.ext

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import ir.romroid.secureboxrecorder.BuildConfig

@SuppressLint("LogNotTimber")
fun Any.logI(tag: String = "", throwable: Throwable? = null) {
    if (BuildConfig.DEBUG)
        Log.i(tag, "$this\n", throwable)
}

@SuppressLint("LogNotTimber")
fun Any.logE(tag: String = "", throwable: Throwable? = null) {
    if (BuildConfig.DEBUG)
        Log.e(tag, "$this\n", throwable)
}

@SuppressLint("LogNotTimber")
fun Any.logD(tag: String = "", throwable: Throwable? = null) {
    if (BuildConfig.DEBUG)
        Log.d(tag, "$this\n", throwable)
}

inline fun <reified NEW> Any.cast(): NEW? {
    return if (this.isCastable<NEW>())
        this as NEW
    else null
}

inline fun <reified NEW> Any.isCastable(): Boolean {
    return this is NEW
}

fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val p = layoutParams as ViewGroup.MarginLayoutParams
        p.setMargins(left, top, right, bottom)
        requestLayout()
    }
}