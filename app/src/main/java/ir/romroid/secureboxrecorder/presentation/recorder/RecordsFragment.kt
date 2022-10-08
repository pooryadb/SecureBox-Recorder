package ir.romroid.secureboxrecorder.presentation.recorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentRecordsBinding
import ir.romroid.secureboxrecorder.ext.getBackStackLiveData
import ir.romroid.secureboxrecorder.ext.toGone
import ir.romroid.secureboxrecorder.ext.toShow
import ir.romroid.secureboxrecorder.ext.toast
import ir.romroid.secureboxrecorder.presentation.safe.SafeViewModel
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_RECORD
import ir.romroid.secureboxrecorder.utils.BACK_FROM_RECORDER
import javax.inject.Inject

@AndroidEntryPoint
class RecordsFragment : BaseFragment<FragmentRecordsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecordsBinding
        get() = FragmentRecordsBinding::inflate

    @Inject
    lateinit var audioAdapter: AudioAdapter

    private val recorderVM by viewModels<RecorderViewModel>()
    private val safeVM by activityViewModels<SafeViewModel>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        if (safeVM.shouldSetUserKey()) {
            findNavController().navigate(
                RecordsFragmentDirections.actionRecordsFragmentToGetKeysFragment()
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
            else {
                recorderVM.liveRecords.value?.let {
                    showEmptyLay(it.isEmpty())
                }
            }
        }
    }

    override fun initObservers() {
        super.initObservers()

        recorderVM.liveRecords.observe(this) {
            showEmptyLay(it.isEmpty())
            audioAdapter.submitList(it)
        }

        recorderVM.liveDeleteRecord.observe(this) {
            if (it) {
                recorderVM.fetchRecords()
            } else
                requireContext().toast(getString(R.string.cant_find_file))
        }
    }

    override fun initBackStackObservers() {
        super.initBackStackObservers()

        findNavController().getBackStackLiveData<Boolean>(BACK_FROM_RECORDER)
            ?.observe(this) {
                if (it) {
                    view?.postDelayed({// FIXME: use better solution!
                        findNavController().navigate(
                            RecordsFragmentDirections.actionRecordsFragmentToBoxFragment()
                        )
                    }, 100L)
                } else {
                    recorderVM.fetchRecords()
                }
            }

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