package ir.romroid.secureboxrecorder.presentation.fileManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentFileManagerBinding
import ir.romroid.secureboxrecorder.ext.*
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_FILE
import javax.inject.Inject


@AndroidEntryPoint
class FileManagerFragment : BaseFragment<FragmentFileManagerBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFileManagerBinding
        get() = FragmentFileManagerBinding::inflate

    @Inject
    lateinit var fileManagerAdapter: FileManagerAdapter

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
                fileManagerAdapter.apply {
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
                        findNavController().navigate(
                            FileManagerFragmentDirections.actionFileManagerFragmentToWebViewFragment(
                                it
                            )
                        )
                    }
                }

                val dividerItemDecoration = DividerItemDecoration(
                    requireContext(),
                    RecyclerView.VERTICAL
                )

                dividerItemDecoration.setDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.shape_item_divider
                    )!!
                )

                addItemDecoration(dividerItemDecoration)

                adapter = fileManagerAdapter

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

            if (it.isEmpty())
                binding?.layEmpty?.root?.toShow()
            else {
                binding?.layEmpty?.root?.toGone()
                fileManagerAdapter.submitList(it)
            }

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
                requireContext().shareAnyFile(it, requireActivity().localClassName)
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
                    loadingDialog(false)
                    findNavController().navigate(
                        FileManagerFragmentDirections.actionFileManagerFragmentToShareFileDialog(
                            it.filePath
                        )
                    )

                }
            }
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