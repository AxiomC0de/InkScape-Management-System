package com.joshua.inkscape.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Activity
import com.joshua.inkscape.data.repository.ActivityRepository
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.text.SimpleDateFormat
import java.util.*

class EditProductViewModel(private val productId: String) : ViewModel() {

    private val database = Firebase.database
    private val productsRef = database.getReference("products")
    private val categoriesRef = database.getReference("categories")
    private val activityRepository = ActivityRepository()

    var productName by mutableStateOf("")
    var description by mutableStateOf("")
    var price by mutableStateOf("")
    var quantity by mutableStateOf("")
    var lowStockThreshold by mutableStateOf("")
    var category by mutableStateOf("")
    var isCategoryDropdownExpanded by mutableStateOf(false)

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories = _categories.asStateFlow()

    init {
        fetchProductDetails()
        fetchCategories()
    }

    private fun fetchProductDetails() {
        viewModelScope.launch {
            try {
                val snapshot = productsRef.child(productId).get().await()
                val product = snapshot.getValue<Product>()
                if (product != null) {
                    productName = product.name
                    description = product.description
                    price = product.price.toString()
                    quantity = product.quantity.toString()
                    lowStockThreshold = product.lowStockThreshold.toString()
                    category = product.category
                }
            } catch (e: Exception) {
                Log.e("EditProductViewModel", "Failed to fetch product details.", e)
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val snapshot = categoriesRef.get().await()
                _categories.value = snapshot.getValue<List<String>>()?.filterNotNull() ?: emptyList()
            } catch (e: Exception) {
                Log.e("EditProductViewModel", "Failed to fetch categories", e)
            }
        }
    }

    fun updateProduct(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val priceDouble = price.toDoubleOrNull()
                val quantityInt = quantity.toIntOrNull()
                val lowStockInt = lowStockThreshold.toIntOrNull()

                if (productName.isBlank() || priceDouble == null || quantityInt == null || category.isBlank() || lowStockInt == null) {
                    onError("Please fill all required fields correctly.")
                    return@launch
                }

                val productUpdate = mapOf(
                    "name" to productName,
                    "description" to description,
                    "price" to priceDouble,
                    "quantity" to quantityInt,
                    "lowStockThreshold" to lowStockInt,
                    "category" to category,
                    "status" to if (quantityInt > 0) "available" else "unavailable",
                    "updatedAt" to Instant.now().toString()
                )
                productsRef.child(productId).updateChildren(productUpdate).await()

                // Log activity
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val activity = Activity(
                    details = "Updated product: $productName (Category: $category)",
                    timestamp = timestamp,
                    type = "product_updated",
                    productId = productId,
                    productName = productName,
                    user = "System"
                )
                activityRepository.addActivity(activity)

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "An unknown error occurred.")
            }
        }
    }
}

class EditProductViewModelFactory(private val productId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProductViewModel(productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 