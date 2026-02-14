package com.example.nfc.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NFCeApiClient {
    
    private const val TIMEOUT_SECONDS = 30L
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://sat.sef.sc.gov.br/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val nfceService: NFCeConsultaService = retrofit.create(NFCeConsultaService::class.java)
    val xmlService: NFCeXmlService = retrofit.create(NFCeXmlService::class.java)
    
    /**
     * Cria um cliente para diferentes estados
     */
    fun createServiceForState(baseUrl: String): NFCeConsultaService {
        val stateRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return stateRetrofit.create(NFCeConsultaService::class.java)
    }
}
