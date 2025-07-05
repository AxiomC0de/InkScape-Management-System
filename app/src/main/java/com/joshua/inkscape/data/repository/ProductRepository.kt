package com.joshua.inkscape.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.joshua.inkscape.data.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val database = Firebase.database
    private val productsRef = database.getReference("products")

    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productMap = snapshot.value as? Map<String, Any>
                val products = productMap?.values?.mapNotNull {
                    val map = it as? Map<String, Any>
                    if (map != null) {
                        val quantity = (map["quantity"] as? Number)?.toInt() ?: 0
                        Product(
                            id = map["id"] as? String,
                            name = map["name"] as? String ?: "",
                            quantity = quantity,
                            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                            category = map["category"] as? String ?: "",
                            status = if (quantity > 0) "available" else "unavailable",
                            description = map["description"] as? String ?: "",
                            lowStockThreshold = (map["lowStockThreshold"] as? Number)?.toInt() ?: 0,
                            imageUrl = map["imageUrl"] as? String ?: "",
                            createdAt = map["createdAt"] as? String ?: "",
                            updatedAt = map["updatedAt"] as? String ?: ""
                        )
                    } else {
                        null
                    }
                } ?: emptyList()
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        productsRef.addValueEventListener(listener)
        awaitClose { productsRef.removeEventListener(listener) }
    }

    suspend fun deleteProduct(productId: String) {
        productsRef.child(productId).removeValue().await()
    }
} 