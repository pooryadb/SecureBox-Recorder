package ir.romroid.secureboxrecorder.base.component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.romroid.secureboxrecorder.R

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding>() : BottomSheetDialogFragment() {

    protected lateinit var activityContext: BaseActivity<*>

    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    protected var binding: VB? = null
        private set

    protected var hasCancelable = true
        set(value) {
            dialog?.apply {
                setCancelable(hasCancelable)
                setCanceledOnTouchOutside(hasCancelable)
            }
            field = value
        }

    open fun showListener() {}
    open fun dismissListener() {}

    protected abstract fun viewHandler(view: View, savedInstanceState: Bundle?)
    protected open fun initObservers() {}

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), R.style.app_theme_sheet).apply {

            setCancelable(hasCancelable)
            setCanceledOnTouchOutside(hasCancelable)
            setOnDismissListener {
                dismissListener()
            }
            setOnShowListener {
                showListener()
            }
        }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        hasCancelable = cancelable
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.app_theme_sheet)
        binding = bindingInflater.invoke(
            inflater.cloneInContext(contextThemeWrapper), container, false
        )

        return requireNotNull(binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        viewHandler(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context as BaseActivity<*>
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissListener()
    }

}