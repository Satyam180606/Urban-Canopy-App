package com.example.urbancanopy.viewmodel

import androidx.lifecycle.*
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.model.Patch
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MapViewModel(private val repository: Repository) : ViewModel() {

    private val _patches = MutableLiveData<List<Patch>>()
    val patches: LiveData<List<Patch>> = _patches

    init {
        viewModelScope.launch {
            repository.getOpenMissions().collect {
                _patches.value = it
            }
        }
    }

    // This would call the C++ clustering logic via Repository -> GameEngine
    fun getClusteredMarkers(zoom: Float): List<Patch> {
        // Simplified for now: in a real app, we'd pass the current patch list to JNI
        return _patches.value ?: emptyList()
    }
}

class MapViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
