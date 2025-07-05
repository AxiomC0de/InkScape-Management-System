package com.joshua.inkscape.data.model

import com.google.firebase.database.PropertyName

data class Sale(
    val id: String? = null,
    val amountPaid: Double = 0.0,
    val category: String = "",
    val customerName: String = "",
    val date: String = "",
    val notes: String = "",
    val paymentDate: String = "",
    val paymentStatus: String = "",
    @get:PropertyName("productsUsed") @set:PropertyName("productsUsed") var productsUsed: List<SaleProduct> = emptyList(),
    val serviceName: String = "",
    val totalAmount: Double = 0.0
) 