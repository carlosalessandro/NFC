package com.example.nfc.data.model

data class GastoMensal(
    val mes: String,
    val ano: Int,
    val valorTotal: Double,
    val quantidadeNotas: Int
)

data class GastoPorCategoria(
    val categoria: String,
    val valorTotal: Double,
    val percentual: Double
)

data class EstatisticasGastos(
    val totalMes: Double,
    val totalAno: Double,
    val mediaGastosDiarios: Double,
    val maiorGasto: Double,
    val menorGasto: Double,
    val quantidadeNotasMes: Int
)
