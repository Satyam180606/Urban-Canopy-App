package com.example.urbancanopy.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urbancanopy.model.Report
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class ReportViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance("gs://urban-canopy-solution.firebasestorage.app")
    private val database = FirebaseDatabase.getInstance("https://urban-canopy-solution-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("reports")

    private val _capturedImage = MutableLiveData<Bitmap?>()
    val capturedImage: LiveData<Bitmap?> = _capturedImage

    private val _location = MutableLiveData<LatLng?>()
    val location: LiveData<LatLng?> = _location

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> = _address

    private val _severity = MutableLiveData<String>()
    val severity: LiveData<String> = _severity

    private val _isAccessible = MutableLiveData<Boolean>()
    val isAccessible: LiveData<Boolean> = _isAccessible

    private val _violationType = MutableLiveData<String>()
    val violationType: LiveData<String> = _violationType

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _isSubmitting = MutableLiveData<Boolean>(false)
    val isSubmitting: LiveData<Boolean> = _isSubmitting

    private val _submissionResult = MutableLiveData<Result<String>?>()
    val submissionResult: LiveData<Result<String>?> = _submissionResult

    fun setCapturedImage(bitmap: Bitmap?) {
        _capturedImage.value = bitmap
    }

    fun setLocation(latLng: LatLng, addr: String) {
        _location.value = latLng
        _address.value = addr
    }

    fun setSeverity(sev: String) {
        _severity.value = sev
    }

    fun setAccessibility(accessible: Boolean) {
        _isAccessible.value = accessible
    }

    fun setViolationType(type: String) {
        _violationType.value = type
    }

    fun setDescription(desc: String) {
        _description.value = desc
    }

    fun submitReport() {
        val bitmap = _capturedImage.value ?: return
        val latLng = _location.value ?: return
        val addr = _address.value ?: ""
        val sev = _severity.value ?: "Medium"
        val accessible = _isAccessible.value ?: true
        val vType = _violationType.value ?: ""
        val desc = _description.value ?: ""

        _isSubmitting.value = true

        try {
            val storage = FirebaseStorage.getInstance()
            // Robust check for placeholder bucket
            if (storage.reference.bucket.isEmpty() || storage.reference.bucket.contains("placeholder")) {
                _isSubmitting.value = false
                _submissionResult.value = Result.failure(Exception("Firebase Storage bucket not configured. Please add your real google-services.json from Firebase Console."))
                return
            }

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val imageData = stream.toByteArray()

            val imageId = UUID.randomUUID().toString()
            val imageRef = storage.reference.child("report_images/$imageId.jpg")

            imageRef.putBytes(imageData)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        saveReportToDatabase(
                            Report(
                                id = imageId,
                                imageUrl = downloadUri.toString(),
                                latitude = latLng.latitude,
                                longitude = latLng.longitude,
                                address = addr,
                                severity = sev,
                                isAccessible = accessible,
                                violationType = vType,
                                description = desc,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else {
                        _isSubmitting.value = false
                        _submissionResult.value = Result.failure(task.exception ?: Exception("Upload failed"))
                    }
                }
        } catch (e: Exception) {
            _isSubmitting.value = false
            _submissionResult.value = Result.failure(e)
        }
    }

    private fun saveReportToDatabase(report: Report) {
        database.child(report.id).setValue(report)
            .addOnSuccessListener {
                _isSubmitting.value = false
                _submissionResult.value = Result.success("Report Submitted Successfully")
            }
            .addOnFailureListener {
                _isSubmitting.value = false
                _submissionResult.value = Result.failure(it)
            }
    }
}
