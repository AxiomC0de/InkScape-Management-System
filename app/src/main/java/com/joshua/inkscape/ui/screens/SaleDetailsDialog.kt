package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.joshua.inkscape.data.model.Product
import com.joshua.inkscape.data.model.Sale
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun SaleDetailsDialog(
    sale: Sale,
    products: List<Product>,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onUpdatePayment: (String, Double) -> Unit,
    onMarkAsPaid: (String) -> Unit
) {
    var newAmountPaid by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Sticky Header
                StickyDialogHeader(onDismiss = onDismiss)
                
                // Scrollable Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // Sale Info Section
                    item {
                        EnhancedSectionCard(
                            title = "Sale Information",
                            subtitle = "Basic sale details and customer information"
                        ) {
                            SaleInfoGrid(sale)
                        }
                    }

                    // Products Used Section
                    item {
                        EnhancedSectionCard(
                            title = "Products Used",
                            subtitle = "Items and quantities for this sale"
                        ) {
                            ProductsUsedTable(sale, products)
                        }
                    }

                    // Payment Summary Section
                    item {
                        EnhancedSectionCard(
                            title = "Payment Summary",
                            subtitle = "Financial breakdown and payment details"
                        ) {
                            PaymentSummary(sale)
                        }
                    }

                    // Payment Status Section
                    item {
                        EnhancedSectionCard(
                            title = "Payment Status",
                            subtitle = "Current payment state and total amount"
                        ) {
                            PaymentStatusSection(sale)
                        }
                    }

                    // Update Payment Section (only for unpaid sales)
                    if (sale.paymentStatus.equals("unpaid", ignoreCase = true)) {
                        item {
                            EnhancedSectionCard(
                                title = "Update Payment",
                                subtitle = "Process additional payment for this sale",
                                isHighlighted = true
                            ) {
                                UpdatePaymentSection(
                                    sale = sale, 
                                    newAmountPaid = newAmountPaid, 
                                    onAmountChange = { newAmountPaid = it }
                                ) {
                                    val amount = newAmountPaid.toDoubleOrNull()
                                    if (amount != null && amount > 0) {
                                        onUpdatePayment(sale.id!!, amount)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Sticky Footer with Action Buttons
                StickyDialogFooter(
                    sale = sale,
                    onDismiss = onDismiss,
                    onDelete = { onDelete(sale.id!!) },
                    onMarkAsPaid = { onMarkAsPaid(sale.id!!) }
                )
            }
        }
    }
}

@Composable
private fun StickyDialogHeader(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sale Details",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                )
                Text(
                    text = "Complete transaction information",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
            FilledTonalIconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun StickyDialogFooter(
    sale: Sale,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsPaid: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button (Left side)
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = "Delete Sale",
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Action Buttons (Right side)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (sale.paymentStatus.equals("unpaid", true)) {
                        Button(
                            onClick = onMarkAsPaid,
                            modifier = Modifier.height(44.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Text(
                                text = "Mark as Paid",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedSectionCard(
    title: String,
    subtitle: String,
    isHighlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 6.dp else 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section Header
            Column(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isHighlighted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            content()
        }
    }
}

@Composable
private fun PaymentStatusSection(sale: Sale) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaymentStatusChip(sale.paymentStatus)
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Grand Total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatCurrency(sale.amountPaid),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SaleInfoGrid(sale: Sale) {
    val formattedDate = try {
        ZonedDateTime.parse(sale.date)
            .format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault()))
    } catch (e: Exception) {
        "Invalid Date"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(
                label = "Sale ID",
                value = sale.id ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                label = "Date & Time",
                value = formattedDate,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(
                label = "Service Type",
                value = sale.serviceName,
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                label = "Category",
                value = sale.category,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        
        InfoItem(
            label = "Customer Name",
            value = sale.customerName,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = textAlign,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProductsUsedTable(sale: Sale, products: List<Product>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Enhanced Table Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PRODUCT ID",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "QUANTITY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Table Body
            if (sale.productsUsed.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No products were used",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "This sale didn't include any physical products",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Column {
                    sale.productsUsed.forEachIndexed { index, saleProduct ->
                        val product = products.find { it.id == saleProduct.productId }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (index % 2 == 0) 
                                        Color.Transparent
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product?.id ?: saleProduct.productId,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = saleProduct.quantityUsed.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSummary(sale: Sale) {
    val balanceDue = sale.totalAmount - sale.amountPaid
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentRow(
                label = "Base Amount",
                amount = sale.totalAmount,
                isTotal = false
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
            
            PaymentRow(
                label = "Total Amount",
                amount = sale.totalAmount,
                isTotal = true
            )
            
            if (sale.paymentStatus.equals("unpaid", ignoreCase = true)) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
                
                PaymentRow(
                    label = "Amount Paid",
                    amount = sale.amountPaid,
                    textColor = MaterialTheme.colorScheme.secondary,
                    isTotal = false
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    thickness = 2.dp
                )
                
                PaymentRow(
                    label = "Balance Due",
                    amount = balanceDue,
                    textColor = MaterialTheme.colorScheme.error,
                    isTotal = true,
                    isHighlighted = true
                )
            }
        }
    }
}

@Composable
private fun PaymentRow(
    label: String,
    amount: Double,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    isTotal: Boolean = false,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlighted) 
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        .padding(8.dp)
                else 
                    Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
        Text(
            text = formatCurrency(amount),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun PaymentStatusChip(status: String) {
    val isPaid = status.equals("paid", ignoreCase = true)
    
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isPaid) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.error
                    )
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isPaid)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer,
            labelColor = if (isPaid)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onErrorContainer,
            leadingIconContentColor = if (isPaid)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        ),

    )
}

@Composable
private fun UpdatePaymentSection(
    sale: Sale,
    newAmountPaid: String,
    onAmountChange: (String) -> Unit,
    onUpdate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current payment status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Paid",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(sale.amountPaid),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Balance Due",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(sale.totalAmount - sale.amountPaid),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Payment input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = newAmountPaid,
                onValueChange = onAmountChange,
                label = { 
                    Text(
                        "Enter payment amount",
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                leadingIcon = { 
                    Text(
                        "₱",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),

            )
            
            Button(
                onClick = onUpdate,
                enabled = newAmountPaid.toDoubleOrNull() != null && newAmountPaid.toDouble() > 0,
                modifier = Modifier.height(56.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Update",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return "₱${NumberFormat.getNumberInstance(Locale.US).format(amount)}"
} 