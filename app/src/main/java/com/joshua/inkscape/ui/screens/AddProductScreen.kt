package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joshua.inkscape.navigation.NavigationManager
import com.joshua.inkscape.viewmodels.AddProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel = viewModel(),
    navigationManager: NavigationManager
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        navigationManager.setHasUnsavedChangesCallback { viewModel.hasUnsavedChanges }
        onDispose {
            navigationManager.clearCallback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add New Product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navigationManager.handleBackPress() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Create Product",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add a new product to your inventory",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Basic Information Section
                ModernSectionCard(
                    title = "Basic Information",
                    description = "Essential product details",
                    icon = Icons.Filled.Info
                ) {
                    Column {
                        ModernTextField(
                            value = viewModel.productName,
                            onValueChange = {
                                viewModel.productName = it
                                viewModel.productNameError.value = false
                            },
                            label = "Product Name",
                            placeholder = "Enter product name",
                            isRequired = true,
                            isError = viewModel.productNameError.value,
                            errorMessage = if (viewModel.productNameError.value) "Product name is required" else null,
                            helperText = "Use a clear, descriptive name"
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        ModernTextField(
                            value = viewModel.description,
                            onValueChange = { viewModel.description = it },
                            label = "Description",
                            placeholder = "Describe your product (optional)",
                            minLines = 3,
                            helperText = "Provide details about features, specifications, or usage"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Pricing & Inventory Section
                ModernSectionCard(
                    title = "Pricing & Inventory",
                    description = "Set price and stock information",
                    icon = Icons.Filled.ShoppingCart
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ModernTextField(
                                value = viewModel.price,
                                onValueChange = {
                                    viewModel.price = it
                                    viewModel.priceError.value = false
                                },
                                label = "Unit Price",
                                placeholder = "0.00",
                                prefixText = "₱",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                isRequired = true,
                                isError = viewModel.priceError.value,
                                errorMessage = if (viewModel.priceError.value) "Valid price required" else null,
                                helperText = "Price per unit"
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            ModernTextField(
                                value = viewModel.quantity,
                                onValueChange = {
                                    viewModel.quantity = it
                                    viewModel.quantityError.value = false
                                },
                                label = "Stock Quantity",
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                isRequired = true,
                                isError = viewModel.quantityError.value,
                                errorMessage = if (viewModel.quantityError.value) "Quantity required" else null,
                                helperText = "Units in stock"
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        ModernTextField(
                            value = viewModel.lowStockThreshold,
                            onValueChange = {
                                viewModel.lowStockThreshold = it
                                viewModel.lowStockThresholdError.value = false
                            },
                            label = "Low Stock Alert",
                            placeholder = "5",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isRequired = true,
                            isError = viewModel.lowStockThresholdError.value,
                            errorMessage = if (viewModel.lowStockThresholdError.value) "Alert threshold required" else null,
                            helperText = "Get notified when stock falls below this level"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Category & Organization Section
                ModernSectionCard(
                    title = "Category & Organization",
                    description = "Organize your product",
                    icon = Icons.Filled.Warning
                ) {
                    Column {
                        val categories by viewModel.categories.collectAsState()
                        
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = viewModel.isCategoryDropdownExpanded,
                            onExpandedChange = { viewModel.isCategoryDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = viewModel.category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Category") },
                                placeholder = { Text("Choose product category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = viewModel.isCategoryDropdownExpanded
                                    )
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                isError = viewModel.categoryError.value,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = viewModel.isCategoryDropdownExpanded,
                                onDismissRequest = { viewModel.isCategoryDropdownExpanded = false }
                            ) {
                                categories.forEach { categoryName ->
                                    DropdownMenuItem(
                                        text = { Text(categoryName) },
                                        onClick = {
                                            viewModel.category = categoryName
                                            viewModel.isCategoryDropdownExpanded = false
                                            viewModel.categoryError.value = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (viewModel.categoryError.value) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Please select a category",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Categorize your product for better organization",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Product Image Section
                ModernSectionCard(
                    title = "Product Image",
                    description = "Add a visual representation",
                    icon = Icons.Filled.Add
                ) {
                    Column {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No image selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = { /* TODO: Implement image picker */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Image")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Recommended: Square image, at least 500x500px",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navigationManager.handleBackPress() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Button(
                        onClick = {
                            viewModel.addProduct(
                                onSuccess = { navigationManager.handleBackPress() },
                                onError = { errorMessage ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(errorMessage)
                                    }
                                }
                            )
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Add Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ModernSectionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            content()
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    prefixText: String = "",
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minLines: Int = 1,
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            prefix = if (prefixText.isNotEmpty()) { { Text(prefixText) } } else null,
            keyboardOptions = keyboardOptions,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (helperText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 