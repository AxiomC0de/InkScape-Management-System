package com.joshua.inkscape.viewmodels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshua.inkscape.data.repository.ProductRepository
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Sale
import com.joshua.inkscape.data.model.SaleProduct
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Fee(val name: String, val amount: Double)

class CreateSaleViewModel : ViewModel() {

    // Input States
    val serviceName = mutableStateOf("")
    val category = mutableStateOf("")
    val customerName = mutableStateOf("")
    val baseAmount = mutableStateOf("")
    val paymentStatus = mutableStateOf("Paid")
    val amountPaid = mutableStateOf("")
    val notes = mutableStateOf("")
    val productsUsed = mutableStateListOf<SaleProduct>()
    val additionalFees = mutableStateListOf<Fee>()

    // Error states
    val serviceNameError = mutableStateOf(false)
    val categoryError = mutableStateOf(false)
    val baseAmountError = mutableStateOf(false)
    val productsUsedError = mutableStateOf(false)

    val hasUnsavedChanges: Boolean
        get() = serviceName.value.isNotEmpty() ||
                category.value.isNotEmpty() ||
                customerName.value.isNotEmpty() ||
                baseAmount.value.isNotEmpty() ||
                notes.value.isNotEmpty() ||
                productsUsed.isNotEmpty() ||
                additionalFees.isNotEmpty()

    // Dropdown and Dialog states
    val isCategoryDropdownExpanded = mutableStateOf(false)
    val isPaymentStatusDropdownExpanded = mutableStateOf(false)
    val showAddProductDialog = mutableStateOf(false)
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery = _productSearchQuery.asStateFlow()

    // Data from repositories
    private val productRepository = ProductRepository()
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = combine(_allProducts, _productSearchQuery) { products, query ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories = _categories.asStateFlow()

    // Calculated States
    val totalAmount: Double
        @Composable
        get() = (baseAmount.value.toDoubleOrNull() ?: 0.0) + additionalFees.sumOf { it.amount }

    val remainingAmount: Double
        @Composable
        get() = totalAmount - (amountPaid.value.toDoubleOrNull() ?: 0.0)


    private val database = Firebase.database
    private val categoriesRef = database.getReference("categories")
    private val salesRef = database.getReference("sales")
    private val productsRef = database.getReference("products")

    init {
        fetchCategories()
        fetchProducts()
    }

    private fun fetchProducts() {
        productRepository.getProductsFlow()
            .onEach { _allProducts.value = it }
            .launchIn(viewModelScope)
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val snapshot = categoriesRef.get().await()
                _categories.value = snapshot.children.mapNotNull { it.getValue(String::class.java) }
            } catch (e: Exception) {
                Log.e("CreateSaleViewModel", "Failed to fetch categories", e)
            }
        }
    }

    fun onProductSearchQueryChanged(query: String) {
        _productSearchQuery.value = query
    }

    fun addProductToSale(productId: String, quantity: Int) {
        // Prevent adding more than available stock
        val product = _allProducts.value.find { it.id == productId }
        if (product != null && quantity <= product.quantity) {
            productsUsed.add(SaleProduct(productId, quantity))
        }
    }

    fun addFee(name: String, amount: Double) {
        additionalFees.add(Fee(name, amount))
    }

    fun createSale(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val baseAmountDouble = baseAmount.value.toDoubleOrNull()

            // Validation
            serviceNameError.value = serviceName.value.isBlank()
            categoryError.value = category.value.isBlank()
            baseAmountError.value = baseAmountDouble == null
            productsUsedError.value = productsUsed.isEmpty()

            if (serviceNameError.value || categoryError.value || baseAmountError.value || productsUsedError.value) {
                onError("Please fill all required fields (*).")
                return@launch
            }

            try {
                val total = baseAmountDouble!! + additionalFees.sumOf { it.amount }
                val paid = if (paymentStatus.value == "Paid") total else amountPaid.value.toDoubleOrNull() ?: 0.0
                val newSaleRef = salesRef.push()

                val sale = Sale(
                    id = newSaleRef.key,
                    serviceName = serviceName.value,
                    category = category.value,
                    customerName = customerName.value,
                    date = com.google.firebase.database.ServerValue.TIMESTAMP.toString(),
                    paymentStatus = paymentStatus.value.lowercase(),
                    productsUsed = productsUsed.toList(),
                    totalAmount = total,
                    amountPaid = paid,
                    notes = notes.value
                )

                newSaleRef.setValue(sale).await()

                // Manually trigger product stock update (as done in backend)
                for (item in productsUsed) {
                    val productRef = productsRef.child(item.productId)
                    productRef.get().await().getValue(Product::class.java)?.let { product ->
                        val newQuantity = product.quantity - item.quantityUsed
                        productRef.child("quantity").setValue(newQuantity).await()
                        productRef.child("status").setValue(if (newQuantity > 0) "available" else "unavailable").await()
                    }
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("CreateSaleViewModel", "Failed to create sale", e)
                onError(e.message ?: "An error occurred")
            }
        }
    }
} 