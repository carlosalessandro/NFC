package com.example.nfc.utils

import android.net.Uri
import java.util.regex.Pattern

data class NFCeUrlInfo(
    val chaveAcesso: String,
    val urlCompleta: String,
    val parametros: String,
    val estado: String,
    val baseUrl: String
)

object NFCeUrlParser {
    
    // Mapeamento de URLs por estado
    private val urlsEstados = mapOf(
        "SC" to "https://sat.sef.sc.gov.br/",
        "SP" to "https://www.fazenda.sp.gov.br/",
        "RJ" to "https://www.fazenda.rj.gov.br/",
        "MG" to "https://www.fazenda.mg.gov.br/",
        "PR" to "https://www.fazenda.pr.gov.br/",
        "RS" to "https://www.sefaz.rs.gov.br/",
        // Adicione mais estados conforme necessário
    )
    
    /**
     * Extrai informações de uma URL de consulta NFC-e
     */
    fun parseUrl(url: String): NFCeUrlInfo? {
        return try {
            val uri = Uri.parse(url)
            val parametros = uri.getQueryParameter("p") ?: return null
            
            // Extrair chave de acesso (primeiros 44 dígitos)
            val chaveAcesso = parametros.split("|").firstOrNull()?.take(44) ?: return null
            
            // Determinar estado pela URL
            val estado = determinarEstado(uri.host ?: "")
            val baseUrl = "${uri.scheme}://${uri.host}/"
            
            NFCeUrlInfo(
                chaveAcesso = chaveAcesso,
                urlCompleta = url,
                parametros = parametros,
                estado = estado,
                baseUrl = baseUrl
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Gera URL de consulta para um estado específico
     */
    fun gerarUrlConsulta(chaveAcesso: String, estado: String = "SC"): String? {
        val baseUrl = urlsEstados[estado] ?: return null
        
        // Para SC, usar o formato padrão
        return when (estado) {
            "SC" -> "${baseUrl}nfce/consulta?p=${chaveAcesso}|2|1|1|"
            else -> "${baseUrl}nfce/consulta?p=${chaveAcesso}"
        }
    }
    
    /**
     * Extrai chave de acesso de uma URL
     */
    fun extrairChaveAcesso(url: String): String? {
        return parseUrl(url)?.chaveAcesso
    }
    
    /**
     * Valida se uma URL é de consulta NFC-e
     */
    fun isUrlNFCe(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val path = uri.path ?: ""
            val hasParam = uri.getQueryParameter("p") != null
            
            path.contains("nfce") && path.contains("consulta") && hasParam
        } catch (e: Exception) {
            false
        }
    }
    
    private fun determinarEstado(host: String): String {
        return when {
            host.contains("sc.gov.br") -> "SC"
            host.contains("sp.gov.br") -> "SP"
            host.contains("rj.gov.br") -> "RJ"
            host.contains("mg.gov.br") -> "MG"
            host.contains("pr.gov.br") -> "PR"
            host.contains("rs.gov.br") -> "RS"
            else -> "SC" // Default
        }
    }
    
    /**
     * Valida formato de chave de acesso (44 dígitos)
     */
    fun validarChaveAcesso(chave: String): Boolean {
        return chave.length == 44 && chave.all { it.isDigit() }
    }
}
