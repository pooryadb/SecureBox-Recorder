package ir.romroid.secureboxrecorder.presentation.recorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentRecordsBinding
import ir.romroid.secureboxrecorder.domain.model.MessageResult
import ir.romroid.secureboxrecorder.ext.getBackStackLiveData
import ir.romroid.secureboxrecorder.ext.toGone
import ir.romroid.secureboxrecorder.ext.toShow
import ir.romroid.secureboxrecorder.ext.toast
import ir.romroid.secureboxrecorder.presentation.keys.KeyViewModel
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_RECORD
import javax.inject.Inject

@AndroidEntryPoint
class RecordsFragment : BaseFragment<FragmentRecordsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecordsBinding
        get() = FragmentRecordsBinding::inflate

    @Inject
    lateinit var audioAdapter: AudioAdapter

    private val recorderVM by activityViewModels<RecorderViewModel>()
    private val safeVM by viewModels<KeyViewModel>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        if (safeVM.shouldSetUserKey()) {
            findNavController().navigate(
                RecordsFragmentDirections.actionRecordsFragmentToKeyFragment()
            )
            return
        }

        binding?.apply {

            btnAdd.setOnClickListener {
                findNavController().navigate(
                    RecordsFragmentDirections.actionRecordsFragmentToDialogRecorder()
                )
            }

            rcAudio.apply {
                audioAdapter.apply {
                    onDeleteClick = {
                        findNavController().navigate(
                            RecordsFragmentDirections
                                .actionRecordsFragmentToDialogDeleteRecord(it.id)
                        )
                    }

                    onItemClick = {
                        findNavController().navigate(
                            RecordsFragmentDirections
                                .actionRecordsFragmentToDialogAudioPlayer(it)
                        )
                    }

                }

                adapter = audioAdapter

            }

            if (isRestoredFromBackStack.not())
                recorderVM.fetchRecords()
            else
                recorderVM.liveRecords.value?.let {
                    showEmptyLay(it.isEmpty())
                }
        }
    }

    override fun initObservers() {
        super.initObservers()

        recorderVM.liveRecords.observe(this) {
            showEmptyLay(it.isEmpty())
            audioAdapter.submitList(it)
        }

        recorderVM.liveMessage.observe(this) {
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

        findNavController().getBackStackLiveData<Long?>(BACK_FROM_DELETE_RECORD)
            ?.observe(this) { recordId ->
                if (recordId != null)
                    recorderVM.deleteRecord(recordId)
            }
    }

    private fun showEmptyLay(show: Boolean) = binding?.apply {
        if (show)
            layEmpty.root.toShow()
        else {
            layEmpty.root.toGone()
        }
    }

}