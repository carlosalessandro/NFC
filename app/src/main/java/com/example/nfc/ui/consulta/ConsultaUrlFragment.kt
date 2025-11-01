package com.example.nfc.ui.consulta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nfc.databinding.FragmentConsultaUrlBinding
import com.example.nfc.ui.invoice.InvoiceDetailViewModel

class ConsultaUrlFragment : Fragment() {
    
    private var _binding: FragmentConsultaUrlBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: InvoiceDetailViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultaUrlBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        setupObservers()
        
        // URL de exemplo pré-preenchida
        binding.editTextUrl.setText("https://sat.sef.sc.gov.br/nfce/consulta?p=42251009477652004779651170000401431910501898|2|1|1|F7B1D144F992A290EE192C870B5F362A546D63FC")
    }
    
    private fun setupClickListeners() {
        binding.buttonConsultar.setOnClickListener {
            val url = binding.editTextUrl.text.toString().trim()
            
            if (url.isNotEmpty()) {
                viewModel.consultarPorUrl(url)
            } else {
                Toast.makeText(requireContext(), "Digite uma URL válida", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonLimpar.setOnClickListener {
            binding.editTextUrl.text?.clear()
        }
    }
    
    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonConsultar.isEnabled = !isLoading
        }
        
        viewModel.nfce.observe(viewLifecycleOwner) { nfce ->
            if (nfce != null) {
                // Navegar para detalhes da NFC-e
                val bundle = Bundle().apply {
                    putString("chaveAcesso", nfce.chaveAcesso)
                }
                // Navegar para detalhes (usando ação genérica)
                findNavController().navigate(
                    com.example.nfc.R.id.invoiceDetailFragment,
                    bundle
                )
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
