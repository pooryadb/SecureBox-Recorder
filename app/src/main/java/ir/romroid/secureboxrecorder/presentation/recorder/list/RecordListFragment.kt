package ir.romroid.secureboxrecorder.presentation.recorder.list

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
import ir.romroid.secureboxrecorder.databinding.FragmentRecordListBinding
import ir.romroid.secureboxrecorder.ext.getBackStackLiveData
import ir.romroid.secureboxrecorder.ext.toGone
import ir.romroid.secureboxrecorder.ext.toShow
import ir.romroid.secureboxrecorder.ext.toast
import ir.romroid.secureboxrecorder.presentation.recorder.RecorderListViewModel
import ir.romroid.secureboxrecorder.presentation.safe.SafeViewModel
import ir.romroid.secureboxrecorder.utils.BACK_FROM_DELETE_RECORD
import ir.romroid.secureboxrecorder.utils.BACK_FROM_RECORDER
import javax.inject.Inject

@AndroidEntryPoint
class RecordListFragment : BaseFragment<FragmentRecordListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecordListBinding
        get() = FragmentRecordListBinding::inflate

    @Inject
    lateinit var audioAdapter: AudioAdapter

    private val recorderVM by viewModels<RecorderListViewModel>()
    private val safeVM by activityViewModels<SafeViewModel>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        if (safeVM.shouldSetUserKey()) {
            findNavController().navigate(
                RecordListFragmentDirections.actionRecordListFragmentToGetKeysFragment()
            )
            return
        }

        binding?.apply {

            btnAdd.setOnClickListener {
                findNavController().navigate(
                    RecordListFragmentDirections.actionRecordListFragmentToDialogRecordList()
                )
            }

            rcAudio.apply {
                audioAdapter.apply {
                    onDeleteClick = {
                        findNavController().navigate(
                            RecordListFragmentDirections
                                .actionRecordListFragmentToDialogDeleteRecord(it.id)
                        )
                    }

                    onItemClick = {
                        findNavController().navigate(
                            RecordListFragmentDirections
                                .actionRecordListFragmentToDialogAudioPlayer(it)
                        )
                    }

                }

                adapter = audioAdapter

            }

            if (isRestoredFromBackStack.not())
                recorderVM.fetchRecordedList(requireContext())
            else {
                recorderVM.liveRecordedList.value?.let {
                    showEmptyLay(it.isEmpty())
                }
            }
        }
    }

    override fun initObservers() {
        super.initObservers()

        recorderVM.liveRecordedList.observe(this) {
            showEmptyLay(it.isEmpty())
            audioAdapter.submitList(it)
        }
    }

    override fun initBackStackObservers() {
        super.initBackStackObservers()

        findNavController().getBackStackLiveData<Boolean>(BACK_FROM_RECORDER)
            ?.observe(this) {
                if (it) {
                    view?.postDelayed({// FIXME: use better solution!
                        findNavController().navigate(
                            RecordListFragmentDirections.actionRecordListFragmentToBoxFragment()
                        )
                    }, 100L)
                } else {
                    recorderVM.fetchRecordedList(requireContext())
                }
            }

        findNavController().getBackStackLiveData<Long?>(BACK_FROM_DELETE_RECORD)
            ?.observe(this) { recordId ->
                if (recordId != null) {
                    recorderVM.deleteRecord(recordId).let {
                        if (it) {
                            recorderVM.fetchRecordedList(requireContext())
                        } else
                            requireContext().toast(getString(R.string.cant_find_file))

                    }
                }

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