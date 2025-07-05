package com.joshua.inkscape.data.model

data class Product(
    val id: String? = null,
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val category: String = "",
    val status: String = "",
    val description: String = "",
    val lowStockThreshold: Int = 0,
    val imageUrl: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) 