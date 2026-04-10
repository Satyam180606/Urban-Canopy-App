package com.example.urbancanopy.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.urbancanopy.databinding.FragmentWalletBinding
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.viewmodel.WalletViewModel
import com.example.urbancanopy.viewmodel.WalletViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WalletViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = Repository()
        val factory = WalletViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(WalletViewModel::class.java)

        setupTabs()
        setupObservers()
    }

    private fun setupTabs() {
        // In a real app, we would set up a ViewPager2 adapter here
        // For now, we just link the TabLayout and ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Missions" else "Rewards"
        }.attach()
    }

    private fun setupObservers() {
        viewModel.pointsBalance.observe(viewLifecycleOwner) { balance ->
            // Update UI with points balance
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
