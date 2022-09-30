package ir.romroid.secureboxrecorder.base.component

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import ir.romroid.secureboxrecorder.base.state.AppFragment
import ir.romroid.secureboxrecorder.utils.state.AppFragmentEnum

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    var binding: VB? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    open fun fragmentEnum(): AppFragment = AppFragmentEnum.DEFAULT

    lateinit var activityContext: BaseActivity<*>

    var isRestoredFromBackStack = false

    private val dialogLoading: LoadingDialog by lazy {
        LoadingDialog(requireContext())
    }

    protected abstract fun viewHandler(view: View, savedInstanceState: Bundle?)
    protected open fun initObservers() {}
    protected open fun initBackStackObservers() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRestoredFromBackStack = false
        initBackStackObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        isRestoredFromBackStack = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = bindingInflater.invoke(inflater, container, false)

        return requireNotNull(binding).root
    }

    override fun onResume() {
        super.onResume()

        handleKeyboardSize()
    }

    private fun handleKeyboardSize() {
        activityContext.windowInsetsHelper.isFullScreen = fragmentEnum().isFullScreen()
        activityContext.windowInsetsHelper.isAutoResizeKeyboard = fragmentEnum().resizeInputMode()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewHandler(view, savedInstanceState)
        initObservers()
    }

    fun loadingDialog(show: Boolean = true) {
        if (show) dialogLoading.show() else dialogLoading.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context as BaseActivity<*>
    }
}