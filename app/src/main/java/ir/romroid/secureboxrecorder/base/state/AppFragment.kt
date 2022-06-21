package ir.romroid.secureboxrecorder.base.state

interface AppFragment {
    fun isShowLoading(): Boolean
    fun isFullScreen(): Boolean
    fun resizeInputMode(): Boolean
}