package com.example.nfc.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nfc.databinding.FragmentHomeBinding
import com.example.nfc.ui.invoice.InvoiceDetailViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.NumberFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private val consultaViewModel: InvoiceDetailViewModel by viewModels()
    
    private lateinit var recentNFCeAdapter: RecentNFCeAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("HomeFragment", "onCreateView: Criando view")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d("HomeFragment", "onCreateView: View criada")
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("HomeFragment", "onViewCreated: Configurando fragment")
        
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        
        Log.d("HomeFragment", "onViewCreated: Carregando dados do dashboard")
        viewModel.loadDashboardData()
    }
    
    private fun setupRecyclerView() {
        recentNFCeAdapter = RecentNFCeAdapter { nfce ->
            // Navegar para detalhes da NFC-e usando Bundle
            val bundle = Bundle().apply {
                putString("chaveAcesso", nfce.chaveAcesso)
            }
            findNavController().navigate(
                com.example.nfc.R.id.action_home_to_invoice_detail,
                bundle
            )
        }
        
        binding.recyclerViewRecentNfce.apply {
            adapter = recentNFCeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupClickListeners() {
        binding.fabScan.setOnClickListener {
            // Navegar para scanner
            findNavController().navigate(com.example.nfc.R.id.action_home_to_scanner)
        }
        
        binding.cardTotalMes.setOnClickListener {
            // Navegar para tela de relatórios detalhados
        }
        
        binding.cardTotalAno.setOnClickListener {
            // Navegar para consulta por URL
            testConsultaUrl()
        }
    }
    
    private fun setupObservers() {
        viewModel.estatisticas.observe(viewLifecycleOwner) { stats ->
            stats?.let { displayEstatisticas(it) }
        }
        
        viewModel.gastosMensais.observe(viewLifecycleOwner) { gastos ->
            setupBarChart(gastos)
        }
        
        viewModel.recentNFCe.observe(viewLifecycleOwner) { nfces ->
            recentNFCeAdapter.submitList(nfces)
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun displayEstatisticas(stats: com.example.nfc.data.model.EstatisticasGastos) {
        binding.apply {
            textTotalMes.text = currencyFormat.format(stats.totalMes)
            textTotalAno.text = currencyFormat.format(stats.totalAno)
            textMediaDiaria.text = currencyFormat.format(stats.mediaGastosDiarios)
            textQuantidadeNotas.text = "${stats.quantidadeNotasMes} notas"
            
            // Mostrar maior e menor gasto
            textMaiorGasto.text = "Maior: ${currencyFormat.format(stats.maiorGasto)}"
            textMenorGasto.text = "Menor: ${currencyFormat.format(stats.menorGasto)}"
        }
    }
    
    private fun setupBarChart(gastosMensais: List<com.example.nfc.data.model.GastoMensal>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        
        gastosMensais.forEachIndexed { index, gasto ->
            entries.add(BarEntry(index.toFloat(), gasto.valorTotal.toFloat()))
            labels.add("${gasto.mes}/${gasto.ano}")
        }
        
        val dataSet = BarDataSet(entries, "Gastos Mensais").apply {
            color = Color.parseColor("#2196F3")
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return currencyFormat.format(value.toDouble())
                }
            }
        }
        
        val barData = BarData(dataSet)
        
        binding.chartGastosMensais.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = true
            
            // Configurar eixo X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                labelRotationAngle = -45f
                textColor = Color.BLACK
            }
            
            // Configurar eixo Y esquerdo
            axisLeft.apply {
                textColor = Color.BLACK
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "R$ ${String.format("%.0f", value)}"
                    }
                }
            }
            
            // Desabilitar eixo Y direito
            axisRight.isEnabled = false
            
            // Animação
            animateY(1000, Easing.EaseInOutQuart)
            
            invalidate()
        }
    }
    
    private fun testConsultaUrl() {
        // URL exata fornecida pelo usuário
        val urlTeste = "https://sat.sef.sc.gov.br/nfce/consulta?p=42251009477652004779651170000401431910501898|2|1|1|F7B1D144F992A290EE192C870B5F362A546D63FC"
        
        Log.d("HomeFragment", "=== TESTE DE CONSULTA NFC-e ===")
        Log.d("HomeFragment", "URL: $urlTeste")
        
        // Mostrar toast para feedback imediato
        android.widget.Toast.makeText(
            requireContext(), 
            "Consultando NFC-e...", 
            android.widget.Toast.LENGTH_SHORT
        ).show()
        
        consultaViewModel.consultarPorUrl(urlTeste)
        
        // Observar resultado apenas uma vez
        consultaViewModel.nfce.removeObservers(viewLifecycleOwner)
        consultaViewModel.error.removeObservers(viewLifecycleOwner)
        
        consultaViewModel.nfce.observe(viewLifecycleOwner) { nfce ->
            if (nfce != null) {
                Log.d("HomeFragment", "✅ NFC-e consultada com sucesso!")
                Log.d("HomeFragment", "Chave: ${nfce.chaveAcesso}")
                Log.d("HomeFragment", "Emitente: ${nfce.nomeEmitente}")
                Log.d("HomeFragment", "Valor: ${nfce.valorTotal}")
                Log.d("HomeFragment", "Status: ${nfce.status}")
                
                android.widget.Toast.makeText(
                    requireContext(), 
                    "NFC-e encontrada: ${nfce.nomeEmitente}", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
                
                // Navegar para detalhes
                val bundle = Bundle().apply {
                    putString("chaveAcesso", nfce.chaveAcesso)
                }
                findNavController().navigate(
                    com.example.nfc.R.id.action_home_to_invoice_detail,
                    bundle
                )
            }
        }
        
        consultaViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Log.e("HomeFragment", "❌ Erro na consulta: $error")
                android.widget.Toast.makeText(
                    requireContext(), 
                    "Erro: $error", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
                consultaViewModel.clearError()
            }
        }
        
        consultaViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("HomeFragment", "Loading: $isLoading")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

