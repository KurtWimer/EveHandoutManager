package com.example.evehandoutmanager.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.databinding.FragmentHomeBinding
import okhttp3.internal.toImmutableList

class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel : HomeViewModel by activityViewModels()
    private var adapter = HandoutAdapter(HandoutRemoveListener { handout: Handout ->
        homeViewModel.onRemoveButtonClick(handout)
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //bind data to viewModel
        _binding  = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.viewModel = homeViewModel
        binding.handoutList.adapter = adapter
        binding.lifecycleOwner = this

        //Set Up Live Data Observers
        homeViewModel.handoutList.observe(viewLifecycleOwner) {
            it?.let {
                Log.d("HandoutList", it.toString())
                adapter.submitList(it.toImmutableList())
            }
        }

        homeViewModel.accountList.observe(viewLifecycleOwner) {
            it?.let {
                homeViewModel.updateAccounts(it.toImmutableList())
            }
        }

        return binding.root
    }
}