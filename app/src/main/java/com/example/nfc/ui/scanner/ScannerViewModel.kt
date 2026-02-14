package com.example.nfc.ui.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nfc.data.database.NFCeDatabase
import com.example.nfc.data.repository.NFCeRepository
import com.example.nfc.utils.QRCodeParser
import com.example.nfc.utils.ChaveAcessoValidator
import com.example.nfc.utils.ValidationResult
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NFCeRepository
    
    private val _scanResult = MutableLiveData<String?>()
    val scanResult: LiveData<String?> = _scanResult
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _validationMessage = MutableLiveData<String?>()
    val validationMessage: LiveData<String?> = _validationMessage
    
    init {
        val database = NFCeDatabase.getDatabase(application)
        repository = NFCeRepository(database.nfceDao())
    }
    
    fun processScannedCode(rawValue: String) {
        viewModelScope.launch {
            _loading.value = true
            _validationMessage.value = "Processando código..."
            
            try {
                val chaveAcesso = QRCodeParser.extractChaveAcesso(rawValue)
                
                if (chaveAcesso != null) {
                    _validationMessage.value = "Validando chave de acesso..."
                    
                    // Validar chave completamente
                    val validacao = ChaveAcessoValidator.validarChaveCompleta(chaveAcesso)
                    
                    when (validacao) {
                        is ValidationResult.Valid -> {
                            _validationMessage.value = "✓ Chave válida! Estado: ${validacao.uf}"
                            
                            // Verificar se a NFC-e já existe no banco
                            val existingNFCe = repository.getNFCeByChave(chaveAcesso)
                            
                            if (existingNFCe != null) {
                                _validationMessage.value = "✓ Nota já cadastrada!"
                            } else {
                                _validationMessage.value = "✓ Nova nota encontrada!"
                            }
                            
                            // Navegar para detalhes
                            _scanResult.value = chaveAcesso
                        }
                        is ValidationResult.Invalid -> {
                            _error.value = "Chave inválida: ${validacao.reason}"
                            _validationMessage.value = null
                        }
                    }
                } else {
                    _error.value = "Não foi possível extrair chave de acesso do código"
                    _validationMessage.value = null
                }
            } catch (e: Exception) {
                _error.value = "Erro ao processar código: ${e.message}"
                _validationMessage.value = null
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearScanResult() {
        _scanResult.value = null
    }
    
    fun clearValidationMessage() {
        _validationMessage.value = null
    }
}
