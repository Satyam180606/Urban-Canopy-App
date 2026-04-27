package com.example.urbancanopy.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.urbancanopy.R
import com.example.urbancanopy.databinding.FragmentHomeBinding
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.viewmodel.HomeViewModel
import com.example.urbancanopy.viewmodel.HomeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = Repository()
        val factory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUsername.text = "Welcome, ${it.username}!"
                binding.tvPoints.text = "Total Points: ${it.totalPoints}"
            }
        }

        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            binding.tvLevel.text = "Level ${stats.level}"
            binding.xpProgressBar.progress = (stats.progress * 100).toInt()
            binding.tvXP.text = "${stats.currentXP} / ${stats.nextLevelXP} XP"
        }
        
        // In a full app, we would set up Adapters for rvLeaderboardPreview and rvMissions here
    }

    private fun setupListeners() {
        binding.btnFindPatch.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_map)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
