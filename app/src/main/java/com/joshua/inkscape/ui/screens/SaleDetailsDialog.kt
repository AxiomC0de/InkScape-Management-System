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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Sticky Header
                StickyDialogHeader(onDismiss = onDismiss)
                
                // Scrollable Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Sale Info Section
                    item {
                        SectionCard(title = "Sale Information") {
                            SaleInfoGrid(sale)
                        }
                    }

                    // Products Used Section
                    item {
                        SectionCard(title = "Products Used") {
                            ProductsUsedTable(sale, products)
                        }
                    }

                    // Payment Summary Section
                    item {
                        SectionCard(title = "Payment Summary") {
                            PaymentSummary(sale)
                        }
                    }

                    // Payment Status Section
                    item {
                        SectionCard(title = "Payment Status") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PaymentStatusChip(sale.paymentStatus)
                                Text(
                                    text = "Grand Total: ${formatCurrency(sale.amountPaid)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Update Payment Section (only for unpaid sales)
                    if (sale.paymentStatus.equals("unpaid", ignoreCase = true)) {
                        item {
                            SectionCard(title = "Update Payment") {
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
                        Spacer(modifier = Modifier.height(16.dp))
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
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sale Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Button (Left side)
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Sale")
            }
            
            // Action Buttons (Right side)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Close")
                }
                
                if (sale.paymentStatus.equals("unpaid", true)) {
                    Button(
                        onClick = onMarkAsPaid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Mark as Paid")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(label = "Sale ID:", value = sale.id ?: "N/A", modifier = Modifier.weight(1f))
            InfoItem(label = "Date:", value = formattedDate, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(label = "Service:", value = sale.serviceName, modifier = Modifier.weight(1f))
            InfoItem(label = "Category:", value = sale.category, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(label = "Customer:", value = sale.customerName, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Column(modifier) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.SemiBold, 
            textAlign = textAlign, 
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProductsUsedTable(sale: Sale, products: List<Product>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "PRODUCT ID", 
                    modifier = Modifier.weight(1f), 
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "QUANTITY", 
                    modifier = Modifier.weight(1f), 
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Table Body
            if (sale.productsUsed.isEmpty()) {
                Text(
                    text = "No products were used in this sale.", 
                    modifier = Modifier.padding(12.dp), 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                sale.productsUsed.forEachIndexed { index, saleProduct ->
                    val product = products.find { it.id == saleProduct.productId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (index % 2 == 0) MaterialTheme.colorScheme.surface 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = product?.id ?: saleProduct.productId, 
                            modifier = Modifier.weight(1f), 
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = saleProduct.quantityUsed.toString(), 
                            modifier = Modifier.weight(1f), 
                            style = MaterialTheme.typography.bodyMedium, 
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Medium
                        )
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
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Base Amount:", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatCurrency(sale.totalAmount), 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount:", 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(sale.totalAmount), 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (sale.paymentStatus.equals("unpaid", ignoreCase = true)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Amount Paid:", 
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = formatCurrency(sale.amountPaid), 
                        style = MaterialTheme.typography.bodyLarge, 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Balance Due:", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = formatCurrency(balanceDue), 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusChip(status: String) {
    val isPaid = status.equals("paid", ignoreCase = true)
    val backgroundColor = if (isPaid) 
        MaterialTheme.colorScheme.primaryContainer
    else 
        MaterialTheme.colorScheme.errorContainer
    val contentColor = if (isPaid) 
        MaterialTheme.colorScheme.onPrimaryContainer 
    else 
        MaterialTheme.colorScheme.onErrorContainer

    AssistChip(
        onClick = { },
        label = {
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor
        )
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current Paid: ${formatCurrency(sale.amountPaid)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Balance: ${formatCurrency(sale.totalAmount - sale.amountPaid)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = newAmountPaid,
                onValueChange = onAmountChange,
                label = { Text("Enter amount paid") },
                leadingIcon = { Text("₱", style = MaterialTheme.typography.bodyLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onUpdate,
                enabled = newAmountPaid.toDoubleOrNull() != null && newAmountPaid.toDouble() > 0
            ) {
                Text("Update")
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return "₱${NumberFormat.getNumberInstance(Locale.US).format(amount)}"
} 