package com.example.nfc.ui.invoice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nfc.databinding.FragmentInvoiceListBinding
import com.example.nfc.ui.home.RecentNFCeAdapter

class InvoiceListFragment : Fragment() {
    
    private var _binding: FragmentInvoiceListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: InvoiceListViewModel by viewModels()
    
    private lateinit var nfceAdapter: RecentNFCeAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoiceListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupRecyclerView() {
        nfceAdapter = RecentNFCeAdapter { nfce ->
            val bundle = Bundle().apply {
                putString("chaveAcesso", nfce.chaveAcesso)
            }
            findNavController().navigate(
                com.example.nfc.R.id.action_invoice_list_to_detail,
                bundle
            )
        }
        
        binding.recyclerViewNfce.apply {
            adapter = nfceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupObservers() {
        viewModel.allNFCe.observe(viewLifecycleOwner) { nfces ->
            nfceAdapter.submitList(nfces)
            
            // Mostrar/ocultar empty state
            if (nfces.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.recyclerViewNfce.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.recyclerViewNfce.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fabScan.setOnClickListener {
            findNavController().navigate(com.example.nfc.R.id.action_invoice_list_to_scanner)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
