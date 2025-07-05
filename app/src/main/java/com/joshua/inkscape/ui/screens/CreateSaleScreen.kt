package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
        AddProductToSaleDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddProductDialog.value = false },
            onProductSelected = { product ->
                viewModel.showAddProductDialog.value = false
                selectedProductForQuantity = product
            }
        )
    }

    if (showAddFeeDialog) {
        AddFeeDialog(
            onDismiss = { showAddFeeDialog = false },
            onConfirm = { name, amount ->
                viewModel.addFee(name, amount)
                showAddFeeDialog = false
            }
        )
    }

    if (selectedProductForQuantity != null) {
        EnterQuantityDialog(
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
                    Text(
                        "Create New Sale",
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Save Sale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Service Details Section
            item {
                SectionCard(
                    title = "Service Details",
                    icon = Icons.Default.Info
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.serviceName.value,
                                onValueChange = {
                                    viewModel.serviceName.value = it
                                    viewModel.serviceNameError.value = false
                                },
                                label = { Text("Service Name*") },
                                modifier = Modifier.weight(1f),
                                isError = viewModel.serviceNameError.value,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenuBox(
                                expanded = viewModel.isCategoryDropdownExpanded.value,
                                onExpandedChange = { viewModel.isCategoryDropdownExpanded.value = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = viewModel.category.value,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category*") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.isCategoryDropdownExpanded.value) },
                                    modifier = Modifier.menuAnchor(),
                                    isError = viewModel.categoryError.value,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
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
            }

            // Customer Information Section
            item {
                SectionCard(
                    title = "Customer Information",
                    icon = Icons.Default.Person
                ) {
                    OutlinedTextField(
                        value = viewModel.customerName.value,
                        onValueChange = { viewModel.customerName.value = it },
                        label = { Text("Customer Name (Optional)") },
                        placeholder = { Text("Enter customer name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Payment Information Section
            item {
                SectionCard(
                    title = "Payment Information",
                    icon = Icons.Default.MonetizationOn
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.baseAmount.value,
                                onValueChange = {
                                    viewModel.baseAmount.value = it
                                    viewModel.baseAmountError.value = false
                                },
                                label = { Text("Base Amount*") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                isError = viewModel.baseAmountError.value,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenuBox(
                                expanded = viewModel.isPaymentStatusDropdownExpanded.value,
                                onExpandedChange = { viewModel.isPaymentStatusDropdownExpanded.value = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = paymentStatus,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Payment Status") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.isPaymentStatusDropdownExpanded.value) },
                                    modifier = Modifier.menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = viewModel.isPaymentStatusDropdownExpanded.value,
                                    onDismissRequest = { viewModel.isPaymentStatusDropdownExpanded.value = false }
                                ) {
                                    listOf("Paid", "Unpaid").forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
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
                            OutlinedTextField(
                                value = viewModel.amountPaid.value,
                                onValueChange = { viewModel.amountPaid.value = it },
                                label = { Text("Amount Paid") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Remaining: ₱${NumberFormat.getNumberInstance(Locale.US).format(viewModel.remainingAmount)}",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Products Used Section
            item {
                SectionCard(
                    title = "Products Used",
                    icon = Icons.Default.ShoppingCart,
                    actionButton = {
                        Button(
                            onClick = { viewModel.showAddProductDialog.value = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Product")
                        }
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (viewModel.productsUsedError.value) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "At least one product must be added to the sale.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else if (viewModel.productsUsed.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "No products have been added to this sale.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            viewModel.productsUsed.forEach { saleProduct ->
                                val product = allProducts.find { it.id == saleProduct.productId }
                                if (product != null) {
                                    ProductUsedItem(
                                        productName = product.name,
                                        quantityUsed = saleProduct.quantityUsed,
                                        onRemove = {
                                            viewModel.productsUsed.remove(saleProduct)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Additional Fees Section
            item {
                SectionCard(
                    title = "Additional Fees",
                    icon = Icons.Default.MonetizationOn,
                    actionButton = {
                        OutlinedButton(
                            onClick = { showAddFeeDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Fee")
                        }
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (viewModel.additionalFees.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "No additional fees added.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            viewModel.additionalFees.forEach { fee ->
                                FeeItem(
                                    fee = fee,
                                    onRemove = { viewModel.additionalFees.remove(fee) }
                                )
                            }
                        }
                    }
                }
            }

            // Notes Section
            item {
                SectionCard(
                    title = "Notes",
                    icon = Icons.Default.Info
                ) {
                    OutlinedTextField(
                        value = viewModel.notes.value,
                        onValueChange = { viewModel.notes.value = it },
                        label = { Text("Additional Notes") },
                        placeholder = { Text("Enter any additional information...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Add some bottom padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionButton: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                actionButton?.invoke()
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun AddProductToSaleDialog(
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
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Select Product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onProductSearchQueryChanged(it) },
                    label = { Text("Search products...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductSelectionItem(
                            product = product,
                            onClick = { onProductSelected(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductSelectionItem(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₱${product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Surface(
                color = if (product.quantity < 5) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Stock: ${product.quantity}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (product.quantity < 5) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun EnterQuantityDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, quantity: Int) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Quantity for ${product.name}") },
        text = {
            Column {
                Text("Available stock: ${product.quantity}")
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        showError = (it.toIntOrNull() ?: 0) > product.quantity
                    },
                    label = { Text("Quantity") },
                    isError = showError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (showError) {
                    Text("Cannot exceed available stock", color = MaterialTheme.colorScheme.error)
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
                enabled = quantity.isNotBlank() && !showError && (quantity.toIntOrNull() ?: 0) > 0
            ) {
                Text("Add")
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
fun ProductUsedItem(
    productName: String,
    quantityUsed: Int,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(6.dp)
                    )
                }
                Column {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Quantity: $quantityUsed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove Product",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddFeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Additional Fee") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Fee Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null
            ) {
                Text("Add")
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
fun FeeItem(fee: Fee, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(6.dp)
                    )
                }
                Column {
                    Text(
                        text = fee.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "₱${NumberFormat.getNumberInstance(Locale.US).format(fee.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove Fee",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 