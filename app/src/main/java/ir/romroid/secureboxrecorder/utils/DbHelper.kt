package ir.romroid.secureboxrecorder.utils

import android.content.Context
import com.aaaamirabbas.reactor.handler.Reactor

object DbHelper {
    private fun getReactor(context: Context): Reactor {
        return Reactor(context, true)
    }

    //------------------------------

    private const val userKey_key = "userKey_key"
    private const val recovery_key = "recovery_key"
    fun getUserKey(context: Context): String =
        with(getReactor(context)) {
            val key = get(userKey_key, "")
            return@with key
        }

    fun getEncryptKey(context: Context): String =
        with(getReactor(context)) {
            val key = get(userKey_key, "-")
            return@with if (key.length < 32) {
                val step = (32 / key.length) + 1
                key.repeat(step)
                    .substring(0, 32)
            } else {
                key
            }
        }

    fun setUserKey(context: Context, data: String) =
        getReactor(context).put(userKey_key, data)

    fun setRecoveryKey(context: Context, data: String) =
        getReactor(context).put(recovery_key, data)

    fun getRecoveryKey(context: Context): String? =
        with(getReactor(context)) {
            return@with get(recovery_key)
        }

    private const val userKeyTime_key = "userKeyTime_key"
    fun getUserKeyTime(context: Context): Long? =
        with(getReactor(context)) {
            return@with get(userKeyTime_key)
        }


    fun setUserKeyTime(context: Context, data: Long) =
        getReactor(context).put(userKeyTime_key, data)
}