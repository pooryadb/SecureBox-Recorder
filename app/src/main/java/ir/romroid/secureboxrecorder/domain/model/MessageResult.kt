package ir.romroid.secureboxrecorder.domain.model

import android.content.Context
import androidx.annotation.StringRes

sealed class MessageResult {
    data class Error(@StringRes val msgRes: Int? = null, val msg: String = "") : MessageResult()
    data class Loading(
        val show: Boolean,
        @StringRes val msgRes: Int? = null,
        val msg: String = ""
    ) : MessageResult()

    fun getMessage(context: Context): String = when (this) {
        is Error -> if (msgRes == null) msg else context.getString(msgRes)
        is Loading -> if (msgRes == null) msg else context.getString(msgRes)
    }
}