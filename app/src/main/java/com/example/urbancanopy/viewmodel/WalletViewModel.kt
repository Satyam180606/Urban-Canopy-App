package com.example.urbancanopy.viewmodel

import androidx.lifecycle.*
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.model.Patch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WalletViewModel(private val repository: Repository) : ViewModel() {

    private val _userPatches = MutableLiveData<List<Patch>>()
    val userPatches: LiveData<List<Patch>> = _userPatches

    private val _pointsBalance = MutableLiveData<Int>(0)
    val pointsBalance: LiveData<Int> = _pointsBalance

    init {
        loadData()
    }

    private fun loadData() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                repository.getUserPatches(currentUser.uid).collect {
                    _userPatches.value = it
                }
            }
            
            viewModelScope.launch {
                val profile = repository.getUserProfile(currentUser.uid)
                _pointsBalance.value = profile?.totalPoints ?: 0
            }
        }
    }
}

class WalletViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
