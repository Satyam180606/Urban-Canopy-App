package com.example.urbancanopy.ui.report

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        
        binding.btnRetake.setOnClickListener {
            findNavController().popBackStack(com.example.urbancanopy.R.id.cameraFragment, false)
        }
        
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val words = s?.toString()?.trim()?.split("\\s+".toRegex())?.filter { it.isNotEmpty() } ?: emptyList()
                binding.tvWordCount.text = "${words.size} / 100 words"
                
                if (words.size >= 100) {
                    binding.tvWordCount.setTextColor(android.graphics.Color.parseColor("#10A37F"))
                } else {
                    binding.tvWordCount.setTextColor(android.graphics.Color.parseColor("#5E6267"))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnSubmitReport.setOnClickListener {
            val words = binding.etDescription.text.toString().trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            if (words.size < 100) {
                Toast.makeText(requireContext(), "Description must be at least 100 words", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Report Submitted Successfully!", Toast.LENGTH_LONG).show()
                findNavController().navigate(com.example.urbancanopy.R.id.homeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
