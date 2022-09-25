package ir.romroid.secureboxrecorder.presentation.fileManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.BuildConfig
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentFileManagerBinding
import ir.romroid.secureboxrecorder.domain.model.FileType
import ir.romroid.secureboxrecorder.ext.getBackStackLiveData
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.toast
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_FILE
import java.net.URLConnection
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
                fileManagerVM.exportData()
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
                        findNavController().navigate(
                            FileManagerFragmentDirections
                                .actionFileManagerFragmentToDeleteFileDialog(it.id)
                        )
                    }

                    onShareListener = {
                        loadingDialog(true)
                        fileManagerVM.shareFile(it.uri)
                    }

                    onClickListener = {
                        if (it.type == FileType.Other)
                            requireContext().toast(requireContext().getString(R.string.open_other_files_msg))
                        else
                            fileManagerVM.tempFile(it.uri)
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

        fileManagerVM.liveShareFile.observe(this) {
            loadingDialog(false)
            if (it != null) {
                val uriProvider = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + "." + requireActivity().localClassName + ".provider",
                    it
                )


                ShareCompat.IntentBuilder(requireContext())
                    .setStream(uriProvider)
                    .setType(URLConnection.guessContentTypeFromName(it.name))
                    .startChooser()
            } else
                requireContext().toast(getString(R.string.error_share_file))
        }

        fileManagerVM.liveExport.observe(this) {
            when (it) {
                is FileManagerViewModel.ExportResult.Progress -> loadingDialog(true)
                is FileManagerViewModel.ExportResult.Error -> {
                    loadingDialog(false)
                    requireContext().toast(it.message)
                }
                is FileManagerViewModel.ExportResult.Success -> {
                    findNavController().navigate(
                        FileManagerFragmentDirections.actionFileManagerFragmentToShareFileDialog(
                            it.filePath
                        )
                    )
                    loadingDialog(false)

                }
            }
        }

        fileManagerVM.liveTempFile.observe(this) {
            if (it != null) {
                findNavController().navigate(
                    FileManagerFragmentDirections.actionFileManagerFragmentToWebViewFragment(
                        it
                    )
                )
            } else
                requireContext().toast(getString(R.string.error_open_file))
        }

    }

    override fun initBackStackObservers() {
        super.initBackStackObservers()

        findNavController().getBackStackLiveData<Long?>(BACK_FROM_DELETE_FILE)
            ?.observe(this) { fileId ->
                if (fileId != null) {
                    loadingDialog(true)
                    fileManagerVM.deleteFile(fileId)
                }
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

    override fun onDestroy() {
        "onDestroy".logD(TAG)
        // FIXME: onDestroy not work properly
        fileManagerVM.clearTemp()
        super.onDestroy()
    }

    private val TAG = "FileManagerFrag"
}