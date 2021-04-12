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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.datastore.R
import com.example.datastore.RELOAD_KEY
import com.example.datastore.databinding.FragmentUserListBinding
import com.example.datastore.store.USER_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserListFragment : Fragment() {

    private val viewModel: UserListViewModel by viewModel()
    private var _binding: FragmentUserListBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUsers()

        val adapter = UserAdapter {
            findNavController().navigate(R.id.action_toUserDetails, bundleOf(USER_KEY to it))
        }

        val backStackStateHandler = findNavController().currentBackStackEntry?.savedStateHandle
        backStackStateHandler?.getLiveData<Boolean>(RELOAD_KEY)?.observe(viewLifecycleOwner) { result ->
            if (result) viewModel.loadUsers()
        }

        with(binding) {

            viewModel.state.observe(viewLifecycleOwner) {
                when (it) {
                    is UserListViewModel.State.Started -> {
                        adapter.submitList(it.users)
                    }
                }
            }
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(context)
            fabAdd.setOnClickListener {
                findNavController().navigate(R.id.action_toAddUser)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}