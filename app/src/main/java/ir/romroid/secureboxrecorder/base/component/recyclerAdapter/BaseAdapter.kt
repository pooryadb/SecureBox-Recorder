package ir.romroid.secureboxrecorder.base.component.recyclerAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ir.romroid.secureboxrecorder.base.component.model.BaseResponseData
import ir.romroid.secureboxrecorder.ext.cast

abstract class BaseAdapter<
        VB : ViewBinding,
        VH : BaseAdapter.VHolder<VB, RECORD>,
        RECORD : BaseResponseData
        > : ListAdapter<RECORD, VH>(BaseDiffCallback()) {

    var onClickListener: ((RECORD) -> Unit)? = null

    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
    protected var binding: VB? = null
        private set

    open class VHolder<VB : ViewBinding, RECORD : BaseResponseData>(binding: VB?) :
        BaseViewHolder<VB, RECORD>(binding)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.adapter = this.cast<RecyclerView.Adapter<RecyclerView.ViewHolder>>()!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        binding = bindingInflater.invoke(LayoutInflater.from(parent.context), parent, false)
        return VHolder<VB, RECORD>(binding) as VH
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        binding = null
    }

    class BaseDiffCallback<RECORD : BaseResponseData> : DiffUtil.ItemCallback<RECORD>() {
        override fun areItemsTheSame(
            oldItem: RECORD,
            newItem: RECORD
        ): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: RECORD,
            newItem: RECORD
        ): Boolean {
            return oldItem == newItem
        }
    }

}