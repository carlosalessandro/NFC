package com.example.nfc.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nfce_table")
data class NFCe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chaveAcesso: String,
    val numeroNota: String,
    val serie: String,
    val dataEmissao: Long, // Timestamp em milissegundos
    val cnpjEmitente: String,
    val nomeEmitente: String,
    val valorTotal: Double,
    val valorTributos: Double? = null,
    val qrCode: String? = null,
    val urlConsulta: String? = null,
    val dataCriacao: Long = System.currentTimeMillis(),
    val status: String = "ATIVA" // ATIVA, CANCELADA, INUTILIZADA
)
