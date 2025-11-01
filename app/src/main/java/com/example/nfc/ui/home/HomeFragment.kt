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
            // Navegar para tela de relatórios anuais
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

