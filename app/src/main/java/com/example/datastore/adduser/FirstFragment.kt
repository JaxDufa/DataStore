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

package com.example.datastore.adduser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.datastore.R
import com.example.datastore.databinding.FragmentFirstBinding
import com.example.datastore.viewmodel.ExampleViewModel
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val viewModel: ExampleViewModel by viewModel()
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.preferencesDataStore()
        viewModel.rxPreferencesDataStore()
        viewModel.protoPreferencesDataStore()

        with(binding) {
            buttonNext.setOnClickListener {
                if (inputTextName.isNotEmpty() && inputTextNickName.isNotEmpty() && inputTextAge.isNotEmpty() && inputTextProfession.isNotEmpty()) {
                    viewModel.addUser(inputTextName.text(), inputTextNickName.text(), inputTextAge.text(), inputTextProfession.text())
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                }
            }

            buttonSkip.setOnClickListener {
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }

            val ages = Array(100) { "$it" }
            val adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, ages)
            (inputTextAge.editText as? AutoCompleteTextView)?.setAdapter(adapter)

            val professions = listOf("Material", "Design", "Components", "Android")
            val professionsAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, professions)
            (inputTextProfession.editText as? AutoCompleteTextView)?.setAdapter(professionsAdapter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun TextInputLayout.text(): String = editText?.text.toString()
}