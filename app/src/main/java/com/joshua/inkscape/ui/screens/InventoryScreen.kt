package com.joshua.inkscape.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joshua.inkscape.navigation.Screen
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.viewmodels.ProductViewModel
import com.joshua.inkscape.viewmodels.ProductViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory())
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedFilter by remember { mutableStateOf("All") }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Get unique categories for filtering
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }
    
    // Filter products based on selected filter
    val filteredProducts = remember(products, selectedFilter) {
        if (selectedFilter == "All") {
            products
        } else {
            products.filter { it.category == selectedFilter }
        }
    }
    
    // Statistics
    val totalProducts = filteredProducts.size
    val lowStockProducts = filteredProducts.count { it.quantity < 5 }
    val outOfStockProducts = filteredProducts.count { it.quantity == 0 }
    val totalValue = filteredProducts.sumOf { it.price * it.quantity }

    if (showFilterDialog) {
        FilterDialog(
            categories = categories,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProduct.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product")
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
                    Text(
                        text = "Inventory Management",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Monitor your stock levels and manage inventory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quick Stats Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            QuickStatCard(
                                title = "Total Products",
                                value = totalProducts.toString(),
                                icon = Icons.Filled.Info,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item {
                            QuickStatCard(
                                title = "Low Stock",
                                value = lowStockProducts.toString(),
                                icon = Icons.Filled.Warning,
                                color = Color(0xFFFF9800)
                            )
                        }
                        item {
                            QuickStatCard(
                                title = "Out of Stock",
                                value = outOfStockProducts.toString(),
                                icon = Icons.Filled.Warning,
                                color = Color(0xFFE53935)
                            )
                        }
                        item {
                            QuickStatCard(
                                title = "Total Value",
                                value = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(totalValue)}",
                                icon = Icons.Filled.Info,
                                color = Color(0xFF4CAF50)
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
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = { Text("Search products...") },
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
                        
                        FilledIconButton(
                            onClick = { navController.navigate(Screen.ManageCategories.route) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Categories")
                        }
                    }
                    
                    // Active Filter Indicator
                    if (selectedFilter != "All") {
                        Spacer(modifier = Modifier.height(8.dp))
                        FilterChip(
                            onClick = { selectedFilter = "All" },
                            label = { Text("Category: $selectedFilter") },
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
            
            // Products List
            if (products.isEmpty() && searchQuery.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading products...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try adjusting your search or filter",
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
                    items(filteredProducts, key = { it.id ?: "" }) { product ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteProductWithUndo(product)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "${product.name} deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                    }
                                    return@rememberSwipeToDismissBoxState true
                                }
                                false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromEndToStart = true,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                        Color.Red.copy(alpha = 0.8f)
                                    } else {
                                        Color.Transparent
                                    }, label = ""
                                )
                                val scale by animateFloatAsState(
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                                    label = ""
                                )

                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Icon",
                                        modifier = Modifier.scale(scale),
                                        tint = Color.White
                                    )
                                }
                            }
                        ) {
                            ModernProductCard(
                                product = product,
                                onClick = {
                                    if (product.id != null) {
                                        navController.navigate(Screen.EditProduct.createRoute(product.id))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    ElevatedCard(
        modifier = Modifier.width(140.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ModernProductCard(product: Product, onClick: () -> Unit) {
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
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Stock Status Indicator
                StockStatusIndicator(
                    quantity = product.quantity,
                    lowStockThreshold = product.lowStockThreshold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Product Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Price Column
                Column {
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Quantity Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${product.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            product.quantity == 0 -> Color(0xFFE53935)
                            product.quantity <= product.lowStockThreshold -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
                
                // Total Value Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(product.price * product.quantity)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Status Bar
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    product.quantity == 0 -> Color(0xFFE53935).copy(alpha = 0.1f)
                    product.quantity <= product.lowStockThreshold -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    else -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when {
                            product.quantity == 0 -> Icons.Filled.Warning
                            product.quantity <= product.lowStockThreshold -> Icons.Filled.Warning
                            else -> Icons.Filled.Info
                        },
                        contentDescription = null,
                        tint = when {
                            product.quantity == 0 -> Color(0xFFE53935)
                            product.quantity <= product.lowStockThreshold -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when {
                            product.quantity == 0 -> "Out of Stock - Needs immediate attention"
                            product.quantity <= product.lowStockThreshold -> "Low Stock - Consider restocking"
                            else -> "In Stock - Good availability"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            product.quantity == 0 -> Color(0xFFE53935)
                            product.quantity <= product.lowStockThreshold -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StockStatusIndicator(quantity: Int, lowStockThreshold: Int) {
    val (color, text) = when {
        quantity == 0 -> Color(0xFFE53935) to "OUT"
        quantity <= lowStockThreshold -> Color(0xFFFF9800) to "LOW"
        else -> Color(0xFF4CAF50) to "OK"
    }
    
    Surface(
        modifier = Modifier.size(48.dp),
        color = color,
        shape = CircleShape
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    categories: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Category") },
        text = {
            LazyColumn {
                items(categories) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFilterSelected(category)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == selectedFilter,
                            onClick = {
                                onFilterSelected(category)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category,
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