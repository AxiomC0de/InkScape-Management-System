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

class SalesRepository {
    private val database = Firebase.database
    private val salesRef = database.getReference("sales")

    fun getSalesFlow(): Flow<List<Sale>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sales = snapshot.children.mapNotNull { it.getValue(Sale::class.java) }
                trySend(sales)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        salesRef.addValueEventListener(listener)
        awaitClose { salesRef.removeEventListener(listener) }
    }

    suspend fun deleteSale(saleId: String) {
        salesRef.child(saleId).removeValue().await()
    }

    suspend fun updatePayment(saleId: String, newAmount: Double) {
        // In a real app, you might want to fetch the current sale object first
        // to ensure you're not overwriting other changes.
        // For simplicity, we'll just update the amountPaid.
        // You would also want to update payment status if amountPaid >= totalAmount
        salesRef.child(saleId).child("amountPaid").setValue(newAmount).await()
    }

    suspend fun markAsPaid(saleId: String, totalAmount: Double) {
        val updates = mapOf(
            "paymentStatus" to "paid",
            "amountPaid" to totalAmount,
            "paymentDate" to com.google.firebase.database.ServerValue.TIMESTAMP
        )
        salesRef.child(saleId).updateChildren(updates).await()
    }

    suspend fun deleteAllSales() {
        salesRef.removeValue().await()
    }
} 