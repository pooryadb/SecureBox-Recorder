package ir.romroid.secureboxrecorder.presentation.safe

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
import ir.romroid.secureboxrecorder.databinding.FragmentKeyBinding
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.toast
import ir.romroid.secureboxrecorder.utils.MyValidator

@AndroidEntryPoint
class KeyFragment : BaseFragment<FragmentKeyBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentKeyBinding
        get() = FragmentKeyBinding::inflate

    private val safeVM by activityViewModels<SafeViewModel>()

    private val TAG = "GetKeys"

    private var uriTemp: Uri? = null

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            btnNext.setOnClickListener {
                if (trySaveKeys()) {
                    if (uriTemp != null)
                        safeVM.unzipFile(uriTemp!!)
                    else
                        findNavController().navigate(
                            KeyFragmentDirections.actionGetKeysFragmentToRecordListFragment()
                        )
                }
            }

            btnRestore.setOnClickListener {
                getContent.launch("application/zip")
            }
        }
    }

    override fun initObservers() {
        super.initObservers()

        safeVM.liveUnzip.observe(this) {
            when (it) {
                is SafeViewModel.UnzipResult.Progress -> {
                    loadingDialog(true)
                }
                is SafeViewModel.UnzipResult.Success -> {
                    loadingDialog(false)
                    requireContext().toast(getString(R.string.restore_backup_success))
                    uriTemp = null
                    binding?.btnNext?.performClick()
                }
                is SafeViewModel.UnzipResult.Error -> {
                    loadingDialog(false)
                    requireContext().toast(getString(R.string.restore_backup_error))
                }
            }
        }
    }

    private fun trySaveKeys(): Boolean = binding?.run {
        val securityKey = etSecurity.editText?.text.toString()

        val securityError = MyValidator.isPasswordValid(securityKey)?.let { getString(it) }

        etSecurity.error = securityError
        if (securityError != null)
            return@run false

        safeVM.saveUserKey(securityKey)

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
                    safeVM.getFileName(it)
                )
        }
        "showFileChooser uri: $it".logD(TAG)

    }

}