package com.example.kafiesta.utilities.adapter_diffutil

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import com.example.kafiesta.databinding.DialogLayoutEditProductBinding
import com.example.kafiesta.databinding.ListItemProductBinding
import com.example.kafiesta.domain.ProductDomain
import com.example.kafiesta.domain.ProductDomaintest
import com.example.kafiesta.domain.Producttest
import com.example.kafiesta.utilities.dialog.ProductEditDialog
import com.example.kafiesta.utilities.helpers.RecyclerClick

class SimpleDiffUtilAdapter(
    private val layoutRes: Int,
    private val onClickCallBack: Any? = null,
    private val context: Context? = null,
) : SimpleListAdapter<Any>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem === newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }

    override fun createBinding(parent: ViewGroup, viewType: Int): ViewDataBinding {
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            layoutRes,
            parent,
            false
        )
    }

    override fun bind(binding: ViewDataBinding, item: Any) {
        when (binding) {
            //deprecated
            is ListItemProductBinding -> {
                binding.model = item as ProductDomaintest
                binding.onClickCallBack = onClickCallBack as RecyclerClick
            }

            is DialogLayoutEditProductBinding -> {
                binding.model = item as ProductDomaintest
//                binding.onClickCallBack = onClickCallBack as RecyclerClick
            }

        }
    }
}