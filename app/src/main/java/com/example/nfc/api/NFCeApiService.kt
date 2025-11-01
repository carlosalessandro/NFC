package com.example.nfc.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NFCeApiService {
    
    @GET("nfce")
    suspend fun consultarNFCe(
        @Query("chave") chaveAcesso: String,
        @Query("formato") formato: String = "json"
    ): Response<NFCeResponse>
}

data class NFCeResponse(
    val status: String,
    val retorno: NFCeData?
)

data class NFCeData(
    val chave: String,
    val numero: String,
    val serie: String,
    val dataEmissao: String,
    val cnpjEmitente: String,
    val nomeEmitente: String,
    val valorTotal: Double,
    val valorTributos: Double?,
    val itens: List<ItemResponse>?
)

data class ItemResponse(
    val codigo: String,
    val descricao: String,
    val quantidade: Double,
    val unidade: String,
    val valorUnitario: Double,
    val valorTotal: Double,
    val ncm: String?,
    val cfop: String?
)
