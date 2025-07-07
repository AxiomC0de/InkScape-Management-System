package com.joshua.inkscape.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.joshua.inkscape.data.model.Sale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

class SalesRepository {
    private val database = Firebase.database
    private val salesRef = database.getReference("sales")

    fun getSalesFlow(): Flow<List<Sale>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val sales = snapshot.children.mapNotNull { dataSnapshot ->
                        try {
                            dataSnapshot.getValue(Sale::class.java)
                        } catch (e: Exception) {
                            Log.e("SalesRepository", "Error deserializing sale: ${dataSnapshot.key}", e)
                            // Try to create a sale object manually if automatic deserialization fails
                            try {
                                createSaleFromSnapshot(dataSnapshot)
                            } catch (manualException: Exception) {
                                Log.e("SalesRepository", "Manual deserialization also failed for: ${dataSnapshot.key}", manualException)
                                null
                            }
                        }
                    }
                    trySend(sales)
                } catch (e: Exception) {
                    Log.e("SalesRepository", "Error processing sales data", e)
                    trySend(emptyList()) // Send empty list instead of crashing
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SalesRepository", "Database error: ${error.message}")
                close(error.toException())
            }
        }
        salesRef.addValueEventListener(listener)
        awaitClose { salesRef.removeEventListener(listener) }
    }
    
    private fun createSaleFromSnapshot(snapshot: DataSnapshot): Sale? {
        try {
            val data = snapshot.value as? Map<String, Any> ?: return null
            
            return Sale(
                id = data["id"] as? String,
                amountPaid = (data["amountPaid"] as? Number)?.toDouble() ?: 0.0,
                category = data["category"] as? String ?: "",
                customerName = data["customerName"] as? String ?: "",
                date = data["date"] as? String ?: "",
                notes = data["notes"] as? String ?: "",
                paymentDateRaw = data["paymentDate"], // Keep as Any to handle both String and Long
                paymentStatus = data["paymentStatus"] as? String ?: "",
                productsUsed = emptyList(), // For now, skip complex nested objects
                serviceName = data["serviceName"] as? String ?: "",
                totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0
            )
        } catch (e: Exception) {
            Log.e("SalesRepository", "Error creating sale from snapshot", e)
            return null
        }
    }

    suspend fun deleteSale(saleId: String) {
        try {
            salesRef.child(saleId).removeValue().await()
        } catch (e: Exception) {
            Log.e("SalesRepository", "Error deleting sale: $saleId", e)
            throw e
        }
    }

    suspend fun updatePayment(saleId: String, newAmount: Double) {
        try {
            // In a real app, you might want to fetch the current sale object first
            // to ensure you're not overwriting other changes.
            // For simplicity, we'll just update the amountPaid.
            // You would also want to update payment status if amountPaid >= totalAmount
            salesRef.child(saleId).child("amountPaid").setValue(newAmount).await()
        } catch (e: Exception) {
            Log.e("SalesRepository", "Error updating payment for sale: $saleId", e)
            throw e
        }
    }

    suspend fun markAsPaid(saleId: String, totalAmount: Double) {
        try {
            val updates = mapOf(
                "paymentStatus" to "paid",
                "amountPaid" to totalAmount,
                "paymentDate" to System.currentTimeMillis() // Use current timestamp instead of ServerValue.TIMESTAMP
            )
            salesRef.child(saleId).updateChildren(updates).await()
        } catch (e: Exception) {
            Log.e("SalesRepository", "Error marking sale as paid: $saleId", e)
            throw e
        }
    }

    suspend fun deleteAllSales() {
        try {
            salesRef.removeValue().await()
        } catch (e: Exception) {
            Log.e("SalesRepository", "Error deleting all sales", e)
            throw e
        }
    }
} 