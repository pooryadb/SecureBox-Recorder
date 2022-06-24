package ir.romroid.secureboxrecorder.presentation.recordList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.romroid.secureboxrecorder.base.component.BaseFragment
import ir.romroid.secureboxrecorder.databinding.FragmentRecordListBinding
import ir.romroid.secureboxrecorder.ext.getBackStackLiveData
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.utils.BACK_FROM_RECORDER
import javax.inject.Inject

@AndroidEntryPoint
class RecordListFragment : BaseFragment<FragmentRecordListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecordListBinding
        get() = FragmentRecordListBinding::inflate

    @Inject
    lateinit var audioAdapter: AudioAdapter

    private val recorderListViewModel by viewModels<RecorderListViewModel>()

    override fun viewHandler(view: View, savedInstanceState: Bundle?) {
        binding?.apply {

            btnAdd.setOnClickListener {
                findNavController().navigate(
                    RecordListFragmentDirections.actionRecordListFragmentToDialogRecordList()
                )
            }

            rcAudio.apply {
                audioAdapter.apply {
                    onDeleteClick = {
                        logD("onDeleteClick")
                    }

                    onItemClick = {
                        logD("onItemClick")
                    }

                }

                adapter = audioAdapter

            }

            if (isRestoredFromBackStack.not())
                recorderListViewModel.fetchRecordedList(requireContext())
        }
    }

    override fun initObservers() {
        super.initObservers()

        recorderListViewModel.liveRecordedList.observe(this) {
            audioAdapter.submitList(it)
        }
    }

    override fun initBackStackObservers() {
        super.initBackStackObservers()

        findNavController().getBackStackLiveData<Boolean>(BACK_FROM_RECORDER)
            ?.observe(this) {
                if (it) {
                    // TODO: goto file manager page
                } else {
                    recorderListViewModel.fetchRecordedList(requireContext())
                }
            }
    }

}