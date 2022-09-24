package ir.romroid.secureboxrecorder.presentation.fileManager

import android.view.LayoutInflater
import android.view.ViewGroup
import ir.romroid.secureboxrecorder.base.component.recyclerAdapter.BaseAdapter
import ir.romroid.secureboxrecorder.databinding.ItemFileManagerBinding
import ir.romroid.secureboxrecorder.domain.model.FileModel

class FileManagerAdapter : BaseAdapter<
        ItemFileManagerBinding,
        BaseAdapter.VHolder<ItemFileManagerBinding, FileModel>,
        FileModel
        >() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ItemFileManagerBinding
        get() = ItemFileManagerBinding::inflate

    var onDeleteListener: ((item: FileModel) -> Unit)? = null
    var onShareListener: ((item: FileModel) -> Unit)? = null

    override fun onBindViewHolder(
        holder: VHolder<ItemFileManagerBinding, FileModel>,
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