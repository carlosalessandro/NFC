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
import com.example.nfc.utils.NFCeHtmlParser
import com.example.nfc.utils.ChaveAcessoValidator
import com.example.nfc.utils.ValidationResult
import com.example.nfc.utils.NFCeXmlParser
import com.example.nfc.utils.NFCeXmlData

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
    
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage
    
    init {
        val database = NFCeDatabase.getDatabase(application)
        repository = NFCeRepository(database.nfceDao())
    }
    
    fun loadNFCeData(chaveAcesso: String) {
        viewModelScope.launch {
            _loading.value = true
            _statusMessage.value = "Validando chave de acesso..."
            
            try {
                // Validar chave de acesso completamente
                val validacao = ChaveAcessoValidator.validarChaveCompleta(chaveAcesso)
                
                when (validacao) {
                    is ValidationResult.Invalid -> {
                        _error.value = "Chave inválida: ${validacao.reason}"
                        _loading.value = false
                        return@launch
                    }
                    is ValidationResult.Valid -> {
                        android.util.Log.d("NFCeValidacao", "Chave válida - UF: ${validacao.uf}, CNPJ: ${validacao.cnpj}")
                        _statusMessage.value = "Chave válida! Buscando dados..."
                    }
                }
                
                // Primeiro, tentar buscar no banco local
                _statusMessage.value = "Verificando banco de dados local..."
                val existingNFCe = repository.getNFCeByChave(chaveAcesso)
                
                if (existingNFCe != null) {
                    _statusMessage.value = "Nota encontrada no banco local!"
                    _nfce.value = existingNFCe
                    val itensNFCe = repository.getItensByNFCeId(existingNFCe.id)
                    _itens.value = itensNFCe
                    
                    // Se status for PENDENTE, tentar atualizar online
                    if (existingNFCe.status == "PENDENTE" || existingNFCe.status == "CONSULTANDO") {
                        consultarNFCeAPI(chaveAcesso)
                    }
                } else {
                    // Se não encontrar, criar uma NFC-e básica com os dados da chave
                    _statusMessage.value = "Criando registro da nota..."
                    val chaveInfo = QRCodeParser.extractInfoFromChave(chaveAcesso)
                    
                    if (chaveInfo != null) {
                        val nfceBasica = NFCe(
                            chaveAcesso = chaveAcesso,
                            numeroNota = chaveInfo.numero.toLongOrNull()?.toString() ?: "N/A",
                            serie = chaveInfo.serie.toIntOrNull()?.toString() ?: "N/A",
                            dataEmissao = DateUtils.getCurrentTimestamp(),
                            cnpjEmitente = chaveInfo.cnpj,
                            nomeEmitente = "Consultando...",
                            valorTotal = 0.0,
                            status = "CONSULTANDO"
                        )
                        
                        // Salvar no banco para futuras consultas
                        val nfceId = repository.insertNFCe(nfceBasica)
                        val nfceSalva = repository.getNFCeById(nfceId)
                        _nfce.value = nfceSalva
                        _itens.value = emptyList()
                        
                        // Tentar consultar dados online
                        consultarNFCeAPI(chaveAcesso)
                    } else {
                        _error.value = "Erro ao processar chave de acesso"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados da NFC-e: ${e.message}"
                android.util.Log.e("NFCeLoad", "Erro: ${e.message}", e)
            } finally {
                _loading.value = false
                _statusMessage.value = null
            }
        }
    }
    
    private suspend fun consultarNFCeAPI(chaveAcesso: String) {
        try {
            _statusMessage.value = "Consultando NFC-e online..."
            android.util.Log.d("NFCeConsulta", "Iniciando consulta para chave: $chaveAcesso")
            
            // Determinar estado pela chave
            val uf = chaveAcesso.substring(0, 2)
            val estado = when (uf) {
                "42" -> "SC"
                "41" -> "PR"
                "43" -> "RS"
                "35" -> "SP"
                else -> "SC" // Default
            }
            
            // Estratégia 1: Tentar consultar XML diretamente
            _statusMessage.value = "Tentando consulta XML..."
            val dadosXml = tentarConsultarXml(chaveAcesso, estado)
            if (dadosXml != null) {
                atualizarNFCeComDados(dadosXml)
                return
            }
            
            // Estratégia 2: Consultar HTML e extrair dados
            _statusMessage.value = "Consultando página HTML..."
            val urlConsulta = NFCeUrlParser.gerarUrlConsulta(chaveAcesso, estado)
            android.util.Log.d("NFCeConsulta", "URL gerada para $estado: $urlConsulta")
            
            if (urlConsulta != null) {
                _statusMessage.value = "Conectando ao servidor da SEFAZ..."
                val response = NFCeApiClient.nfceService.consultarNFCe(urlConsulta)
                android.util.Log.d("NFCeConsulta", "Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    _statusMessage.value = "Processando resposta..."
                    val htmlContent = response.body()?.string()
                    android.util.Log.d("NFCeConsulta", "HTML recebido: ${htmlContent?.length} chars")
                    
                    if (htmlContent != null) {
                        // Usar o parser robusto (que já tenta XML primeiro)
                        val dadosExtraidos = NFCeHtmlParser.extrairDados(htmlContent)
                        android.util.Log.d("NFCeConsulta", "Dados extraídos: $dadosExtraidos")
                        
                        if (dadosExtraidos != null) {
                            _statusMessage.value = "Atualizando dados da nota..."
                            
                            // Atualizar NFC-e com dados reais
                            val nfceAtualizada = _nfce.value?.copy(
                                nomeEmitente = dadosExtraidos.nomeEmitente,
                                valorTotal = dadosExtraidos.valorTotal,
                                cnpjEmitente = dadosExtraidos.cnpjEmitente ?: _nfce.value?.cnpjEmitente ?: "",
                                numeroNota = dadosExtraidos.numeroNota ?: _nfce.value?.numeroNota ?: "",
                                serie = dadosExtraidos.serie ?: _nfce.value?.serie ?: "",
                                status = "ATIVA"
                            )
                            
                            if (nfceAtualizada != null) {
                                repository.updateNFCe(nfceAtualizada)
                                _nfce.value = nfceAtualizada
                                
                                // Salvar itens se existirem
                                dadosExtraidos.itens?.let { itensData ->
                                    val itensNFCe = itensData.map { item ->
                                        ItemNFCe(
                                            nfceId = nfceAtualizada.id,
                                            codigo = "",
                                            descricao = item.descricao,
                                            quantidade = item.quantidade,
                                            unidade = "UN",
                                            valorUnitario = item.valorUnitario,
                                            valorTotal = item.valorTotal
                                        )
                                    }
                                    repository.insertItens(itensNFCe)
                                    _itens.value = itensNFCe
                                }
                                
                                _statusMessage.value = "✓ Dados atualizados com sucesso!"
                                android.util.Log.d("NFCeConsulta", "NFC-e atualizada com sucesso")
                            }
                        } else {
                            android.util.Log.w("NFCeConsulta", "Não foi possível extrair dados do HTML")
                            // Marcar como consultada mesmo sem dados específicos
                            val nfceConsultada = _nfce.value?.copy(
                                nomeEmitente = "NFC-e Válida (dados não disponíveis)",
                                status = "CONSULTADA"
                            )
                            if (nfceConsultada != null) {
                                repository.updateNFCe(nfceConsultada)
                                _nfce.value = nfceConsultada
                            }
                            _statusMessage.value = "⚠ Nota válida, mas dados detalhados não disponíveis"
                        }
                    }
                } else {
                    android.util.Log.e("NFCeConsulta", "Erro HTTP: ${response.code()} - ${response.message()}")
                    _statusMessage.value = "⚠ Erro ao consultar: ${response.code()}"
                    
                    // Marcar como erro mas manter dados básicos
                    val nfceErro = _nfce.value?.copy(
                        status = "ERRO_CONSULTA"
                    )
                    if (nfceErro != null) {
                        repository.updateNFCe(nfceErro)
                        _nfce.value = nfceErro
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NFCeConsulta", "Erro na consulta: ${e.message}", e)
            _statusMessage.value = "⚠ Erro de conexão: ${e.message}"
        }
    }
    
    /**
     * Tenta consultar XML diretamente
     */
    private suspend fun tentarConsultarXml(chaveAcesso: String, estado: String): NFCeXmlData? {
        return try {
            // URLs conhecidas para download de XML
            val urlsXml = listOf(
                "https://www.sefaz.${estado.lowercase()}.gov.br/nfce/xml/$chaveAcesso",
                "https://sat.sef.${estado.lowercase()}.gov.br/nfce/xml/$chaveAcesso",
                "https://www.fazenda.${estado.lowercase()}.gov.br/nfce/xml/$chaveAcesso"
            )
            
            for (url in urlsXml) {
                try {
                    android.util.Log.d("NFCeConsulta", "Tentando XML: $url")
                    val response = NFCeApiClient.xmlService.consultarXml(url)
                    
                    if (response.isSuccessful) {
                        val xmlContent = response.body()?.string()
                        if (xmlContent != null && xmlContent.contains("<?xml")) {
                            android.util.Log.d("NFCeConsulta", "XML encontrado!")
                            val dados = NFCeXmlParser.extrairDadosXml(xmlContent)
                            if (dados != null) {
                                return dados
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.d("NFCeConsulta", "Falha em $url: ${e.message}")
                }
            }
            
            null
        } catch (e: Exception) {
            android.util.Log.e("NFCeConsulta", "Erro ao tentar XML: ${e.message}")
            null
        }
    }
    
    /**
     * Atualiza NFC-e com dados do XML
     */
    private suspend fun atualizarNFCeComDados(dadosXml: NFCeXmlData) {
        _statusMessage.value = "Atualizando dados da nota..."
        
        val nfceAtualizada = _nfce.value?.copy(
            nomeEmitente = dadosXml.nomeEmitente,
            valorTotal = dadosXml.valorTotal,
            cnpjEmitente = dadosXml.cnpjEmitente ?: _nfce.value?.cnpjEmitente ?: "",
            numeroNota = dadosXml.numeroNota ?: _nfce.value?.numeroNota ?: "",
            serie = dadosXml.serie ?: _nfce.value?.serie ?: "",
            valorTributos = dadosXml.valorTributos,
            status = "ATIVA"
        )
        
        if (nfceAtualizada != null) {
            repository.updateNFCe(nfceAtualizada)
            _nfce.value = nfceAtualizada
            
            // Salvar itens
            if (dadosXml.itens.isNotEmpty()) {
                val itensNFCe = dadosXml.itens.map { item ->
                    ItemNFCe(
                        nfceId = nfceAtualizada.id,
                        codigo = item.codigo,
                        descricao = item.descricao,
                        quantidade = item.quantidade,
                        unidade = item.unidade,
                        valorUnitario = item.valorUnitario,
                        valorTotal = item.valorTotal,
                        ncm = item.ncm,
                        cfop = item.cfop
                    )
                }
                repository.insertItens(itensNFCe)
                _itens.value = itensNFCe
            }
            
            _statusMessage.value = "✓ Dados atualizados com sucesso!"
            android.util.Log.d("NFCeConsulta", "NFC-e atualizada com XML")
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
            _statusMessage.value = "Analisando URL..."
            
            try {
                android.util.Log.d("NFCeConsulta", "Consultando URL: $url")
                
                val urlInfo = NFCeUrlParser.parseUrl(url)
                android.util.Log.d("NFCeConsulta", "URL Info: $urlInfo")
                
                if (urlInfo != null) {
                    _statusMessage.value = "Validando chave de acesso..."
                    
                    // Validar chave
                    val validacao = ChaveAcessoValidator.validarChaveCompleta(urlInfo.chaveAcesso)
                    
                    when (validacao) {
                        is ValidationResult.Invalid -> {
                            _error.value = "Chave inválida: ${validacao.reason}"
                            _loading.value = false
                            _statusMessage.value = null
                            return@launch
                        }
                        is ValidationResult.Valid -> {
                            android.util.Log.d("NFCeConsulta", "Chave válida - UF: ${validacao.uf}")
                        }
                    }
                    
                    // Criar entrada básica no banco
                    _statusMessage.value = "Criando registro da nota..."
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
                    
                    // Consultar online com a URL específica
                    _statusMessage.value = "Conectando ao servidor da SEFAZ..."
                    android.util.Log.d("NFCeConsulta", "Fazendo requisição HTTP...")
                    val response = NFCeApiClient.nfceService.consultarNFCe(url)
                    android.util.Log.d("NFCeConsulta", "Response: ${response.code()} - ${response.message()}")
                    
                    if (response.isSuccessful) {
                        _statusMessage.value = "Processando resposta..."
                        val htmlContent = response.body()?.string()
                        android.util.Log.d("NFCeConsulta", "HTML recebido: ${htmlContent?.length} chars")
                        
                        if (htmlContent != null) {
                            // Usar o novo parser robusto
                            val dadosExtraidos = NFCeHtmlParser.extrairDados(htmlContent)
                            android.util.Log.d("NFCeConsulta", "Dados extraídos: $dadosExtraidos")
                            
                            if (dadosExtraidos != null) {
                                _statusMessage.value = "Atualizando dados da nota..."
                                
                                val nfceAtualizada = _nfce.value?.copy(
                                    nomeEmitente = dadosExtraidos.nomeEmitente,
                                    valorTotal = dadosExtraidos.valorTotal,
                                    cnpjEmitente = dadosExtraidos.cnpjEmitente ?: _nfce.value?.cnpjEmitente ?: "",
                                    numeroNota = dadosExtraidos.numeroNota ?: _nfce.value?.numeroNota ?: "",
                                    serie = dadosExtraidos.serie ?: _nfce.value?.serie ?: "",
                                    status = "ATIVA"
                                )
                                
                                if (nfceAtualizada != null) {
                                    repository.updateNFCe(nfceAtualizada)
                                    _nfce.value = nfceAtualizada
                                    
                                    // Salvar itens se existirem
                                    dadosExtraidos.itens?.let { itensData ->
                                        val itensNFCe = itensData.map { item ->
                                            ItemNFCe(
                                                nfceId = nfceAtualizada.id,
                                                codigo = "",
                                                descricao = item.descricao,
                                                quantidade = item.quantidade,
                                                unidade = "UN",
                                                valorUnitario = item.valorUnitario,
                                                valorTotal = item.valorTotal
                                            )
                                        }
                                        repository.insertItens(itensNFCe)
                                        _itens.value = itensNFCe
                                    }
                                    
                                    _statusMessage.value = "✓ Dados atualizados com sucesso!"
                                    android.util.Log.d("NFCeConsulta", "NFC-e atualizada com dados reais")
                                }
                            } else {
                                // Mesmo sem dados específicos, marcar como consultada
                                val nfceConsultada = _nfce.value?.copy(
                                    nomeEmitente = "NFC-e Válida (dados não disponíveis)",
                                    status = "CONSULTADA"
                                )
                                if (nfceConsultada != null) {
                                    repository.updateNFCe(nfceConsultada)
                                    _nfce.value = nfceConsultada
                                }
                                _statusMessage.value = "⚠ Nota válida, mas dados detalhados não disponíveis"
                            }
                        }
                    } else {
                        android.util.Log.e("NFCeConsulta", "Erro HTTP: ${response.code()}")
                        _error.value = "Erro na consulta: ${response.code()} - ${response.message()}"
                        _statusMessage.value = null
                    }
                } else {
                    _error.value = "URL de consulta inválida"
                    _statusMessage.value = null
                }
            } catch (e: Exception) {
                android.util.Log.e("NFCeConsulta", "Erro na consulta: ${e.message}", e)
                _error.value = "Erro ao consultar NFC-e: ${e.message}"
                _statusMessage.value = null
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
