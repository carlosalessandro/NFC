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
import com.example.nfc.data.api.NFCeApiClient
import com.example.nfc.utils.NFCeUrlParser

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
                        
                        // Tentar consultar dados online
                        consultarNFCeAPI(chaveAcesso)
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
        try {
            android.util.Log.d("NFCeConsulta", "Iniciando consulta para chave: $chaveAcesso")
            
            // Gerar URL de consulta para SC (padrão)
            val urlConsulta = NFCeUrlParser.gerarUrlConsulta(chaveAcesso, "SC")
            android.util.Log.d("NFCeConsulta", "URL gerada: $urlConsulta")
            
            if (urlConsulta != null) {
                val response = NFCeApiClient.nfceService.consultarNFCe(urlConsulta)
                android.util.Log.d("NFCeConsulta", "Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val htmlContent = response.body()?.string()
                    android.util.Log.d("NFCeConsulta", "HTML recebido: ${htmlContent?.take(500)}...")
                    
                    if (htmlContent != null) {
                        // Extrair dados do HTML retornado
                        val dadosExtraidos = extrairDadosHTML(htmlContent)
                        android.util.Log.d("NFCeConsulta", "Dados extraídos: $dadosExtraidos")
                        
                        if (dadosExtraidos != null) {
                            // Atualizar NFC-e com dados reais
                            val nfceAtualizada = _nfce.value?.copy(
                                nomeEmitente = dadosExtraidos.nomeEmitente,
                                valorTotal = dadosExtraidos.valorTotal,
                                status = "ATIVA"
                            )
                            
                            if (nfceAtualizada != null) {
                                repository.updateNFCe(nfceAtualizada)
                                _nfce.value = nfceAtualizada
                                android.util.Log.d("NFCeConsulta", "NFC-e atualizada com sucesso")
                            }
                        } else {
                            android.util.Log.w("NFCeConsulta", "Não foi possível extrair dados do HTML")
                        }
                    }
                } else {
                    android.util.Log.e("NFCeConsulta", "Erro HTTP: ${response.code()} - ${response.message()}")
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("NFCeConsulta", "Error body: $errorBody")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NFCeConsulta", "Erro na consulta: ${e.message}", e)
        }
    }
    
    private fun extrairDadosHTML(html: String): DadosNFCe? {
        return try {
            android.util.Log.d("NFCeParser", "Tentando extrair dados do HTML...")
            
            // Tentar diferentes padrões para nome do emitente
            var nomeEmitente = extrairTextoEntre(html, "Razão Social:", "</") 
                ?: extrairTextoEntre(html, "Nome/Razão Social:", "</")
                ?: extrairTextoEntre(html, "Emitente:", "</")
                ?: extrairTextoEntre(html, "razao-social", "</")
                ?: extrairTextoEntre(html, "nome-emitente", "</")
            
            // Tentar diferentes padrões para valor total
            var valorTotalStr = extrairTextoEntre(html, "Valor Total:", "</")
                ?: extrairTextoEntre(html, "Total da Nota:", "</")
                ?: extrairTextoEntre(html, "valor-total", "</")
                ?: extrairTextoEntre(html, "total-nfce", "</")
            
            // Limpar nome do emitente
            nomeEmitente = nomeEmitente?.trim()?.replace(Regex("<[^>]*>"), "")
            
            // Processar valor total
            val valorTotal = valorTotalStr?.let { valor ->
                valor.replace(Regex("[^0-9,.]"), "")
                    .replace(",", ".")
                    .toDoubleOrNull()
            } ?: 0.0
            
            android.util.Log.d("NFCeParser", "Nome extraído: '$nomeEmitente'")
            android.util.Log.d("NFCeParser", "Valor extraído: '$valorTotalStr' -> $valorTotal")
            
            // Se não conseguiu extrair dados específicos, tentar buscar por padrões gerais
            if (nomeEmitente.isNullOrBlank()) {
                nomeEmitente = buscarNomeGenerico(html)
                android.util.Log.d("NFCeParser", "Nome genérico: '$nomeEmitente'")
            }
            
            if (nomeEmitente != null && nomeEmitente.isNotBlank()) {
                DadosNFCe(nomeEmitente, valorTotal)
            } else {
                android.util.Log.w("NFCeParser", "Não foi possível extrair nome do emitente")
                null
            }
            
        } catch (e: Exception) {
            android.util.Log.e("NFCeParser", "Erro ao extrair dados: ${e.message}", e)
            null
        }
    }
    
    private fun buscarNomeGenerico(html: String): String? {
        // Buscar por padrões comuns em páginas de consulta NFC-e
        val padroes = listOf(
            Regex("CNPJ[^>]*>\\s*([^<]+)<"),
            Regex("Emitente[^>]*>\\s*([^<]+)<"),
            Regex("Empresa[^>]*>\\s*([^<]+)<"),
            Regex("class=\"[^\"]*emitente[^\"]*\"[^>]*>\\s*([^<]+)<"),
            Regex("id=\"[^\"]*emitente[^\"]*\"[^>]*>\\s*([^<]+)<")
        )
        
        for (padrao in padroes) {
            val match = padrao.find(html)
            if (match != null && match.groupValues.size > 1) {
                val nome = match.groupValues[1].trim()
                if (nome.isNotBlank() && nome.length > 3) {
                    return nome
                }
            }
        }
        
        return null
    }
    
    private fun extrairTextoEntre(texto: String, inicio: String, fim: String): String? {
        val startIndex = texto.indexOf(inicio)
        if (startIndex == -1) return null
        
        val endIndex = texto.indexOf(fim, startIndex + inicio.length)
        if (endIndex == -1) return null
        
        return texto.substring(startIndex + inicio.length, endIndex).trim()
    }
    
    data class DadosNFCe(
        val nomeEmitente: String,
        val valorTotal: Double
    )
    
    fun consultarPorUrl(url: String) {
        viewModelScope.launch {
            _loading.value = true
            
            try {
                android.util.Log.d("NFCeConsulta", "Consultando URL: $url")
                
                val urlInfo = NFCeUrlParser.parseUrl(url)
                android.util.Log.d("NFCeConsulta", "URL Info: $urlInfo")
                
                if (urlInfo != null) {
                    // Primeiro, criar entrada básica no banco
                    val nfceBasica = NFCe(
                        chaveAcesso = urlInfo.chaveAcesso,
                        numeroNota = urlInfo.chaveAcesso.takeLast(9),
                        serie = urlInfo.chaveAcesso.substring(22, 25),
                        dataEmissao = DateUtils.getCurrentTimestamp(),
                        cnpjEmitente = urlInfo.chaveAcesso.substring(6, 20),
                        nomeEmitente = "Consultando...",
                        valorTotal = 0.0,
                        status = "CONSULTANDO"
                    )
                    
                    val nfceId = repository.insertNFCe(nfceBasica)
                    val nfceSalva = repository.getNFCeById(nfceId)
                    _nfce.value = nfceSalva
                    
                    // Depois, tentar consultar online com a URL específica
                    android.util.Log.d("NFCeConsulta", "Fazendo requisição HTTP...")
                    val response = NFCeApiClient.nfceService.consultarNFCe(url)
                    android.util.Log.d("NFCeConsulta", "Response: ${response.code()} - ${response.message()}")
                    
                    if (response.isSuccessful) {
                        val htmlContent = response.body()?.string()
                        android.util.Log.d("NFCeConsulta", "HTML recebido: ${htmlContent?.length} chars")
                        
                        if (htmlContent != null) {
                            val dadosExtraidos = extrairDadosHTML(htmlContent)
                            android.util.Log.d("NFCeConsulta", "Dados extraídos: $dadosExtraidos")
                            
                            if (dadosExtraidos != null) {
                                val nfceAtualizada = _nfce.value?.copy(
                                    nomeEmitente = dadosExtraidos.nomeEmitente,
                                    valorTotal = dadosExtraidos.valorTotal,
                                    status = "ATIVA"
                                )
                                
                                if (nfceAtualizada != null) {
                                    repository.updateNFCe(nfceAtualizada)
                                    _nfce.value = nfceAtualizada
                                    android.util.Log.d("NFCeConsulta", "NFC-e atualizada com dados reais")
                                }
                            } else {
                                // Mesmo sem dados específicos, marcar como consultada
                                val nfceConsultada = _nfce.value?.copy(
                                    nomeEmitente = "NFC-e Válida",
                                    status = "CONSULTADA"
                                )
                                if (nfceConsultada != null) {
                                    repository.updateNFCe(nfceConsultada)
                                    _nfce.value = nfceConsultada
                                }
                            }
                        }
                    } else {
                        android.util.Log.e("NFCeConsulta", "Erro HTTP: ${response.code()}")
                        _error.value = "Erro na consulta: ${response.code()} - ${response.message()}"
                    }
                } else {
                    _error.value = "URL de consulta inválida"
                }
            } catch (e: Exception) {
                android.util.Log.e("NFCeConsulta", "Erro na consulta: ${e.message}", e)
                _error.value = "Erro ao consultar NFC-e: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
