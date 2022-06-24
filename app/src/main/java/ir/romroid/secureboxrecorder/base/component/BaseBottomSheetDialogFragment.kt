package ir.romroid.secureboxrecorder.base.component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.romroid.secureboxrecorder.R

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding>() : BottomSheetDialogFragment() {

    lateinit var activityContext: BaseActivity<*>

    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    var binding: VB? = null

    private val onDismiss: (() -> Unit)? = null

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

    abstract fun viewHandler(view: View, savedInstanceState: Bundle?)
    protected open fun initObservers() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.app_theme_sheet).apply {

            setCancelable(hasCancelable)
            setCanceledOnTouchOutside(hasCancelable)
            setOnDismissListener {
                dismissListener()
            }
            setOnShowListener {
                showListener()
            }
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

        ViewCompat.setLayoutDirection(requireNotNull(binding).root, ViewCompat.LAYOUT_DIRECTION_RTL)
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
        onDismiss?.invoke()
    }

}