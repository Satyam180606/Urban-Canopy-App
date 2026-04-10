package com.example.urbancanopy.ui.camera

import com.example.urbancanopy.viewmodel.ReportViewModel
import com.example.urbancanopy.R
import androidx.navigation.fragment.findNavController
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.urbancanopy.databinding.FragmentCameraBinding
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.viewmodel.CameraViewModel
import com.example.urbancanopy.viewmodel.CameraViewModelFactory
import com.google.common.util.concurrent.ListenableFuture
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CameraViewModel
    private lateinit var reportViewModel: ReportViewModel
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private lateinit var cameraExecutor: ExecutorService
    private var capturedBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = Repository()
        val factory = CameraViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(CameraViewModel::class.java)
        reportViewModel = ViewModelProvider(requireActivity()).get(ReportViewModel::class.java)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        binding.imageCaptureButton.setOnClickListener { takePhoto() }
        binding.btnFlipCamera.setOnClickListener { flipCamera() }
        binding.btnFlash.setOnClickListener { toggleFlash() }
        
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupObservers()
    }

    private fun flipCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    private fun toggleFlash() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        
        val icon = when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> android.R.drawable.btn_star_big_on
            ImageCapture.FLASH_MODE_AUTO -> android.R.drawable.ic_menu_help
            else -> android.R.drawable.stat_sys_warning
        }
        binding.btnFlash.setImageResource(icon)
        startCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.submissionStatus.observe(viewLifecycleOwner) { status ->
            if (status == "SUCCESS") {
                Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_LONG).show()
                resetCamera()
            } else if (status != null) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.flashMode = flashMode
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    capturedBitmap = bitmap
                    
                    requireActivity().runOnUiThread {
                        showCapturedImage(bitmap)
                    }
                    image.close()
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraFragment", "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        
        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun showCapturedImage(bitmap: Bitmap) {
        binding.ivCapturedImage.setImageBitmap(bitmap)
        binding.ivCapturedImage.visibility = View.VISIBLE
        
        // Hide camera controls
        binding.reticleContainer.visibility = View.GONE
        binding.bottomControls.visibility = View.GONE
        binding.sideControls.visibility = View.GONE
        
        // Show post-capture controls
        binding.postCaptureControls.visibility = View.VISIBLE
        
        binding.btnDiscard.setOnClickListener {
            resetCamera()
        }
        
        binding.btnConfirm.setOnClickListener {
            reportViewModel.setCapturedImage(bitmap)
            findNavController().navigate(R.id.action_camera_to_reportLocation)
        }
    }

    private fun resetCamera() {
        binding.ivCapturedImage.visibility = View.GONE
        binding.postCaptureControls.visibility = View.GONE
        
        binding.reticleContainer.visibility = View.VISIBLE
        binding.bottomControls.visibility = View.VISIBLE
        binding.sideControls.visibility = View.VISIBLE
        
        capturedBitmap = null
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
