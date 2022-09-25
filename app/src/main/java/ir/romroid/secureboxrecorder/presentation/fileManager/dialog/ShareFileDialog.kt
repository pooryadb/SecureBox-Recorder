package ir.romroid.secureboxrecorder.presentation.fileManager.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.BuildConfig
import ir.romroid.secureboxrecorder.base.component.BaseBottomSheetDialogFragment
import ir.romroid.secureboxrecorder.databinding.DialogShareFileBinding
import java.io.File
import java.net.URLConnection

@AndroidEntryPoint
class ShareFileDialog : BaseBottomSheetDialogFragment<DialogShareFileBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogShareFileBinding
        get() = DialogShareFileBinding::inflate

    private val args by navArgs<ShareFileDialogArgs>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            tvAddress.text = args.address

            btnShare.setOnClickListener {

                val uriProvider = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + "." + requireActivity().localClassName + ".provider",
                    File(args.address)
                )

                ShareCompat.IntentBuilder(requireContext())
                    .setStream(uriProvider)
                    .setType(URLConnection.guessContentTypeFromName(args.address))
                    .startChooser()

                dismiss()
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

}