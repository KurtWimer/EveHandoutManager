package com.example.evehandoutmanager.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.evehandoutmanager.databinding.ListItemAccountBinding

class AccountAdapter(private val logoutListener: AccountLogoutListener) :ListAdapter<Account,AccountAdapter.AccountViewHolder>(AccountDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position), logoutListener)
    }

    class AccountViewHolder private constructor(private val binding: ListItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Account, clickListener: AccountLogoutListener){
            binding.account = item
            binding.logoutListener = clickListener
            //TODO apply character icon
        }

        companion object {
            fun from(parent: ViewGroup) : AccountViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ListItemAccountBinding.inflate(inflater, parent, false)
                return AccountViewHolder(binding)
            }
        }
    }

}

class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
        return  oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
        return oldItem == newItem
    }
}

class AccountLogoutListener(val clickListener: (Account) -> Unit){
    fun onClick(account: Account) = clickListener(account)
}
