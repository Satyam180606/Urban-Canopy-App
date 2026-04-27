package com.example.urbancanopy.viewmodel

import androidx.lifecycle.*
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.logic.UserStats
import com.example.urbancanopy.model.LeaderboardEntry
import com.example.urbancanopy.model.Mission
import com.example.urbancanopy.model.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: Repository) : ViewModel() {

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _userStats = MutableLiveData<UserStats>()
    val userStats: LiveData<UserStats> = _userStats

    private val _missions = MutableLiveData<List<Mission>>()
    val missions: LiveData<List<Mission>> = _missions

    private val _leaderboardPreview = MutableLiveData<List<LeaderboardEntry>>()
    val leaderboardPreview: LiveData<List<LeaderboardEntry>> = _leaderboardPreview

    init {
        loadData()
    }

    private fun loadData() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                val profile = repository.getUserProfile(currentUser.uid)
                _userProfile.value = profile
                profile?.let {
                    _userStats.value = repository.getUserStats(it.totalPoints)
                }
            }
        }

        viewModelScope.launch {
            repository.getMissions().collect {
                _missions.value = it
            }
        }

        viewModelScope.launch {
            repository.getLeaderboard().collect {
                _leaderboardPreview.value = it.take(3)
            }
        }
    }
}

class HomeViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
