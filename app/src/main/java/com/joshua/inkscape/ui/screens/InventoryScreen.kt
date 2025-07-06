package com.joshua.inkscape.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joshua.inkscape.navigation.Screen
import com.joshua.inkscape.navigation.NavigationManager
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.viewmodels.ProductViewModel
import com.joshua.inkscape.viewmodels.ProductViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory())
) {
    // Clear any existing unsaved changes callback when entering inventory
    val navigationManager = remember { NavigationManager() }
    
    DisposableEffect(Unit) {
        navigationManager.setNavController(navController)
        navigationManager.clearCallback() // Clear any existing callbacks
        onDispose {
            navigationManager.clearCallback()
        }
    }
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    
    // Multi-select state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedProducts by remember { mutableStateOf(setOf<String>()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All Categories") }

    // Filter products
    val filteredProducts = remember(products, searchQuery, selectedFilter) {
        products.filter { product ->
            val matchesSearch = if (searchQuery.isBlank()) true 
            else product.name.contains(searchQuery, ignoreCase = true) ||
                 product.category.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = if (selectedFilter == "All Categories") true
            else product.category == selectedFilter
            
            matchesSearch && matchesCategory
        }
    }

    val categories = remember(products) {
        listOf("All Categories") + products.map { it.category }.distinct().sorted()
    }

    // Calculate stats
    val totalProducts = filteredProducts.size
    val lowStockProducts = filteredProducts.count { it.quantity <= it.lowStockThreshold }
    val outOfStockProducts = filteredProducts.count { it.quantity == 0 }
    val totalValue = filteredProducts.sumOf { it.price }

    // Exit selection mode when no products selected
    LaunchedEffect(selectedProducts.size) {
        if (selectedProducts.isEmpty() && isSelectionMode) {
            isSelectionMode = false
        }
    }

    // Filter Dialog
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
        topBar = {
                         if (isSelectionMode) {
                // Selection Mode Top Bar
                    TopAppBar(
                        title = { 
                            Text("${selectedProducts.size} selected")
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSelectionMode = false
                                selectedProducts = setOf()
                            }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Cancel Selection")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    if (selectedProducts.size < filteredProducts.size) {
                                        selectedProducts = filteredProducts.mapNotNull { it.id }.toSet()
                                    } else {
                                        selectedProducts = setOf()
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = "Select All")
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val productsToDelete = products.filter { it.id in selectedProducts }
                                        productsToDelete.forEach { viewModel.deleteProductWithUndo(it) }
                                        
                                        val result = snackbarHostState.showSnackbar(
                                            message = "${productsToDelete.size} products deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                        
                                        selectedProducts = setOf()
                                        isSelectionMode = false
                                    }
                                },
                                enabled = selectedProducts.isNotEmpty()
                            ) {
                                Icon(
                                    Icons.Filled.Delete, 
                                    contentDescription = "Delete Selected",
                                    tint = if (selectedProducts.isNotEmpty()) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
            } else {
                // Normal Mode Top Bar
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                "Inventory Management",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Manage your product inventory",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddProduct.route) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(paddingValues)
        ) {
            // Statistics Dashboard
            StatsCards(
                totalProducts = totalProducts,
                lowStockProducts = lowStockProducts,
                outOfStockProducts = outOfStockProducts,
                totalValue = totalValue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search and Filter Section
            SearchAndFilterSection(
                searchQuery = searchQuery,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                selectedFilter = selectedFilter,
                onFilterClick = { showFilterDialog = true },
                onCategoriesClick = { navController.navigate(Screen.ManageCategories.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Products List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredProducts.isEmpty()) {
                EmptyStateCard()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredProducts,
                        key = { it.id ?: "" }
                    ) { product ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically() + scaleOut()
                        ) {
                            EnhancedProductItem(
                                product = product,
                                isSelected = product.id in selectedProducts,
                                isSelectionMode = isSelectionMode,
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedProducts = setOf(product.id ?: "")
                                    }
                                },
                                onSelectionChange = { isSelected ->
                                    if (isSelected) {
                                        selectedProducts = selectedProducts + (product.id ?: "")
                                    } else {
                                        selectedProducts = selectedProducts - (product.id ?: "")
                                    }
                                },
                                onClick = {
                                    if (isSelectionMode) {
                                        val isSelected = product.id in selectedProducts
                                        if (isSelected) {
                                            selectedProducts = selectedProducts - (product.id ?: "")
                                        } else {
                                            selectedProducts = selectedProducts + (product.id ?: "")
                                        }
                                    } else {
                                        if (product.id != null) {
                                            navController.navigate(Screen.EditProduct.createRoute(product.id))
                                        }
                                    }
                                },
                                onSwipeToDelete = {
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
                                }
                            )
                        }
                    }
                    
                    // Bottom padding for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCards(
    totalProducts: Int,
    lowStockProducts: Int,
    outOfStockProducts: Int,
    totalValue: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Total Products",
            value = totalProducts.toString(),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Low Stock",
            value = lowStockProducts.toString(),
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Out of Stock",
            value = outOfStockProducts.toString(),
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Total Value",
            value = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(totalValue)}",
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedFilter: String,
    onFilterClick: () -> Unit,
    onCategoriesClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text("Search products...") },
                placeholder = { Text("Search by name or category") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter Button
                FilledTonalButton(
                    onClick = onFilterClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Filter",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Filter")
                }
                
                // Categories Button
                OutlinedButton(
                    onClick = onCategoriesClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = "Categories",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Categories")
                }
            }
            
            // Active Filter Display
            if (selectedFilter != "All Categories") {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Filtered by: $selectedFilter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProductItem(
    product: Product,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongPress: () -> Unit,
    onSelectionChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onSwipeToDelete: () -> Unit
) {
    // Simplified card without swipe-to-dismiss to avoid experimental APIs
    // Swipe functionality is replaced with long-press multi-select
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .then(
                    if (isSelected) {
                        Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 6.dp else 2.dp
            ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Checkbox (visible in selection mode)
                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onSelectionChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Product Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Category: ${product.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(product.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Stock: ${product.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                product.quantity == 0 -> Color(0xFFF44336)
                                product.quantity <= product.lowStockThreshold -> Color(0xFFFF9800)
                                else -> Color(0xFF4CAF50)
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status Indicator
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            product.quantity == 0 -> Color(0xFFF44336).copy(alpha = 0.1f)
                            product.quantity <= product.lowStockThreshold -> Color(0xFFFF9800).copy(alpha = 0.1f)
                            else -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        }
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = when {
                            product.quantity == 0 -> "Out of Stock"
                            product.quantity <= product.lowStockThreshold -> "Low Stock"
                            else -> "In Stock"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            product.quantity == 0 -> Color(0xFFF44336)
                            product.quantity <= product.lowStockThreshold -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No products found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Try adjusting your search or filter criteria",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FilterDialog(
    categories: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Filter by Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close")
            }
        }
    )
} 