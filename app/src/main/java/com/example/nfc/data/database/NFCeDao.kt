package com.example.nfc.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.nfc.data.model.NFCe
import com.example.nfc.data.model.ItemNFCe
import com.example.nfc.data.model.GastoMensal
import com.example.nfc.data.model.GastoPorCategoria
import java.util.Date

@Dao
interface NFCeDao {
    
    @Query("SELECT * FROM nfce_table ORDER BY dataEmissao DESC")
    fun getAllNFCe(): LiveData<List<NFCe>>
    
    @Query("SELECT * FROM nfce_table WHERE id = :id")
    suspend fun getNFCeById(id: Long): NFCe?
    
    @Query("SELECT * FROM nfce_table WHERE chaveAcesso = :chaveAcesso")
    suspend fun getNFCeByChave(chaveAcesso: String): NFCe?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNFCe(nfce: NFCe): Long
    
    @Update
    suspend fun updateNFCe(nfce: NFCe)
    
    @Delete
    suspend fun deleteNFCe(nfce: NFCe)
    
    @Query("SELECT * FROM item_nfce_table WHERE nfceId = :nfceId")
    suspend fun getItensByNFCeId(nfceId: Long): List<ItemNFCe>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItens(itens: List<ItemNFCe>)
    
    @Query("""
        SELECT 
            strftime('%m', datetime(dataEmissao/1000, 'unixepoch')) as mes,
            strftime('%Y', datetime(dataEmissao/1000, 'unixepoch')) as ano,
            SUM(valorTotal) as valorTotal,
            COUNT(*) as quantidadeNotas
        FROM nfce_table 
        WHERE datetime(dataEmissao/1000, 'unixepoch') >= datetime('now', '-12 months')
        GROUP BY strftime('%Y-%m', datetime(dataEmissao/1000, 'unixepoch'))
        ORDER BY ano DESC, mes DESC
    """)
    suspend fun getGastosPorMes(): List<GastoMensal>
    
    @Query("""
        SELECT SUM(valorTotal) as total 
        FROM nfce_table 
        WHERE datetime(dataEmissao/1000, 'unixepoch') >= datetime('now', 'start of month')
    """)
    suspend fun getTotalGastosMesAtual(): Double?
    
    @Query("""
        SELECT SUM(valorTotal) as total 
        FROM nfce_table 
        WHERE datetime(dataEmissao/1000, 'unixepoch') >= datetime('now', 'start of year')
    """)
    suspend fun getTotalGastosAnoAtual(): Double?
    
    @Query("""
        SELECT AVG(valorTotal) as media 
        FROM nfce_table 
        WHERE datetime(dataEmissao/1000, 'unixepoch') >= datetime('now', '-30 days')
    """)
    suspend fun getMediaGastosDiarios(): Double?
    
    @Query("SELECT MAX(valorTotal) FROM nfce_table")
    suspend fun getMaiorGasto(): Double?
    
    @Query("SELECT MIN(valorTotal) FROM nfce_table WHERE valorTotal > 0")
    suspend fun getMenorGasto(): Double?
    
    @Query("""
        SELECT COUNT(*) 
        FROM nfce_table 
        WHERE datetime(dataEmissao/1000, 'unixepoch') >= datetime('now', 'start of month')
    """)
    suspend fun getQuantidadeNotasMesAtual(): Int
}
