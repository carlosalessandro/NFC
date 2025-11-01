package com.example.nfc.ui.invoice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nfc.data.database.NFCeDatabase
import com.example.nfc.data.model.ItemNFCe
import com.example.nfc.data.model.NFCe
import com.example.nfc.data.repository.NFCeRepository
import com.example.nfc.utils.QRCodeParser
import kotlinx.coroutines.launch
import com.example.nfc.utils.DateUtils

class InvoiceDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NFCeRepository
    
    private val _nfce = MutableLiveData<NFCe?>()
    val nfce: LiveData<NFCe?> = _nfce
    
    private val _itens = MutableLiveData<List<ItemNFCe>>()
    val itens: LiveData<List<ItemNFCe>> = _itens
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        val database = NFCeDatabase.getDatabase(application)
        repository = NFCeRepository(database.nfceDao())
    }
    
    fun loadNFCeData(chaveAcesso: String) {
        viewModelScope.launch {
            _loading.value = true
            
            try {
                // Primeiro, tentar buscar no banco local
                val existingNFCe = repository.getNFCeByChave(chaveAcesso)
                
                if (existingNFCe != null) {
                    _nfce.value = existingNFCe
                    val itensNFCe = repository.getItensByNFCeId(existingNFCe.id)
                    _itens.value = itensNFCe
                } else {
                    // Se não encontrar, criar uma NFC-e básica com os dados da chave
                    val chaveInfo = QRCodeParser.extractInfoFromChave(chaveAcesso)
                    
                    if (chaveInfo != null) {
                        val nfceBasica = NFCe(
                            chaveAcesso = chaveAcesso,
                            numeroNota = chaveInfo.numero.toLongOrNull()?.toString() ?: "N/A",
                            serie = chaveInfo.serie.toIntOrNull()?.toString() ?: "N/A",
                            dataEmissao = DateUtils.getCurrentTimestamp(),
                            cnpjEmitente = chaveInfo.cnpj,
                            nomeEmitente = "Emitente não identificado",
                            valorTotal = 0.0,
                            status = "PENDENTE"
                        )
                        
                        // Salvar no banco para futuras consultas
                        val nfceId = repository.insertNFCe(nfceBasica)
                        val nfceSalva = repository.getNFCeById(nfceId)
                        _nfce.value = nfceSalva
                        _itens.value = emptyList()
                        
                        // Aqui você poderia fazer uma consulta à API para buscar mais dados
                        // consultarNFCeAPI(chaveAcesso)
                    } else {
                        _error.value = "Chave de acesso inválida"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados da NFC-e: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    
    private suspend fun consultarNFCeAPI(chaveAcesso: String) {
        // TODO: Implementar consulta à API da Receita Federal ou outro serviço
        // Esta funcionalidade requer integração com APIs específicas de cada estado
    }
    
    fun clearError() {
        _error.value = null
    }
}
