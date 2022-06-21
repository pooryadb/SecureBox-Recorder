package ir.romroid.secureboxrecorder.utils.state

import ir.romroid.secureboxrecorder.base.state.AppFragment

enum class AppFragmentEnum(
    private val isShowLoading: Boolean = true,
    private val isFullScreen: Boolean = false,
    private val resizeInputMode: Boolean = false
) : AppFragment {
    DEFAULT,

    HOME(),

    ;

    override fun isShowLoading(): Boolean = isShowLoading
    override fun isFullScreen(): Boolean = isFullScreen
    override fun resizeInputMode(): Boolean = resizeInputMode
}