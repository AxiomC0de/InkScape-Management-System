package com.joshua.inkscape.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joshua.inkscape.data.repository.ProductRepository
import com.joshua.inkscape.data.repository.SalesRepository
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Sale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalesViewModel(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales = _sales.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredSales = combine(sales, searchQuery) { sales, query ->
        if (query.isBlank()) {
            sales
        } else {
            sales.filter { it.serviceName.contains(query, ignoreCase = true) || it.customerName.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchSales()
        fetchProducts()
    }

    private fun fetchSales() {
        salesRepository.getSalesFlow()
            .onEach { _sales.value = it }
            .launchIn(viewModelScope)
    }

    private fun fetchProducts() {
        productRepository.getProductsFlow()
            .onEach { _products.value = it }
            .launchIn(viewModelScope)
    }

    fun deleteSale(saleId: String) {
        viewModelScope.launch {
            salesRepository.deleteSale(saleId)
            // The flow will automatically update the list
        }
    }

    fun updatePayment(saleId: String, newAmount: Double) {
        viewModelScope.launch {
            salesRepository.updatePayment(saleId, newAmount)
        }
    }

    fun markAsPaid(saleId: String) {
        viewModelScope.launch {
            val sale = _sales.value.find { it.id == saleId }
            if (sale != null) {
                salesRepository.markAsPaid(saleId, sale.totalAmount)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteAllSales() {
        viewModelScope.launch {
            salesRepository.deleteAllSales()
        }
    }
}

class SalesViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            return SalesViewModel(SalesRepository(), ProductRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 