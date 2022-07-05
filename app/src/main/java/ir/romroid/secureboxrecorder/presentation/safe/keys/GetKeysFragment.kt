package ir.romroid.secureboxrecorder.presentation.safe.keys

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentGetKeysBinding
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.presentation.safe.SafeViewModel
import ir.romroid.secureboxrecorder.utils.MyValidator

@AndroidEntryPoint
class GetKeysFragment : BaseFragment<FragmentGetKeysBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGetKeysBinding
        get() = FragmentGetKeysBinding::inflate

    private val safeVM by activityViewModels<SafeViewModel>()

    private val TAG = "GetKeys"

    private var uriTemp: Uri? = null

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            btnNext.setOnClickListener {

                if (trySaveKeys()) {
                    if (uriTemp != null) {
                        // TODO: unpack restore file}
                    }

                    findNavController().navigate(
                        GetKeysFragmentDirections.actionGetKeysFragmentToRecordListFragment()
                    )
                }
            }

            btnRestore.setOnClickListener {
                getContent.launch("application/zip")
            }
        }
    }

    private fun trySaveKeys(): Boolean = binding?.run {
        val securityKey = etSecurity.editText?.text.toString()
        val recoveryKey = etRecovery.editText?.text.toString()

        val securityError = MyValidator.isPasswordValid(securityKey)?.let { getString(it) }
        val recoveryError = MyValidator.isPasswordValid(recoveryKey)?.let { getString(it) }

        etSecurity.error = securityError
        etRecovery.error = recoveryError
        if (securityError != null || recoveryError != null)
            return@run false

        safeVM.saveUserKey(securityKey)
        safeVM.saveRecoveryKey(recoveryKey)

        return@run true
    } ?: false

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            uriTemp = it

            binding?.tvRestoreFileName?.text =
                "%s:\n%s".format(
                    getString(R.string.file_name),
                    safeVM.getFileNameFromCursor(requireContext(), it)
                )
        }
        "showFileChooser uri: $it".logD(TAG)

    }

}