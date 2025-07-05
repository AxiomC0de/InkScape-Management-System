package com.joshua.inkscape.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshua.inkscape.data.model.Product
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class AddProductViewModel : ViewModel() {

    var productName by mutableStateOf("")
    var description by mutableStateOf("")
    var price by mutableStateOf("")
    var quantity by mutableStateOf("")
    var lowStockThreshold by mutableStateOf("")
    var category by mutableStateOf("")
    var isCategoryDropdownExpanded by mutableStateOf(false)

    // Error states
    val productNameError = mutableStateOf(false)
    val priceError = mutableStateOf(false)
    val quantityError = mutableStateOf(false)
    val lowStockThresholdError = mutableStateOf(false)
    val categoryError = mutableStateOf(false)

    val hasUnsavedChanges: Boolean
        get() = productName.isNotEmpty() ||
                description.isNotEmpty() ||
                price.isNotEmpty() ||
                quantity.isNotEmpty() ||
                lowStockThreshold.isNotEmpty() ||
                category.isNotEmpty()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories = _categories.asStateFlow()

    private val database = Firebase.database
    private val productsRef = database.getReference("products")
    private val categoriesRef = database.getReference("categories")

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val snapshot = categoriesRef.get().await()
                _categories.value = snapshot.getValue<List<String>>()?.filterNotNull() ?: emptyList()
            } catch (e: Exception) {
                Log.e("AddProductViewModel", "Failed to fetch categories", e)
            }
        }
    }

    fun addProduct(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val priceDouble = price.toDoubleOrNull()
            val quantityInt = quantity.toIntOrNull()
            val lowStockInt = lowStockThreshold.toIntOrNull()

            // Validation
            productNameError.value = productName.isBlank()
            priceError.value = priceDouble == null
            quantityError.value = quantityInt == null
            lowStockThresholdError.value = lowStockInt == null
            categoryError.value = category.isBlank()

            if (productNameError.value || priceError.value || quantityError.value || lowStockThresholdError.value || categoryError.value) {
                onError("Please fill all required fields correctly.")
                return@launch
            }

            try {
                val newProductRef = productsRef.push()
                val product = Product(
                    id = newProductRef.key,
                    name = productName,
                    description = description,
                    price = priceDouble!!,
                    quantity = quantityInt!!,
                    lowStockThreshold = lowStockInt!!,
                    category = category,
                    status = if (quantityInt > 0) "available" else "unavailable",
                    imageUrl = "" // Image URL is not handled yet
                )
                newProductRef.setValue(product).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "An unknown error occurred.")
            }
        }
    }
} 