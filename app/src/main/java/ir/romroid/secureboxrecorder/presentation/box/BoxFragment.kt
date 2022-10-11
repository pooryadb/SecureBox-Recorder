package ir.romroid.secureboxrecorder.presentation.box

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
import ir.romroid.secureboxrecorder.databinding.FragmentBoxBinding
import ir.romroid.secureboxrecorder.domain.model.MessageResult
import ir.romroid.secureboxrecorder.ext.*
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_FILE
import javax.inject.Inject

@AndroidEntryPoint
class BoxFragment : BaseFragment<FragmentBoxBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBoxBinding
        get() = FragmentBoxBinding::inflate

    @Inject
    lateinit var boxAdapter: BoxAdapter

    private val boxVM by viewModels<BoxViewModel>()

    private companion object {
        const val TAG = "BoxFrag"
    }

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {

        binding?.apply {

            btnExport.setOnClickListener {
                boxVM.exportData()
            }

            btnExit.setOnClickListener {
                requireActivity().finish()
                boxVM.clearTemp()
            }

            btnAdd.setOnClickListener {
                getContent.launch("*/*")
            }

            rcFileManager.apply {
                boxAdapter.apply {
                    onDeleteListener = {
                        findNavController().navigate(
                            BoxFragmentDirections.actionBoxFragmentToDeleteFileDialog(it.id)
                        )
                    }

                    onShareListener = {
                        boxVM.shareFile(it.uri)
                    }

                    onClickListener = {
                        findNavController().navigate(
                            BoxFragmentDirections.actionBoxFragmentToWebViewFragment(it)
                        )
                    }
                }

                val dividerItemDecoration = DividerItemDecoration(
                    requireContext(),
                    RecyclerView.VERTICAL
                ).apply {
                    setDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.shape_item_divider)!!
                    )
                }

                addItemDecoration(dividerItemDecoration)

                adapter = boxAdapter

            }

            if (isRestoredFromBackStack.not()) {
                boxVM.fetchFileList()
            }
        }
    }

    override fun initObservers() {
        super.initObservers()

        boxVM.liveFileList.observe(this) {
            showEmptyLay(it.isEmpty())
            boxAdapter.submitList(it)
        }

        boxVM.liveShareFile.observe(this) {
            requireContext().shareAnyFile(it, requireActivity().localClassName)
        }

        boxVM.liveExportPath.observe(this) {
            findNavController().navigate(
                BoxFragmentDirections.actionBoxFragmentToShareFileDialog(it)
            )
        }

        boxVM.liveMessage.observe(this) {
            when (it) {
                is MessageResult.Error -> {
                    loadingDialog(false)
                    requireContext().toast(it.getMessage(requireContext()))
                }
                is MessageResult.Loading -> loadingDialog(it.show)
            }
        }

    }

    override fun initBackStackObservers() {
        super.initBackStackObservers()

        findNavController().getBackStackLiveData<Long?>(BACK_FROM_DELETE_FILE)
            ?.observe(this) { fileId ->
                if (fileId != null) {
                    boxVM.deleteFile(fileId)
                }
            }
    }

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            boxVM.addFile(it)

        }
        "showFileChooser uri: $it".logD(TAG)

    }

    override fun onDestroy() {
        "onDestroy".logD(TAG)
        // FIXME: onDestroy isn't best approach
        boxVM.clearTemp()
        super.onDestroy()
    }

    private fun showEmptyLay(show: Boolean) = binding?.apply {
        if (show)
            layEmpty.root.toShow()
        else {
            layEmpty.root.toGone()
        }
    }

}