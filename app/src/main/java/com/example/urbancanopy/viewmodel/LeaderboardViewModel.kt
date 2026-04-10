package com.example.urbancanopy.viewmodel

import androidx.lifecycle.*
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.model.LeaderboardEntry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LeaderboardViewModel(private val repository: Repository) : ViewModel() {

    private val _leaderboard = MutableLiveData<List<LeaderboardEntry>>()
    val leaderboard: LiveData<List<LeaderboardEntry>> = _leaderboard

    init {
        viewModelScope.launch {
            repository.getLeaderboard().collect {
                _leaderboard.value = it
            }
        }
    }
}

class LeaderboardViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeaderboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
