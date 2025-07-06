package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joshua.inkscape.navigation.Screen
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Sale
import com.joshua.inkscape.viewmodels.SalesViewModel
import com.joshua.inkscape.viewmodels.SalesViewModelFactory
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    navController: NavController,
    salesViewModel: SalesViewModel = viewModel(factory = SalesViewModelFactory())
) {
    val sales by salesViewModel.filteredSales.collectAsState()
    val products by salesViewModel.products.collectAsState()
    val searchQuery by salesViewModel.searchQuery.collectAsState()
    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Calculate analytics
    val analytics = remember(sales) {
        derivedStateOf {
            calculateSalesAnalytics(sales, products)
        }
    }.value
    
    // Get payment methods for filtering
    val paymentMethods = remember(sales) {
        listOf("All") + sales.map { it.paymentStatus.replaceFirstChar { it.uppercase() } }.distinct().sorted()
    }
    
    // Filter sales based on selected filter
    val filteredSales = remember(sales, selectedFilter) {
        if (selectedFilter == "All") {
            sales
        } else {
            sales.filter { it.paymentStatus.replaceFirstChar { it.uppercase() } == selectedFilter }
        }
    }

    if (selectedSale != null) {
        SaleDetailsDialog(
            sale = selectedSale!!,
            products = products,
            onDismiss = { selectedSale = null },
            onDelete = {
                salesViewModel.deleteSale(it)
                selectedSale = null
            },
            onUpdatePayment = { saleId, amount ->
                salesViewModel.updatePayment(saleId, amount)
                selectedSale = null
            },
            onMarkAsPaid = {
                salesViewModel.markAsPaid(it)
                selectedSale = null
            }
        )
    }

    if (showDeleteConfirmation) {
        DeleteAllSalesConfirmationDialog(
            onConfirm = {
                salesViewModel.deleteAllSales()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }

    if (showFilterDialog) {
        SalesFilterDialog(
            paymentMethods = paymentMethods,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateSale.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Sale")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title and Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sales Management",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "Track and manage your sales performance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Delete All Button
                        FilledIconButton(
                            onClick = { showDeleteConfirmation = true },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFFE53935).copy(alpha = 0.1f),
                                contentColor = Color(0xFFE53935)
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete All Sales")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Analytics Cards
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            AnalyticsCard(
                                title = "Total Sales",
                                value = analytics.totalSales.toString(),
                                icon = Icons.Filled.ShoppingCart,
                                color = MaterialTheme.colorScheme.primary,
                                subtitle = "Transactions"
                            )
                        }
                        item {
                            AnalyticsCard(
                                title = "Total Revenue",
                                value = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(analytics.totalRevenue)}",
                                icon = Icons.Filled.Info,
                                color = Color(0xFF4CAF50),
                                subtitle = "All time"
                            )
                        }
                        item {
                            AnalyticsCard(
                                title = "Avg Sale",
                                value = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(analytics.averageSale)}",
                                icon = Icons.Filled.Info,
                                color = Color(0xFF2196F3),
                                subtitle = "Per transaction"
                            )
                        }
                        item {
                            AnalyticsCard(
                                title = "Today's Sales",
                                value = analytics.todaySales.toString(),
                                icon = Icons.Filled.Warning,
                                color = Color(0xFFFF9800),
                                subtitle = "${analytics.todayRevenue.let { "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(it)}" }}"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search and Filter Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { salesViewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search sales...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        FilledIconButton(
                            onClick = { showFilterDialog = true },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = "Filter")
                        }
                    }
                    
                    // Active Filter Indicator
                    if (selectedFilter != "All") {
                        Spacer(modifier = Modifier.height(8.dp))
                        FilterChip(
                            onClick = { selectedFilter = "All" },
                            label = { Text("Payment: $selectedFilter") },
                            selected = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Clear filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
            
            // Sales List
            if (filteredSales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (searchQuery.isNotEmpty() || selectedFilter != "All") Icons.Filled.Search else Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedFilter != "All") "No sales found" else "No sales yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedFilter != "All") "Try adjusting your search or filter" else "Click the + button to create your first sale",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSales.sortedByDescending { it.date }) { sale ->
                        ModernSaleCard(
                            sale = sale,
                            products = products,
                            onClick = { selectedSale = sale }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    subtitle: String
) {
    ElevatedCard(
        modifier = Modifier.width(160.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModernSaleCard(
    sale: Sale,
    products: List<Product>,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sale #${sale.id?.takeLast(8) ?: "Unknown"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val formattedDate = try {
                        val zonedDateTime = ZonedDateTime.parse(sale.date)
                        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm", Locale.getDefault())
                        zonedDateTime.format(formatter)
                    } catch (e: Exception) {
                        sale.date
                    }
                    
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Payment Status Badge
                PaymentStatusBadge(paymentStatus = sale.paymentStatus)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Products Summary
            if (sale.productsUsed.isNotEmpty()) {
                Text(
                    text = "Items Sold",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                sale.productsUsed.take(3).forEach { item ->
                    val product = products.find { p -> p.id == item.productId }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${product?.name ?: "Unknown Product"} (x${item.quantityUsed})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(product?.price?.times(item.quantityUsed) ?: 0.0)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (sale.productsUsed.size > 3) {
                    Text(
                        text = "+${sale.productsUsed.size - 3} more items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(sale.totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentStatusBadge(paymentStatus: String) {
    val (color, backgroundColor) = when (paymentStatus.lowercase()) {
        "paid" -> Color(0xFF4CAF50) to Color(0xFF4CAF50).copy(alpha = 0.1f)
        "pending" -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.1f)
        "cancelled" -> Color(0xFFE53935) to Color(0xFFE53935).copy(alpha = 0.1f)
        else -> Color(0xFF2196F3) to Color(0xFF2196F3).copy(alpha = 0.1f)
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = paymentStatus.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SalesFilterDialog(
    paymentMethods: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Payment Status") },
        text = {
            LazyColumn {
                items(paymentMethods) { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFilterSelected(method)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = method == selectedFilter,
                            onClick = {
                                onFilterSelected(method)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = method,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun DeleteAllSalesConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete All Sales?") },
        text = { Text("Are you sure you want to delete all sales? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to calculate analytics
private fun calculateSalesAnalytics(sales: List<Sale>, products: List<Product>): SalesAnalytics {
    val totalSales = sales.size
    val totalRevenue = sales.sumOf { sale ->
        // Calculate actual product revenue + service fees
        val productRevenue = sale.productsUsed.sumOf { item ->
            val product = products.find { it.id == item.productId }
            (product?.price ?: 0.0) * item.quantityUsed
        }
        productRevenue + sale.totalAmount // Add service fees
    }
    val averageSale = if (totalSales > 0) totalRevenue / totalSales else 0.0
    
    // Today's sales
    val today = LocalDateTime.now()
    val todaySales = sales.count { sale ->
        try {
            val saleDate = ZonedDateTime.parse(sale.date).toLocalDateTime()
            saleDate.toLocalDate() == today.toLocalDate()
        } catch (e: Exception) {
            false
        }
    }
    
    val todayRevenue = sales.filter { sale ->
        try {
            val saleDate = ZonedDateTime.parse(sale.date).toLocalDateTime()
            saleDate.toLocalDate() == today.toLocalDate()
        } catch (e: Exception) {
            false
        }
    }.sumOf { sale ->
        val productRevenue = sale.productsUsed.sumOf { item ->
            val product = products.find { it.id == item.productId }
            (product?.price ?: 0.0) * item.quantityUsed
        }
        productRevenue + sale.totalAmount
    }
    
    return SalesAnalytics(
        totalSales = totalSales,
        totalRevenue = totalRevenue,
        averageSale = averageSale,
        todaySales = todaySales,
        todayRevenue = todayRevenue
    )
}

data class SalesAnalytics(
    val totalSales: Int,
    val totalRevenue: Double,
    val averageSale: Double,
    val todaySales: Int,
    val todayRevenue: Double
) 