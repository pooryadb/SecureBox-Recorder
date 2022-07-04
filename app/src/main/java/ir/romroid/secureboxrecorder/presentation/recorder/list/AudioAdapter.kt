package ir.romroid.secureboxrecorder.presentation.recorder.list

import android.view.LayoutInflater
import android.view.ViewGroup
import ir.romroid.secureboxrecorder.base.component.recyclerAdapter.BaseAdapter
import ir.romroid.secureboxrecorder.databinding.ItemAudioBinding
import ir.romroid.secureboxrecorder.domain.model.AudioModel

class AudioAdapter : BaseAdapter<
        ItemAudioBinding,
        BaseAdapter.VHolder<ItemAudioBinding, AudioModel>,
        AudioModel
        >() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ItemAudioBinding
        get() = ItemAudioBinding::inflate

    var onItemClick: ((item: AudioModel) -> Unit)? = null
    var onDeleteClick: ((item: AudioModel) -> Unit)? = null

    override fun onBindViewHolder(holder: VHolder<ItemAudioBinding, AudioModel>, position: Int) {
        super.onBindViewHolder(holder, position)

        val item = getItem(position)

        binding?.apply {
            clAudioItem.setOnClickListener {
                onItemClick?.invoke(item)
            }
            tvTitle.text = item.name
            btnDelete.setOnClickListener {
                onDeleteClick?.invoke(item)
            }
        }

    }

}