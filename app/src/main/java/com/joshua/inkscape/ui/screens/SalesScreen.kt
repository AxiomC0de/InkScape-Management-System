package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joshua.inkscape.navigation.Screen
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Sale
import com.joshua.inkscape.viewmodels.SalesViewModel
import com.joshua.inkscape.viewmodels.SalesViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf

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
                // Note: The dialog will not auto-refresh the sale object after this.
                // A more robust solution might involve observing the specific sale for changes.
                // For now, we just dismiss.
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { salesViewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search Sales...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete All Sales")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateSale.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Sale")
            }
        }
    ) { paddingValues ->
        if (sales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No sales yet. Click the + button to add a new sale.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sales.sortedByDescending { it.date }) { sale ->
                    SaleItem(sale = sale, products = products, onClick = { selectedSale = sale })
                }
            }
        }
    }
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

@Composable
fun SaleItem(sale: Sale, products: List<Product>, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val formattedDate = try {
                val zonedDateTime = ZonedDateTime.parse(sale.date)
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
                zonedDateTime.format(formatter)
            } catch (e: Exception) {
                sale.date // fallback to raw date if parsing fails
            }

            Text(text = "Sale ID: ${sale.id}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Date: $formattedDate", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Total Amount: $${sale.totalAmount}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Payment Method: ${sale.paymentStatus.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)

            // Display sold items
            sale.productsUsed.forEach { item ->
                val product = products.find { p -> p.id == item.productId }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${product?.name ?: "Unknown Product"} (x${item.quantityUsed})")
                    Text(text = "$${product?.price?.times(item.quantityUsed)}")
                }
            }
        }
    }
} 