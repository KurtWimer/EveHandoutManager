package com.example.evehandoutmanager.fleetConfiguration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.databinding.FragmentFleetSetupBinding
import okhttp3.internal.toImmutableList

class FleetConfigurationFragment : Fragment() {
    private val viewModel: FleetConfigurationViewModel by viewModels()
    private var adapter = FleetConfigAdapter(FleetRemoveListener { fleetConfigItem: FleetConfigItem ->
        viewModel.onRemoveItem(fleetConfigItem)
    })
    private var _binding : FragmentFleetSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_fleet_setup, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.fleetConfigList.adapter = adapter

        viewModel.configList.observe(viewLifecycleOwner){
            it?.let {
                adapter.submitList(it.toImmutableList())
                binding.emptyRecyclerView.isVisible = it.isEmpty()
            }
        }

        return binding.root
    }
}