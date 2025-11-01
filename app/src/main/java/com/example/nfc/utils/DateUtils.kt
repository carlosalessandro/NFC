package com.example.nfc.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    /**
     * Converte timestamp para String formatada
     */
    fun timestampToString(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    /**
     * Converte timestamp para String apenas com data
     */
    fun timestampToDateString(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }
    
    /**
     * Converte String para timestamp
     */
    fun stringToTimestamp(dateString: String): Long {
        return try {
            dateFormat.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * Obtém timestamp atual
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * Converte Date para timestamp
     */
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
    
    /**
     * Converte timestamp para Date
     */
    fun timestampToDate(timestamp: Long): Date {
        return Date(timestamp)
    }
}
