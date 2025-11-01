package com.example.nfc.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface NFCeConsultaService {
    
    /**
     * Consulta NFC-e através da URL completa
     */
    @GET
    suspend fun consultarNFCe(@Url url: String): Response<ResponseBody>
    
    /**
     * Consulta NFC-e Santa Catarina
     */
    @GET("nfce/consulta")
    suspend fun consultarNFCeSC(@Query("p") parametros: String): Response<ResponseBody>
}
