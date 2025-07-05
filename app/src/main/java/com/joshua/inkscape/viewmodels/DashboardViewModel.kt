package com.joshua.inkscape.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Sale
import com.joshua.inkscape.data.repository.ProductRepository
import com.joshua.inkscape.data.repository.SalesRepository
import com.joshua.inkscape.data.model.Activity
import com.joshua.inkscape.data.repository.ActivityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import com.joshua.inkscape.data.repository.ProductRepository
import com.joshua.inkscape.data.repository.SalesRepository
import com.joshua.inkscape.data.model.Activity
import com.joshua.inkscape.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joshua.inkscape.data.model.Product

class DashboardViewModel(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val sales: StateFlow<List<Sale>> = salesRepository.getSalesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val products: StateFlow<List<Product>> = productRepository.getProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()

    private val _totalSales = MutableStateFlow(0)
    val totalSales: StateFlow<Int> = _totalSales.asStateFlow()

    private val _lowStockProducts = MutableStateFlow<List<Product>>(emptyList())
    val lowStockProducts: StateFlow<List<Product>> = _lowStockProducts.asStateFlow()

    private val _recentActivities = MutableStateFlow<List<Activity>>(emptyList())
    val recentActivities: StateFlow<List<Activity>> = _recentActivities.asStateFlow()

    init {
        fetchTotalRevenue()
        fetchTotalSales()
        fetchLowStockProducts()
        fetchRecentActivities()
    }

    private fun fetchTotalRevenue() {
        viewModelScope.launch {
            sales.combine(products) { salesList, _ ->
                salesList.sumOf { it.totalAmount }
            }.collect { totalRevenue ->
                _totalRevenue.value = totalRevenue
            }
        }
    }

    private fun fetchTotalSales() {
        viewModelScope.launch {
            sales.combine(products) { salesList, _ ->
                salesList.size
            }.collect { totalSales ->
                _totalSales.value = totalSales
            }
        }
    }

    private fun fetchLowStockProducts() {
        viewModelScope.launch {
            productRepository.getProductsFlow().collect { products ->
                _lowStockProducts.value = products.filter { it.quantity < 5 }
            }
        }
    }

    private fun fetchRecentActivities() {
        viewModelScope.launch {
            activityRepository.getRecentActivities().collect { activities ->
                _recentActivities.value = activities
            }
        }
    }

    val unpaidAmount: StateFlow<Double> = sales.combine(products) { salesList, _ ->
        salesList.filter { it.paymentStatus.equals("unpaid", ignoreCase = true) }
            .sumOf { it.totalAmount - it.amountPaid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categorySales: StateFlow<Map<String, Double>> = sales.combine(products) { salesList, productList ->
        salesList
            .flatMap { sale ->
                sale.productsUsed.map { saleProduct ->
                    val product = productList.find { it.id == saleProduct.productId }
                    val category = product?.category ?: "Unknown"
                    val saleValue = (product?.price ?: 0.0) * saleProduct.quantityUsed
                    category to saleValue
                }
            }
            .groupBy { it.first }
            .mapValues { entry -> entry.value.sumOf { it.second } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


    val salesTrend: StateFlow<Map<ZonedDateTime, Double>> = sales.combine(products) { salesList, _ ->
        salesList
            .filter {
                try {
                    val saleDate = ZonedDateTime.parse(it.date)
                    saleDate.isAfter(ZonedDateTime.now().minusDays(7))
                } catch (e: Exception) {
                    false
                }
            }
            .groupBy { ZonedDateTime.parse(it.date).truncatedTo(ChronoUnit.DAYS) }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}

class DashboardViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                salesRepository = SalesRepository(),
                productRepository = ProductRepository(),
                activityRepository = ActivityRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 