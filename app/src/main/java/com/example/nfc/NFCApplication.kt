package com.example.nfc

import android.app.Application
import com.example.nfc.data.database.NFCeDatabase

class NFCApplication : Application() {
    
    val database by lazy { NFCeDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
    }
}
