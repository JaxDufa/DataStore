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

package com.example.datastore.user.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.example.datastore.R
import com.example.datastore.databinding.FragmentAddUserBinding
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddUserFragment : Fragment() {

    private val viewModel: AddUserViewModel by viewModel()
    private var _binding: FragmentAddUserBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            viewModel.state.asLiveData().observe(viewLifecycleOwner) {

                when (it) {
                    is AddUserViewModel.State.Started -> {
                        inputTextCode.editText?.setText(it.code)
                        val professionsAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, it.professionNames)
                        (inputTextProfession.editText as? AutoCompleteTextView)?.setAdapter(professionsAdapter)
                    }
                    is AddUserViewModel.State.Completed -> findNavController().popBackStack()
                    else -> Unit
                }
            }

            buttonNext.setOnClickListener {
                viewModel.addUser(inputTextName.text(), inputTextEmail.text(), inputTextCode.text(), inputTextProfession.text())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun TextInputLayout.text(): String = editText?.text.toString()
}
