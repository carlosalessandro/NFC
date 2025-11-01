package com.example.nfc.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "item_nfce_table",
    foreignKeys = [
        ForeignKey(
            entity = NFCe::class,
            parentColumns = ["id"],
            childColumns = ["nfceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemNFCe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nfceId: Long,
    val codigo: String,
    val descricao: String,
    val quantidade: Double,
    val unidade: String,
    val valorUnitario: Double,
    val valorTotal: Double,
    val ncm: String? = null,
    val cfop: String? = null
)
