package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.navigation.NavigationManager
import com.joshua.inkscape.viewmodels.CreateSaleViewModel
import com.joshua.inkscape.viewmodels.Fee
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSaleScreen(
    viewModel: CreateSaleViewModel = viewModel(),
    navigationManager: NavigationManager
) {
    val paymentStatus by viewModel.paymentStatus
    val categories by viewModel.categories.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val allProducts by viewModel.filteredProducts.collectAsState()

    DisposableEffect(Unit) {
        navigationManager.setHasUnsavedChangesCallback { viewModel.hasUnsavedChanges }
        onDispose {
            navigationManager.clearCallback()
        }
    }

    var selectedProductForQuantity by remember { mutableStateOf<Product?>(null) }
    var showAddFeeDialog by remember { mutableStateOf(false) }

    if (viewModel.showAddProductDialog.value) {
        ModernAddProductDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddProductDialog.value = false },
            onProductSelected = { product ->
                viewModel.showAddProductDialog.value = false
                selectedProductForQuantity = product
            }
        )
    }

    if (showAddFeeDialog) {
        ModernAddFeeDialog(
            onDismiss = { showAddFeeDialog = false },
            onConfirm = { name, amount ->
                viewModel.addFee(name, amount)
                showAddFeeDialog = false
            }
        )
    }

    if (selectedProductForQuantity != null) {
        ModernQuantityDialog(
            product = selectedProductForQuantity!!,
            onDismiss = { selectedProductForQuantity = null },
            onConfirm = { productId, quantity ->
                viewModel.addProductToSale(productId, quantity)
                selectedProductForQuantity = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Create New Sale",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Build your service transaction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigationManager.handleBackPress() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Progress Indicator
                ProgressIndicatorSection()
                
                Spacer(modifier = Modifier.height(16.dp))

                // Service Information Section
                ModernSectionCard(
                    title = "Service Information",
                    description = "Basic details about your service",
                    icon = Icons.Filled.LocalOffer,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernTextField(
                                value = viewModel.serviceName.value,
                                onValueChange = {
                                    viewModel.serviceName.value = it
                                    viewModel.serviceNameError.value = false
                                },
                                label = "Service Name",
                                placeholder = "e.g., Hair Cut & Style",
                                isError = viewModel.serviceNameError.value,
                                modifier = Modifier.weight(1f),
                                required = true
                            )
                            
                            ExposedDropdownMenuBox(
                                expanded = viewModel.isCategoryDropdownExpanded.value,
                                onExpandedChange = { viewModel.isCategoryDropdownExpanded.value = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                ModernTextField(
                                    value = viewModel.category.value,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Category",
                                    placeholder = "Select category",
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = viewModel.isCategoryDropdownExpanded.value
                                        ) 
                                    },
                                    modifier = Modifier.menuAnchor(),
                                    isError = viewModel.categoryError.value,
                                    required = true
                                )
                                ExposedDropdownMenu(
                                    expanded = viewModel.isCategoryDropdownExpanded.value,
                                    onDismissRequest = { viewModel.isCategoryDropdownExpanded.value = false }
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category) },
                                            onClick = {
                                                viewModel.category.value = category
                                                viewModel.isCategoryDropdownExpanded.value = false
                                                viewModel.categoryError.value = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer Information Section
                ModernSectionCard(
                    title = "Customer Information",
                    description = "Optional customer details",
                    icon = Icons.Filled.Person,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ModernTextField(
                            value = viewModel.customerName.value,
                            onValueChange = { viewModel.customerName.value = it },
                            label = "Customer Name",
                            placeholder = "Enter customer name (optional)",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pricing & Payment Section
                ModernSectionCard(
                    title = "Pricing & Payment",
                    description = "Set service cost and payment status",
                    icon = Icons.Filled.LocalOffer,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernTextField(
                                value = viewModel.baseAmount.value,
                                onValueChange = {
                                    viewModel.baseAmount.value = it
                                    viewModel.baseAmountError.value = false
                                },
                                label = "Service Amount",
                                placeholder = "0.00",
                                prefixText = "₱",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                isError = viewModel.baseAmountError.value,
                                required = true
                            )
                            
                            ExposedDropdownMenuBox(
                                expanded = viewModel.isPaymentStatusDropdownExpanded.value,
                                onExpandedChange = { viewModel.isPaymentStatusDropdownExpanded.value = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                ModernTextField(
                                    value = paymentStatus,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Payment Status",
                                    placeholder = "Select status",
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = viewModel.isPaymentStatusDropdownExpanded.value
                                        ) 
                                    },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = viewModel.isPaymentStatusDropdownExpanded.value,
                                    onDismissRequest = { viewModel.isPaymentStatusDropdownExpanded.value = false }
                                ) {
                                    listOf("Paid", "Unpaid").forEach { status ->
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        if (status == "Paid") Icons.Filled.CheckCircle else Icons.Filled.Info,
                                                        contentDescription = null,
                                                        tint = if (status == "Paid") Color(0xFF4CAF50) else Color(0xFFFF9800),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(status)
                                                }
                                            },
                                            onClick = {
                                                viewModel.paymentStatus.value = status
                                                viewModel.isPaymentStatusDropdownExpanded.value = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (paymentStatus == "Unpaid") {
                            ModernTextField(
                                value = viewModel.amountPaid.value,
                                onValueChange = { viewModel.amountPaid.value = it },
                                label = "Amount Paid",
                                placeholder = "0.00",
                                prefixText = "₱",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Remaining Balance: ₱${NumberFormat.getNumberInstance(Locale.US).format(viewModel.remainingAmount)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Products Section
                ModernSectionCard(
                    title = "Products Used",
                    description = "Add products used in this service",
                    icon = Icons.Filled.ShoppingCart,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    actionButton = {
                        FilledTonalButton(
                            onClick = { viewModel.showAddProductDialog.value = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = "Add Product", 
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Add Product")
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.productsUsedError.value) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "At least one product must be added to the sale.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else if (viewModel.productsUsed.isEmpty()) {
                            EmptyStateCard(
                                icon = Icons.Filled.ShoppingCart,
                                message = "No products added yet",
                                description = "Tap 'Add Product' to include products used in this service"
                            )
                        } else {
                            viewModel.productsUsed.forEach { saleProduct ->
                                val product = allProducts.find { it.id == saleProduct.productId }
                                if (product != null) {
                                    ModernProductItem(
                                        productName = product.name,
                                        quantityUsed = saleProduct.quantityUsed,
                                        onRemove = { viewModel.productsUsed.remove(saleProduct) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Additional Fees Section
                ModernSectionCard(
                    title = "Additional Fees",
                    description = "Optional service charges",
                    icon = Icons.Filled.Add,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    actionButton = {
                        OutlinedButton(
                            onClick = { showAddFeeDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                contentDescription = "Add Fee", 
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Add Fee")
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.additionalFees.isEmpty()) {
                            EmptyStateCard(
                                icon = Icons.Filled.Add,
                                message = "No additional fees",
                                description = "Add extra charges like delivery or service fees"
                            )
                        } else {
                            viewModel.additionalFees.forEach { fee ->
                                ModernFeeItem(
                                    fee = fee,
                                    onRemove = { viewModel.additionalFees.remove(fee) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Section
                ModernSectionCard(
                    title = "Additional Notes",
                    description = "Optional notes about this service",
                    icon = Icons.Filled.Info,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ModernTextField(
                            value = viewModel.notes.value,
                            onValueChange = { viewModel.notes.value = it },
                            label = "Notes",
                            placeholder = "Add any special notes or instructions...",
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp)) // Space for bottom bar
            }

            // Modern Floating Action Bar
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₱${NumberFormat.getNumberInstance(Locale.US).format(viewModel.totalAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Button(
                        onClick = { 
                            viewModel.createSale(
                                onSuccess = { navigationManager.handleBackPress() },
                                onError = {}
                            ) 
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Save Sale",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressIndicatorSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.LocalOffer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Creating New Sale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Fill in the details to complete the transaction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModernSectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                actionButton?.invoke()
            }
            Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)
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
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    required: Boolean = false,
    readOnly: Boolean = false,
    prefixText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                if (required) "$label *" else label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        prefix = prefixText?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        trailingIcon = trailingIcon,
        modifier = modifier,
        isError = isError,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernProductItem(
    productName: String,
    quantityUsed: Int,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Quantity: $quantityUsed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Product")
            }
        }
    }
}

@Composable
fun ModernFeeItem(fee: Fee, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = fee.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₱${NumberFormat.getNumberInstance(Locale.US).format(fee.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Fee")
            }
        }
    }
}

// Modern Dialog Components
@Composable
fun ModernAddProductDialog(
    viewModel: CreateSaleViewModel,
    onDismiss: () -> Unit,
    onProductSelected: (Product) -> Unit
) {
    val products by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.productSearchQuery.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Select Product",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Choose products used in this service",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onProductSearchQueryChanged(it) },
                    label = { Text("Search products") },
                    placeholder = { Text("Type product name...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductSelected(product) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Category: ${product.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (product.quantity > 10) 
                                            Color(0xFF4CAF50).copy(alpha = 0.1f) 
                                        else 
                                            Color(0xFFFF9800).copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Stock: ${product.quantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (product.quantity > 10) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernQuantityDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, quantity: Int) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { 
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Set Quantity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Available stock: ${product.quantity} units",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        showError = (it.toIntOrNull() ?: 0) > product.quantity
                    },
                    label = { Text("Quantity to use") },
                    placeholder = { Text("Enter quantity") },
                    isError = showError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Cannot exceed available stock",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantityInt = quantity.toIntOrNull()
                    if (quantityInt != null && quantityInt > 0 && !showError) {
                        onConfirm(product.id!!, quantityInt)
                    }
                },
                enabled = quantity.isNotBlank() && !showError && (quantity.toIntOrNull() ?: 0) > 0,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add to Sale")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ModernAddFeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Add Additional Fee",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Fee Description") },
                    placeholder = { Text("e.g., Delivery fee, Service charge") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("₱") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (name.isNotBlank() && amountDouble != null) {
                        onConfirm(name, amountDouble)
                    }
                },
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Fee")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
} 