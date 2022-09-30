package ir.romroid.secureboxrecorder.presentation.fileManager.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogOpenFileBinding
import ir.romroid.secureboxrecorder.ext.openAnyFile
import ir.romroid.secureboxrecorder.ext.setBackStackLiveData
import ir.romroid.secureboxrecorder.utils.BACK_FROM_OPEN_FILE

@AndroidEntryPoint
class OpenFileDialog : BaseBottomSheetDialogFragment<DialogOpenFileBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogOpenFileBinding
        get() = DialogOpenFileBinding::inflate

    private val args by navArgs<OpenFileDialogArgs>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {

            btnOpen.setOnClickListener {

                requireContext().openAnyFile(
                    args.fileModel.uri.toFile(),
                    getString(R.string.error_share_file),
                    requireActivity().localClassName
                )

                cancel()
            }

            btnCancel.setOnClickListener {
                cancel()
            }
        }
    }

    fun cancel() {
        findNavController().setBackStackLiveData(BACK_FROM_OPEN_FILE, null)
    }

}