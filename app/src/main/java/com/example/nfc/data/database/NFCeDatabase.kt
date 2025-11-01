package com.example.nfc.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.nfc.data.model.NFCe
import com.example.nfc.data.model.ItemNFCe

@Database(
    entities = [NFCe::class, ItemNFCe::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NFCeDatabase : RoomDatabase() {
    
    abstract fun nfceDao(): NFCeDao
    
    companion object {
        @Volatile
        private var INSTANCE: NFCeDatabase? = null
        
        fun getDatabase(context: Context): NFCeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NFCeDatabase::class.java,
                    "nfce_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
