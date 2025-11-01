package com.example.nfc.data.repository

import androidx.lifecycle.LiveData
import com.example.nfc.data.database.NFCeDao
import com.example.nfc.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NFCeRepository(private val nfceDao: NFCeDao) {
    
    fun getAllNFCe(): LiveData<List<NFCe>> = nfceDao.getAllNFCe()
    
    suspend fun insertNFCe(nfce: NFCe): Long {
        return withContext(Dispatchers.IO) {
            nfceDao.insertNFCe(nfce)
        }
    }
    
    suspend fun insertNFCeWithItens(nfce: NFCe, itens: List<ItemNFCe>): Long {
        return withContext(Dispatchers.IO) {
            val nfceId = nfceDao.insertNFCe(nfce)
            val itensWithNFCeId = itens.map { it.copy(nfceId = nfceId) }
            nfceDao.insertItens(itensWithNFCeId)
            nfceId
        }
    }
    
    suspend fun getNFCeById(id: Long): NFCe? {
        return withContext(Dispatchers.IO) {
            nfceDao.getNFCeById(id)
        }
    }
    
    suspend fun getNFCeByChave(chaveAcesso: String): NFCe? {
        return withContext(Dispatchers.IO) {
            nfceDao.getNFCeByChave(chaveAcesso)
        }
    }
    
    suspend fun getItensByNFCeId(nfceId: Long): List<ItemNFCe> {
        return withContext(Dispatchers.IO) {
            nfceDao.getItensByNFCeId(nfceId)
        }
    }
    
    suspend fun deleteNFCe(nfce: NFCe) {
        withContext(Dispatchers.IO) {
            nfceDao.deleteNFCe(nfce)
        }
    }
    
    suspend fun getGastosPorMes(): List<GastoMensal> {
        return withContext(Dispatchers.IO) {
            nfceDao.getGastosPorMes()
        }
    }
    
    suspend fun getEstatisticasGastos(): EstatisticasGastos {
        return withContext(Dispatchers.IO) {
            EstatisticasGastos(
                totalMes = nfceDao.getTotalGastosMesAtual() ?: 0.0,
                totalAno = nfceDao.getTotalGastosAnoAtual() ?: 0.0,
                mediaGastosDiarios = nfceDao.getMediaGastosDiarios() ?: 0.0,
                maiorGasto = nfceDao.getMaiorGasto() ?: 0.0,
                menorGasto = nfceDao.getMenorGasto() ?: 0.0,
                quantidadeNotasMes = nfceDao.getQuantidadeNotasMesAtual()
            )
        }
    }
}
