package ir.romroid.secureboxrecorder.ext


fun <T> Collection<T>.logListE(tag: String = "") {
    this.forEachIndexed { index, t ->
        t?.logE(tag + index)
    }
}

fun <T> Array<T>.logArrayE(tag: String = "") {
    this.forEachIndexed { index, t ->
        t?.logE(tag + index)
    }
}

fun <K, V> Map<*, *>.logMapE(tag: String = "") {
    this.forEach {
        this.logE("$tag${it.key}: ${it.value}")
    }
}