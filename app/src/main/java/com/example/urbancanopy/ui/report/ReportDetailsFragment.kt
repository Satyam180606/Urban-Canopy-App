package com.example.urbancanopy.ui.report

import com.example.urbancanopy.viewmodel.ReportViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.urbancanopy.databinding.FragmentReportDetailsBinding

class ReportDetailsFragment : Fragment() {

    private var _binding: FragmentReportDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportViewModel: ReportViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reportViewModel = ViewModelProvider(requireActivity()).get(ReportViewModel::class.java)

        reportViewModel.capturedImage.observe(viewLifecycleOwner) { bitmap ->
            binding.ivCapturedPreview.setImageBitmap(bitmap)
        }
        
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        
        binding.btnRetake.setOnClickListener {
            findNavController().popBackStack(com.example.urbancanopy.R.id.cameraFragment, false)
        }
        
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val characters = s?.length ?: 0
                binding.tvWordCount.text = "$characters / 100 characters"
                
                if (characters >= 100) {
                    binding.tvWordCount.setTextColor(android.graphics.Color.parseColor("#10A37F"))
                } else {
                    binding.tvWordCount.setTextColor(android.graphics.Color.parseColor("#5E6267"))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnSubmitReport.setOnClickListener {
            val violationType = binding.etViolationType.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val words = description.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            
            if (violationType.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter violation type", Toast.LENGTH_SHORT).show()
            } else if (description.length < 100) {
                Toast.makeText(requireContext(), "Description must be at least 100 characters", Toast.LENGTH_SHORT).show()
            } else {
                reportViewModel.setViolationType(violationType)
                reportViewModel.setDescription(description)
                reportViewModel.submitReport()
            }
        }

        reportViewModel.isSubmitting.observe(viewLifecycleOwner) { isSubmitting ->
            binding.btnSubmitReport.isEnabled = !isSubmitting
            binding.btnSubmitReport.text = if (isSubmitting) "Submitting..." else "Submit Report"
        }

        reportViewModel.submissionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    findNavController().navigate(com.example.urbancanopy.R.id.action_reportDetails_to_submissionSuccess)
                } else {
                    Toast.makeText(requireContext(), "Error: ${it.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
