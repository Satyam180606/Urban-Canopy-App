package com.example.urbancanopy.viewmodel

import androidx.lifecycle.*
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.model.Report
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MapViewModel(private val repository: Repository) : ViewModel() {

    private val _reports = MutableLiveData<List<Report>>()
    val reports: LiveData<List<Report>> = _reports

    init {
        viewModelScope.launch {
            repository.getReports().collect {
                _reports.value = it
            }
        }
    }

    // This would call the C++ clustering logic via Repository -> GameEngine
    fun getClusteredMarkers(zoom: Float): List<Report> {
        // Simplified for now: in a real app, we'd pass the current report list to JNI
        return _reports.value ?: emptyList()
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
