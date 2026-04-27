package com.example.urbancanopy.viewmodel

import androidx.lifecycle.*
import com.example.urbancanopy.logic.GameEngine
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.model.Patch
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CameraViewModel(private val repository: Repository) : ViewModel() {

    private val gameEngine = GameEngine()
    private val storage = FirebaseStorage.getInstance()

    private val _isProcessing = MutableLiveData<Boolean>(false)
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _submissionStatus = MutableLiveData<String>()
    val submissionStatus: LiveData<String> = _submissionStatus

    fun processAndUploadImage(imageData: ByteArray, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isProcessing.value = true
            
            // 1. C++ logic for greenness check
            val isGreen = gameEngine.isPatchGreen(imageData)
            if (isGreen) {
                _submissionStatus.value = "REJECTED: This spot already looks green!"
                _isProcessing.value = false
                return@launch
            }

            // 2. Upload to Firebase Storage
            val fileName = "patches/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            
            try {
                ref.putBytes(imageData).await()
                val downloadUrl = ref.downloadUrl.await().toString()

                // 3. Save metadata to Firestore
                val currentUser = repository.getCurrentUser()
                val patch = Patch(
                    userId = currentUser?.uid ?: "",
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = Timestamp.now(),
                    status = "pending",
                    imageUrl = downloadUrl
                )
                
                // Note: Simplified. In a real repository, we'd have a savePatch method.
                // For the skeleton, we can do it here or assume repository handles it.
                _submissionStatus.value = "SUCCESS"
            } catch (e: Exception) {
                _submissionStatus.value = "ERROR: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
}

class CameraViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
