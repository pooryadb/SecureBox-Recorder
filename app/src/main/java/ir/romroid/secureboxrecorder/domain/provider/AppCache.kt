package ir.romroid.secureboxrecorder.domain.provider

import com.aaaamirabbas.reactor.handler.Reactor
import javax.inject.Inject

class AppCache @Inject constructor(
    private val reactorAES: Reactor,
    private val reactorBase64: Reactor,
) {
    private val _userKey = "_userKey"
    private val _recoveryKey = "_recoveryKey"
    private val _userKeyTime = "_userKeyTime"

    var userKey: String
        get() = reactorAES.get(_userKey, "")
        set(value) {
            reactorAES.put(_userKey, value)
        }

    fun getEncryptKey(): String? {
        val key = userKey
        return when {
            key.isEmpty() -> null
            key.length < 32 -> {
                val step = (32 / key.length) + 1
                key.repeat(step).substring(0, 32)
            }
            else -> key
        }
    }

    var recoveryKey: String
        get() = reactorAES.get(_recoveryKey, "")
        set(value) {
            reactorAES.put(_recoveryKey, value)
        }

    var userKeyTime: Long
        get() = reactorAES.get(_userKeyTime, -1L)
        set(value) {
            reactorAES.put(_userKeyTime, value)
        }
}