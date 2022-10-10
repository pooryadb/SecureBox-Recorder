package ir.romroid.secureboxrecorder.presentation.keys

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentKeyBinding
import ir.romroid.secureboxrecorder.ext.getFileNameFromCursor
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.toast
import ir.romroid.secureboxrecorder.utils.MyValidator

@AndroidEntryPoint
class KeyFragment : BaseFragment<FragmentKeyBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentKeyBinding
        get() = FragmentKeyBinding::inflate

    private val safeVM by viewModels<KeyViewModel>()

    private var uriTemp: Uri? = null

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            btnNext.setOnClickListener {
                if (trySaveKeys())
                    goNextPage()
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
                is KeyViewModel.UnzipResult.Progress -> {
                    loadingDialog(true)
                }
                is KeyViewModel.UnzipResult.Success -> {
                    loadingDialog(false)
                    requireContext().toast(getString(R.string.restore_backup_success))
                    uriTemp = null
                    binding?.btnNext?.performClick()
                }
                is KeyViewModel.UnzipResult.Error -> {
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

    private fun goNextPage() {
        uriTemp?.let {
            safeVM.unzipFile(it)
        } ?: run {
            findNavController().navigate(
                KeyFragmentDirections.actionKeyFragmentToRecordsFragment()
            )
        }
    }

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            uriTemp = it

            binding?.tvRestoreFileName?.text =
                "%s:\n%s".format(
                    getString(R.string.file_name),
                    requireContext().getFileNameFromCursor(it)
                )
        }
        "showFileChooser uri: $it".logD(TAG)

    }

    private companion object {
        const val TAG = "KeyFragment"
    }

}