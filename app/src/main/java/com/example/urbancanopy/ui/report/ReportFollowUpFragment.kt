package com.example.urbancanopy.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.urbancanopy.R
import com.example.urbancanopy.databinding.FragmentReportFollowupBinding

import com.example.urbancanopy.viewmodel.ReportViewModel
import androidx.lifecycle.ViewModelProvider

class ReportFollowUpFragment : Fragment() {

    private var _binding: FragmentReportFollowupBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportViewModel: ReportViewModel
    private var selectedSeverity = "Medium"
    private var isAccessible = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportFollowupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reportViewModel = ViewModelProvider(requireActivity()).get(ReportViewModel::class.java)
        
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupSeveritySelection()
        setupAccessibilitySelection()
        
        binding.btnContinue.setOnClickListener {
            reportViewModel.setSeverity(selectedSeverity)
            reportViewModel.setAccessibility(isAccessible)
            findNavController().navigate(R.id.action_reportFollowUp_to_reportDetails)
        }
    }

    private fun setupSeveritySelection() {
        val cards = listOf(binding.cvHigh, binding.cvMedium, binding.cvLow)
        val radios = listOf(binding.rbHigh, binding.rbMedium, binding.rbLow)
        val severities = listOf("High", "Medium", "Low")

        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                cards.forEach { it.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selector_severity_bg)) }
                cards.forEach { it.isSelected = false }
                radios.forEach { it.isChecked = false }

                card.isSelected = true
                card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selector_severity_bg))
                radios[index].isChecked = true
                selectedSeverity = severities[index]
            }
        }
    }

    private fun setupAccessibilitySelection() {
        binding.btnYes.setOnClickListener {
            isAccessible = true
            binding.btnYes.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.nav_background))
            binding.btnYes.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btnYes.setIconTintResource(R.color.white)

            binding.btnNo.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.selector_severity_bg))
            binding.btnNo.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.btnNo.setIconTintResource(R.color.black)
        }

        binding.btnNo.setOnClickListener {
            isAccessible = false
            binding.btnNo.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.nav_background))
            binding.btnNo.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btnNo.setIconTintResource(R.color.white)

            binding.btnYes.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.selector_severity_bg))
            binding.btnYes.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.btnYes.setIconTintResource(R.color.black)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
