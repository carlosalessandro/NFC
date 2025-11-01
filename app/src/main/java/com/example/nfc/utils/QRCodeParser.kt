package com.example.nfc.utils

import java.net.URL
import java.net.URLDecoder

object QRCodeParser {
    
    /**
     * Extrai a chave de acesso de um QR Code de NFC-e
     * Formato típico: https://www.fazenda.pr.gov.br/nfce/qrcode?p=41200214200166000166650010000000046|2|1|1|65B84CA3F2AC2D4A
     */
    fun extractChaveAcesso(qrCodeContent: String): String? {
        return try {
            when {
                // URL com parâmetro p
                qrCodeContent.startsWith("http") -> {
                    val url = URL(qrCodeContent)
                    val query = url.query ?: return null
                    
                    val params = query.split("&").associate { param ->
                        val parts = param.split("=", limit = 2)
                        if (parts.size == 2) {
                            parts[0] to URLDecoder.decode(parts[1], "UTF-8")
                        } else {
                            parts[0] to ""
                        }
                    }
                    
                    params["p"]?.split("|")?.firstOrNull()?.let { chave ->
                        if (chave.length == 44) chave else null
                    }
                }
                
                // Chave direta (44 dígitos)
                qrCodeContent.length == 44 && qrCodeContent.all { it.isDigit() } -> {
                    qrCodeContent
                }
                
                // Código de barras (44 dígitos)
                qrCodeContent.matches(Regex("\\d{44}")) -> {
                    qrCodeContent
                }
                
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Valida se uma chave de acesso está no formato correto
     */
    fun isValidChaveAcesso(chave: String): Boolean {
        return chave.length == 44 && chave.all { it.isDigit() }
    }
    
    /**
     * Formata a chave de acesso para exibição
     */
    fun formatChaveAcesso(chave: String): String {
        return if (chave.length == 44) {
            "${chave.substring(0, 4)} ${chave.substring(4, 8)} ${chave.substring(8, 12)} " +
            "${chave.substring(12, 16)} ${chave.substring(16, 20)} ${chave.substring(20, 24)} " +
            "${chave.substring(24, 28)} ${chave.substring(28, 32)} ${chave.substring(32, 36)} " +
            "${chave.substring(36, 40)} ${chave.substring(40, 44)}"
        } else {
            chave
        }
    }
    
    /**
     * Extrai informações básicas da chave de acesso
     */
    fun extractInfoFromChave(chave: String): ChaveInfo? {
        return if (isValidChaveAcesso(chave)) {
            ChaveInfo(
                uf = chave.substring(0, 2),
                anoMes = chave.substring(2, 6),
                cnpj = chave.substring(6, 20),
                modelo = chave.substring(20, 22),
                serie = chave.substring(22, 25),
                numero = chave.substring(25, 34),
                tipoEmissao = chave.substring(34, 35),
                codigoNumerico = chave.substring(35, 43),
                dv = chave.substring(43, 44)
            )
        } else {
            null
        }
    }
}

data class ChaveInfo(
    val uf: String,
    val anoMes: String,
    val cnpj: String,
    val modelo: String,
    val serie: String,
    val numero: String,
    val tipoEmissao: String,
    val codigoNumerico: String,
    val dv: String
)
