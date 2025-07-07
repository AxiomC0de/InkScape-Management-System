package com.joshua.inkscape.data.model

import com.google.firebase.database.PropertyName
import com.google.firebase.database.Exclude

data class Sale(
    val id: String? = null,
    val amountPaid: Double = 0.0,
    val category: String = "",
    val customerName: String = "",
    val date: String = "",
    val notes: String = "",
    
    // Handle both String and Long for paymentDate to fix Firebase deserialization
    @get:PropertyName("paymentDate") @set:PropertyName("paymentDate") 
    var paymentDateRaw: Any? = null,
    
    val paymentStatus: String = "",
    @get:PropertyName("productsUsed") @set:PropertyName("productsUsed") var productsUsed: List<SaleProduct> = emptyList(),
    val serviceName: String = "",
    val totalAmount: Double = 0.0
) {
    // Helper property to get paymentDate as String regardless of stored type
    @get:Exclude
    val paymentDate: String
        get() = when (paymentDateRaw) {
            is String -> paymentDateRaw as String
            is Long -> {
                // Convert Firebase timestamp to readable date string
                val date = java.util.Date(paymentDateRaw as Long)
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date)
            }
            else -> ""
        }
    
    // Helper function to set paymentDate as String
    @Exclude
    fun setPaymentDate(dateString: String) {
        paymentDateRaw = dateString
    }
    
    // Helper function to set paymentDate as timestamp
    @Exclude
    fun setPaymentTimestamp(timestamp: Long) {
        paymentDateRaw = timestamp
    }
} 