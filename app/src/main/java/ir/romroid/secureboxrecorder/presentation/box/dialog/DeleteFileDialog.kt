package ir.romroid.secureboxrecorder.presentation.box.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogDeleteFileBinding
import ir.romroid.secureboxrecorder.ext.setBackStackLiveData
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_FILE

@AndroidEntryPoint
class DeleteFileDialog : BaseBottomSheetDialogFragment<DialogDeleteFileBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogDeleteFileBinding
        get() = DialogDeleteFileBinding::inflate

    private val args by navArgs<DeleteFileDialogArgs>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            btnDelete.setOnClickListener {
                dismiss(args.id)
            }

            btnCancel.setOnClickListener {
                dismiss(null)
            }
        }
    }

    private fun dismiss(id: Long?) {
        findNavController().setBackStackLiveData(BACK_FROM_DELETE_FILE, id)
    }

}