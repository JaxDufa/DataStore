/*
 * Copyright 2021 ArcTouch LLC.
 * All rights reserved.
 *
 * This file, its contents, concepts, methods, behavior, and operation
 * (collectively the "Software") are protected by trade secret, patent,
 * and copyright laws. The use of the Software is governed by a license
 * agreement. Disclosure of the Software to third parties, in any form,
 * in whole or in part, is expressly prohibited except as authorized by
 * the license agreement.
 */

package com.example.datastore.user.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datastore.databinding.LayoutUserItemBinding
import com.example.datastore.store.UserInfo

class UserAdapter(private val onClickListener: (code: Int) -> Unit) : ListAdapter<UserInfo, UserAdapter.UserViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = LayoutUserItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class UserViewHolder(private val binding: LayoutUserItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserInfo) {
            with(binding) {
                textName.text = item.name
                textEmail.text = item.email

                root.setOnClickListener {
                    onClickListener(item.code)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserInfo>() {

        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }
    }
}