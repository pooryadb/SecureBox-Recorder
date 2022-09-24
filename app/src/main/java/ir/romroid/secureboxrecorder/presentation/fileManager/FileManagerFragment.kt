package ir.romroid.secureboxrecorder.presentation.fileManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentFileManagerBinding
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.toast
import javax.inject.Inject

@AndroidEntryPoint
class FileManagerFragment : BaseFragment<FragmentFileManagerBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFileManagerBinding
        get() = FragmentFileManagerBinding::inflate

    @Inject
    lateinit var audioAdapter: FileManagerAdapter

    private val fileManagerVM by viewModels<FileManagerViewModel>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {

        binding?.apply {

            btnExport.setOnClickListener {

            }

            btnExit.setOnClickListener {
                requireActivity().finish()
            }

            btnAdd.setOnClickListener {
                getContent.launch("*/*")
            }

            rcFileManager.apply {
                audioAdapter.apply {
                    onDeleteListener = {
                        loadingDialog(true)
                        fileManagerVM.deleteFile(it.id)
                    }

                    onShareListener = {

                    }

                    onClickListener = {

                    }
                }

                adapter = audioAdapter

            }

            if (isRestoredFromBackStack.not()) {
                loadingDialog(true)
                fileManagerVM.fetchFileList()
            }
        }
    }

    override fun initObservers() {
        super.initObservers()

        fileManagerVM.liveFileList.observe(this) {
            loadingDialog(false)
            audioAdapter.submitList(it)
        }

        fileManagerVM.liveAddFile.observe(this) {
            loadingDialog(false)
            if (it)
                fileManagerVM.fetchFileList()
            else
                requireContext().toast(getString(R.string.error_add_file))
        }

        fileManagerVM.liveDeleteFile.observe(this) {
            loadingDialog(false)
            if (it)
                fileManagerVM.fetchFileList()
            else
                requireContext().toast(getString(R.string.error_delete_file))
        }

    }

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {

            loadingDialog(true)
            fileManagerVM.addFile(it)

        }
        "showFileChooser uri: $it".logD(TAG)

    }

    private val TAG = "FileManagerFrag"

}