package com.example.urbancanopy.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urbancanopy.databinding.FragmentLeaderboardBinding
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.viewmodel.LeaderboardViewModel
import com.example.urbancanopy.viewmodel.LeaderboardViewModelFactory

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LeaderboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = Repository()
        val factory = LeaderboardViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(LeaderboardViewModel::class.java)

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.leaderboard.observe(viewLifecycleOwner) { entries ->
            // In a real app, update the RecyclerView adapter here
            
            // Highlight current user (mock logic)
            val currentUser = Repository().getCurrentUser()
            val myEntry = entries.find { it.userId == currentUser?.uid }
            myEntry?.let {
                binding.tvUserRank.text = "#${it.rank}"
                binding.tvUserPoints.text = "${it.points} pts"
                binding.tvUserName.text = "You (${it.username})"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
