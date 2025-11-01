package com.example.nfc.ui.invoice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.nfc.data.database.NFCeDatabase
import com.example.nfc.data.model.NFCe
import com.example.nfc.data.repository.NFCeRepository

class InvoiceListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NFCeRepository
    
    val allNFCe: LiveData<List<NFCe>>
    
    init {
        val database = NFCeDatabase.getDatabase(application)
        repository = NFCeRepository(database.nfceDao())
        allNFCe = repository.getAllNFCe()
    }
}
