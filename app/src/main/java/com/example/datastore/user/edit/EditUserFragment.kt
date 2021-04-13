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

package com.example.datastore.user.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.datastore.R
import com.example.datastore.RELOAD_KEY
import com.example.datastore.databinding.FragmentEditUserBinding
import com.example.datastore.store.USER_KEY
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditUserFragment : Fragment() {

    private val viewModel: EditUserViewModel by viewModel { parametersOf(arguments?.getInt(USER_KEY)!!) }
    private var _binding: FragmentEditUserBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            viewModel.state.observe(viewLifecycleOwner) {
                when (it) {
                    is EditUserViewModel.State.Started -> {
                        val professionsAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, it.professionNames)
                        (inputTextProfession.editText as? AutoCompleteTextView)?.setAdapter(professionsAdapter)

                        viewModel.loadUser()
                    }
                    is EditUserViewModel.State.Loaded -> {
                        inputTextName.editText?.setText(it.user.name)
                        inputTextEmail.editText?.setText(it.user.email)
                        (inputTextProfession.editText as? AutoCompleteTextView)?.setText(it.user.profession.toString(), false)
                    }
                    is EditUserViewModel.State.Completed -> {
                        enableViews(false, inputTextName, inputTextEmail, inputTextProfession)
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(RELOAD_KEY, true)
                        findNavController().popBackStack()
                    }
                }
            }

            buttonRemove.setOnClickListener {
                viewModel.removeUser()
            }

            fabEdit.setOnClickListener {
                enableViews(true, inputTextName, inputTextEmail, inputTextProfession)
                fabSave.visibility = View.VISIBLE
                fabEdit.visibility = View.GONE
            }

            fabSave.setOnClickListener {
                viewModel.editUser(inputTextName.text(), inputTextEmail.text(), inputTextProfession.text())
                fabSave.visibility = View.GONE
                fabEdit.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun enableViews(enable: Boolean, vararg views: View) {
        views.forEach { it.isEnabled = enable }
    }

    private fun TextInputLayout.text(): String = editText?.text.toString()
}