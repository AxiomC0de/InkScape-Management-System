package com.joshua.inkscape.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joshua.inkscape.data.repository.ProductRepository
import com.joshua.inkscape.data.repository.ActivityRepository
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Activity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _pendingDeletionProduct = MutableStateFlow<Product?>(null)
    private var deletionJob: Job? = null
    private val activityRepository = ActivityRepository()

    val products: StateFlow<List<Product>> =
        combine(_allProducts, _pendingDeletionProduct, searchQuery) { products, pendingDeletion, query ->
            val listWithoutPending = if (pendingDeletion != null) {
                products.filter { it.id != pendingDeletion.id }
            } else {
                products
            }
            if (query.isBlank()) {
                listWithoutPending
            } else {
                listWithoutPending.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        repository.getProductsFlow()
            .onEach { products ->
                _allProducts.value = products
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteProductWithUndo(product: Product) {
        deletionJob?.cancel() // Cancel any previous deletion job
        _pendingDeletionProduct.value = product
        deletionJob = viewModelScope.launch {
            delay(5000) // Snackbar duration
            product.id?.let { 
                repository.deleteProduct(it)
                
                // Log activity
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val activity = Activity(
                    details = "Deleted product: ${product.name} (Category: ${product.category})",
                    timestamp = timestamp,
                    type = "product_deleted",
                    productName = product.name,
                    user = "System"
                )
                activityRepository.addActivity(activity)
            }
            _pendingDeletionProduct.value = null
        }
    }

    fun undoDelete() {
        deletionJob?.cancel()
        _pendingDeletionProduct.value = null
    }
}

class ProductViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            val repository = ProductRepository()
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 