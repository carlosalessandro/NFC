package com.example.nfc.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nfc.data.database.NFCeDatabase
import com.example.nfc.data.model.EstatisticasGastos
import com.example.nfc.data.model.GastoMensal
import com.example.nfc.data.model.NFCe
import com.example.nfc.data.repository.NFCeRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NFCeRepository
    
    private val _estatisticas = MutableLiveData<EstatisticasGastos?>()
    val estatisticas: LiveData<EstatisticasGastos?> = _estatisticas
    
    private val _gastosMensais = MutableLiveData<List<GastoMensal>>()
    val gastosMensais: LiveData<List<GastoMensal>> = _gastosMensais
    
    private val _recentNFCe = MutableLiveData<List<NFCe>>()
    val recentNFCe: LiveData<List<NFCe>> = _recentNFCe
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    // Observar todas as NFC-e para atualizações em tempo real
    val allNFCe: LiveData<List<NFCe>>
    
    init {
        val database = NFCeDatabase.getDatabase(application)
        repository = NFCeRepository(database.nfceDao())
        allNFCe = repository.getAllNFCe()
        
        // Observar mudanças nas NFC-e para atualizar dashboard
        allNFCe.observeForever { nfces ->
            _recentNFCe.value = nfces.take(5) // Mostrar apenas as 5 mais recentes
            loadDashboardData()
        }
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _loading.value = true
            
            try {
                // Carregar estatísticas
                val stats = repository.getEstatisticasGastos()
                _estatisticas.value = stats
                
                // Carregar gastos mensais
                val gastos = repository.getGastosPorMes()
                _gastosMensais.value = gastos.take(12) // Últimos 12 meses
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
}
