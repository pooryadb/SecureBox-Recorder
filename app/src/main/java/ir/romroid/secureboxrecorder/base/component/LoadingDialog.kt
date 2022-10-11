package ir.romroid.secureboxrecorder.base.component

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.databinding.DialogLoadingBinding
import ir.romroid.secureboxrecorder.ext.runAfter

class LoadingDialog(
    private val mContext: Context
) : BaseDialog<DialogLoadingBinding>(mContext, R.style.loading) {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogLoadingBinding
        get() = DialogLoadingBinding::inflate

    private var dismissTimer: Long = -1

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
    }

    private var mDimBehind = false
    private var mCancelable = false

    fun show(dimBehind: Boolean, cancelable: Boolean = false) {
        if (isShowing && dimBehind == mDimBehind && cancelable == mCancelable)
            return

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            if (dimBehind.not()) {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }

            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            setFlags(
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            )
        }

        setCancelable(cancelable)
        setCanceledOnTouchOutside(cancelable)

        dismissTimer = System.currentTimeMillis()
        mDimBehind = dimBehind
        mCancelable = cancelable

        super.show()
    }

    override fun show() {
        show(true, cancelable = false)
    }

    override fun dismiss() {
        val timeDifference = System.currentTimeMillis() - dismissTimer
        if (
            dismissTimer > 0 && timeDifference >= 500L
        ) {
            super.dismiss()
        } else {
            runAfter(timeDifference, {
                super.dismiss()
            })
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_OUTSIDE -> {
                true
            }
            else -> {
                super.onTouchEvent(event)
            }
        }

    }

}