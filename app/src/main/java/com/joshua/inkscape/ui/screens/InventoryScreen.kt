package com.joshua.inkscape.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.joshua.inkscape.navigation.Screen
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.viewmodels.ProductViewModel
import com.joshua.inkscape.viewmodels.ProductViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddProduct.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    label = { Text("Search Products") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { navController.navigate(Screen.ManageCategories.route) }) {
                    Text("Categories")
                }
            }

            if (products.isEmpty() && searchQuery.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products, key = { it.id ?: "" }) { product ->
                        SwipeableProductItem(
                            product = product,
                            onDelete = {
                                productToDelete = product
                                showDeleteDialog = true
                            },
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
        
        // Delete confirmation dialog
        if (showDeleteDialog && productToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    productToDelete = null
                },
                title = { Text("Delete Product") },
                text = { 
                    Text("Are you sure you want to delete '${productToDelete?.name}'? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            productToDelete?.let { product ->
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
                            showDeleteDialog = false
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = false
                            productToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SwipeableProductItem(
    product: Product,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isSwiped by remember { mutableStateOf(false) }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "offsetX"
    )
    
    val deleteButtonScale by animateFloatAsState(
        targetValue = if (isSwiped) 1f else 0.8f,
        label = "deleteButtonScale"
    )
    
    val deleteButtonColor by animateColorAsState(
        targetValue = if (isSwiped) Color.Red.copy(alpha = 0.9f) else Color.Red.copy(alpha = 0.7f),
        label = "deleteButtonColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Delete button background
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(80.dp)
                .background(deleteButtonColor)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.scale(deleteButtonScale),
                tint = Color.White
            )
        }
        
        // Product card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffsetX.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (abs(offsetX) > 100f) {
                                isSwiped = true
                                onDelete()
                            } else {
                                offsetX = 0f
                                isSwiped = false
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = offsetX + dragAmount.x
                        offsetX = newOffset.coerceAtMost(0f).coerceAtLeast(-200f)
                        isSwiped = abs(offsetX) > 50f
                    }
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Name: ${product.name}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Category: ${product.category}")
                Text(text = "Price: ₱${product.price}")
                Text(text = "Quantity: ${product.quantity}")
                Text(
                    text = "Status: ${product.status.replaceFirstChar { it.uppercase() }}",
                    color = if (product.status.equals("available", true)) Color(0xFF388E3C) else Color.Red
                )
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${product.name}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Category: ${product.category}")
            Text(text = "Price: ₱${product.price}")
            Text(text = "Quantity: ${product.quantity}")
            Text(
                text = "Status: ${product.status.replaceFirstChar { it.uppercase() }}",
                color = if (product.status.equals("available", true)) Color(0xFF388E3C) else Color.Red
            )
        }
    }
} 