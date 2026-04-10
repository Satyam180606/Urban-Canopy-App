package com.example.urbancanopy.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.urbancanopy.R
import com.example.urbancanopy.databinding.FragmentMapBinding
import com.example.urbancanopy.logic.Repository
import com.example.urbancanopy.viewmodel.MapViewModel
import com.example.urbancanopy.viewmodel.MapViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MapViewModel
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = Repository()
        val factory = MapViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(MapViewModel::class.java)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupListeners()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }

        viewModel.patches.observe(viewLifecycleOwner) { patches ->
            googleMap?.clear()
            for (patch in patches) {
                val color = when (patch.status) {
                    "verified" -> BitmapDescriptorFactory.HUE_GREEN
                    "pending" -> BitmapDescriptorFactory.HUE_RED
                    else -> BitmapDescriptorFactory.HUE_YELLOW
                }
                
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(patch.latitude, patch.longitude))
                        .title(patch.description)
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                )
            }
        }

        googleMap?.setOnMapLongClickListener { latLng ->
            // In a real app, show a Bottom Sheet here
            // For this flow, we'll navigate directly to Camera to "submit" this spot
            findNavController().navigate(R.id.action_map_to_camera)
        }
    }

    private fun setupListeners() {
        binding.fabMyLocation.setOnClickListener {
            // Logic to zoom to user location
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
