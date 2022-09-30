package ir.romroid.secureboxrecorder.presentation.fileManager.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogShareFileBinding
import ir.romroid.secureboxrecorder.ext.shareAnyFile
import java.io.File

@AndroidEntryPoint
class ShareFileDialog : BaseBottomSheetDialogFragment<DialogShareFileBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogShareFileBinding
        get() = DialogShareFileBinding::inflate

    private val args by navArgs<ShareFileDialogArgs>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            tvAddress.text = args.path

            btnShare.setOnClickListener {

                requireContext().shareAnyFile(File(args.path), requireActivity().localClassName)

                dismiss()
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

}