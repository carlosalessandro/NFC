package com.example.nfc.ui.invoice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nfc.databinding.FragmentInvoiceDetailBinding
import com.example.nfc.utils.QRCodeParser
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class InvoiceDetailFragment : Fragment() {
    
    private var _binding: FragmentInvoiceDetailBinding? = null
    private val binding get() = _binding!!
    
    // Obter argumentos via Bundle em vez de Safe Args
    private val chaveAcesso: String by lazy {
        arguments?.getString("chaveAcesso") ?: ""
    }
    private val viewModel: InvoiceDetailViewModel by viewModels()
    
    private lateinit var itensAdapter: ItensAdapter
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoiceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        
        // Carregar dados da NFC-e
        viewModel.loadNFCeData(chaveAcesso)
    }
    
    private fun setupRecyclerView() {
        itensAdapter = ItensAdapter()
        binding.recyclerViewItens.apply {
            adapter = itensAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupObservers() {
        viewModel.nfce.observe(viewLifecycleOwner) { nfce ->
            nfce?.let { displayNFCeData(it) }
        }
        
        viewModel.itens.observe(viewLifecycleOwner) { itens ->
            itensAdapter.submitList(itens)
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun displayNFCeData(nfce: com.example.nfc.data.model.NFCe) {
        binding.apply {
            // Informações básicas
            textNumeroNota.text = "Nota: ${nfce.numeroNota}"
            textSerie.text = "Série: ${nfce.serie}"
            textDataEmissao.text = dateFormat.format(nfce.dataEmissao)
            
            // Emitente
            textNomeEmitente.text = nfce.nomeEmitente
            textCnpjEmitente.text = formatCNPJ(nfce.cnpjEmitente)
            
            // Valores
            textValorTotal.text = currencyFormat.format(nfce.valorTotal)
            nfce.valorTributos?.let {
                textValorTributos.text = currencyFormat.format(it)
                layoutTributos.visibility = View.VISIBLE
            } ?: run {
                layoutTributos.visibility = View.GONE
            }
            
            // Chave de acesso
            textChaveAcesso.text = QRCodeParser.formatChaveAcesso(nfce.chaveAcesso)
            
            // Status
            textStatus.text = nfce.status
            when (nfce.status) {
                "ATIVA" -> textStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                "CANCELADA" -> textStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                else -> textStatus.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
        }
    }
    
    private fun formatCNPJ(cnpj: String): String {
        return if (cnpj.length == 14) {
            "${cnpj.substring(0, 2)}.${cnpj.substring(2, 5)}.${cnpj.substring(5, 8)}/" +
            "${cnpj.substring(8, 12)}-${cnpj.substring(12, 14)}"
        } else {
            cnpj
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
