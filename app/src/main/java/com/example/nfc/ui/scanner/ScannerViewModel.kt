package com.example.nfc.ui.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nfc.data.database.NFCeDatabase
import com.example.nfc.data.repository.NFCeRepository
import com.example.nfc.utils.QRCodeParser
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NFCeRepository
    
    private val _scanResult = MutableLiveData<String?>()
    val scanResult: LiveData<String?> = _scanResult
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    init {
        val database = NFCeDatabase.getDatabase(application)
        repository = NFCeRepository(database.nfceDao())
    }
    
    fun processScannedCode(rawValue: String) {
        viewModelScope.launch {
            _loading.value = true
            
            try {
                val chaveAcesso = QRCodeParser.extractChaveAcesso(rawValue)
                
                if (chaveAcesso != null && QRCodeParser.isValidChaveAcesso(chaveAcesso)) {
                    // Verificar se a NFC-e já existe no banco
                    val existingNFCe = repository.getNFCeByChave(chaveAcesso)
                    
                    if (existingNFCe != null) {
                        _scanResult.value = chaveAcesso
                    } else {
                        // Buscar dados da NFC-e via API ou processar offline
                        _scanResult.value = chaveAcesso
                    }
                } else {
                    _error.value = "Código QR ou código de barras inválido para NFC-e"
                }
            } catch (e: Exception) {
                _error.value = "Erro ao processar código: ${e.message}"
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
}
