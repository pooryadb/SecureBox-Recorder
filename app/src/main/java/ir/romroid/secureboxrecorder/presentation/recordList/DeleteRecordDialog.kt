package ir.romroid.secureboxrecorder.presentation.recordList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogDeleteRecordBinding
import ir.romroid.secureboxrecorder.ext.setBackStackLiveData
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_RECORD

@AndroidEntryPoint
class DeleteRecordDialog : BaseBottomSheetDialogFragment<DialogDeleteRecordBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogDeleteRecordBinding
        get() = DialogDeleteRecordBinding::inflate

    private val args by navArgs<DeleteRecordDialogArgs>()

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
        findNavController().setBackStackLiveData(BACK_FROM_DELETE_RECORD, id)
    }

}