package ir.romroid.secureboxrecorder.presentation.box

import android.view.LayoutInflater
import android.view.ViewGroup
import ir.romroid.secureboxrecorder.base.component.recyclerAdapter.BaseAdapter
import ir.romroid.secureboxrecorder.databinding.ItemBoxBinding
import ir.romroid.secureboxrecorder.domain.model.FileModel

class BoxAdapter : BaseAdapter<
        ItemBoxBinding,
        BaseAdapter.VHolder<ItemBoxBinding, FileModel>,
        FileModel
        >() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ItemBoxBinding
        get() = ItemBoxBinding::inflate

    var onDeleteListener: ((item: FileModel) -> Unit)? = null
    var onShareListener: ((item: FileModel) -> Unit)? = null

    override fun onBindViewHolder(
        holder: VHolder<ItemBoxBinding, FileModel>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        val item = getItem(position)

        binding?.apply {
            cl.setOnClickListener {
                onClickListener?.invoke(item)
            }
            btnShare.setOnClickListener {
                onShareListener?.invoke(item)
            }
            btnDelete.setOnClickListener {
                onDeleteListener?.invoke(item)
            }

            tvTitle.text = item.name
            ivThumbnail.setImageResource(item.type.imageRes)

        }

    }

}