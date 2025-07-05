package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                title = { Text("Create New Sale") },
                navigationIcon = {
                    IconButton(onClick = { navigationManager.handleBackPress() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount: ₱${NumberFormat.getNumberInstance(Locale.US).format(viewModel.totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = { viewModel.createSale(
                        onSuccess = { navigationManager.handleBackPress() },
                        onError = {}
                    ) }) {
                        Text("Save Sale")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Service Name and Category
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.serviceName.value,
                    onValueChange = {
                        viewModel.serviceName.value = it
                        viewModel.serviceNameError.value = false
                    },
                    label = { Text("Service Name*") },
                    modifier = Modifier.weight(1f),
                    isError = viewModel.serviceNameError.value
                )
                Spacer(Modifier.width(8.dp))
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
                        isError = viewModel.categoryError.value
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
            Spacer(Modifier.height(16.dp))

            // Customer Name
            OutlinedTextField(
                value = viewModel.customerName.value,
                onValueChange = { viewModel.customerName.value = it },
                label = { Text("Customer Name") },
                placeholder = { Text("Optional customer name")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Base Amount and Payment Status
            Row(Modifier.fillMaxWidth()) {
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
                    isError = viewModel.baseAmountError.value
                )
                Spacer(Modifier.width(8.dp))
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
                        modifier = Modifier.menuAnchor()
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
            Spacer(Modifier.height(16.dp))

            // Amount Paid (conditional)
            if (paymentStatus == "Unpaid") {
                OutlinedTextField(
                    value = viewModel.amountPaid.value,
                    onValueChange = { viewModel.amountPaid.value = it },
                    label = { Text("Amount Paid") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Remaining: ₱${NumberFormat.getNumberInstance(Locale.US).format(viewModel.remainingAmount)}",
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(Modifier.height(16.dp))
            }

            // Products Used
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Products Used*", fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.showAddProductDialog.value = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Product")
                }
            }
            if (viewModel.productsUsedError.value) {
                Text(
                    "At least one product must be added to the sale.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            } else if(viewModel.productsUsed.isEmpty()){
                Text("No products have been added to this sale.", modifier = Modifier.padding(start = 16.dp))
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
            Spacer(Modifier.height(16.dp))

            // Additional Fees
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Additional Fees", fontWeight = FontWeight.Bold)
                TextButton(onClick = { showAddFeeDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Fee", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Fee")
                }
            }
            if(viewModel.additionalFees.isEmpty()){
                Text("No additional fees added.", modifier = Modifier.padding(start = 16.dp))
            } else {
                viewModel.additionalFees.forEach { fee ->
                    FeeItem(
                        fee = fee,
                        onRemove = { viewModel.additionalFees.remove(fee) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = viewModel.notes.value,
                onValueChange = { viewModel.notes.value = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
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
        Card(modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Product", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onProductSearchQueryChanged(it) },
                    label = { Text("Search products...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(products) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductSelected(product) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(product.name)
                            Text("Stock: ${product.quantity}")
                        }
                    }
                }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$productName (x$quantityUsed)")
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Product")
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${fee.name}: ₱${fee.amount}")
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove Fee")
        }
    }
} 