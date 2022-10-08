package ir.romroid.secureboxrecorder.base.component.recyclerAdapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ir.romroid.secureboxrecorder.base.component.model.BaseResponseData

abstract class BaseViewHolder<VB : ViewBinding, RESPONSE : BaseResponseData>(val binding: VB?) :
    RecyclerView.ViewHolder(requireNotNull(binding?.root)) {

    internal lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>


    protected open fun bind(item: RESPONSE?) {}
}