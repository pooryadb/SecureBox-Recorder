package ir.romroid.secureboxrecorder.base.component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StyleRes
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<VB : ViewBinding>(context: Context, @StyleRes theme: Int? = null) :
    Dialog(context, theme ?: 0) {

    var binding: VB? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    abstract fun viewHandler(view: View, savedInstanceState: Bundle?)//use binding

    protected open fun initDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDialog()

        binding = bindingInflater.invoke(LayoutInflater.from(context), null, false)
        ViewCompat.setLayoutDirection(requireNotNull(binding).root, ViewCompat.LAYOUT_DIRECTION_RTL)
        setContentView(requireNotNull(binding).root)

        viewHandler(binding!!.root, savedInstanceState)
    }
}