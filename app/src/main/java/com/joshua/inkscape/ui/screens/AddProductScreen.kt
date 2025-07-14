package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joshua.inkscape.navigation.NavigationManager
import com.joshua.inkscape.viewmodels.AddProductViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel = viewModel(),
    navigationManager: NavigationManager
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessMessage by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        navigationManager.setHasUnsavedChangesCallback { viewModel.hasUnsavedChanges }
        onDispose {
            navigationManager.clearCallback()
        }
    }

    // Show success message and navigate back after a delay
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            snackbarHostState.showSnackbar("Product has been successfully added!")
            delay(1500) // Wait 1.5 seconds for user to see the message
            navigationManager.handleBackPress()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add New Product") },
                navigationIcon = {
                    IconButton(onClick = { navigationManager.handleBackPress() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = viewModel.productName,
                onValueChange = {
                    viewModel.productName = it
                    viewModel.productNameError.value = false
                },
                label = { Text("Product Name*") },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.productNameError.value
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.price,
                    onValueChange = {
                        viewModel.price = it
                        viewModel.priceError.value = false
                    },
                    label = { Text("Price*") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = viewModel.priceError.value
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = viewModel.quantity,
                    onValueChange = {
                        viewModel.quantity = it
                        viewModel.quantityError.value = false
                    },
                    label = { Text("Quantity*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = viewModel.quantityError.value
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.lowStockThreshold,
                onValueChange = {
                    viewModel.lowStockThreshold = it
                    viewModel.lowStockThresholdError.value = false
                },
                label = { Text("Low Stock Threshold*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.lowStockThresholdError.value
            )
            Spacer(modifier = Modifier.height(16.dp))

            val categories by viewModel.categories.collectAsState()

            ExposedDropdownMenuBox(
                expanded = viewModel.isCategoryDropdownExpanded,
                onExpandedChange = { viewModel.isCategoryDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = viewModel.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category*") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = viewModel.isCategoryDropdownExpanded
                        )
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = viewModel.categoryError.value
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
            Spacer(modifier = Modifier.height(16.dp))

            Text("Product Image", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { /* TODO: Implement image picker */ }) {
                    Text("Choose file")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("No file chosen")
            }
            Spacer(modifier = Modifier.height(32.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { navigationManager.handleBackPress() },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text("Cancel")
                }
                Button(onClick = {
                    viewModel.addProduct(
                        onSuccess = {
                            showSuccessMessage = true
                        },
                        onError = { errorMessage ->
                            scope.launch {
                                snackbarHostState.showSnackbar(errorMessage)
                            }
                        }
                    )
                }) {
                    Text("Add Product")
                }
            }
        }
    }
} 