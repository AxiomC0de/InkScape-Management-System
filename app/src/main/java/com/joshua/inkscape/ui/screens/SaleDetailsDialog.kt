package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sale Details", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Sale Info
                SaleInfoGrid(sale)
                Spacer(modifier = Modifier.height(24.dp))

                // Products Used
                Text("Products Used", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ProductsUsedTable(sale, products)
                Spacer(modifier = Modifier.height(24.dp))

                // Payment Summary
                Text("Payment Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                PaymentSummary(sale)
                Spacer(modifier = Modifier.height(24.dp))

                // Payment Status & Grand Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PaymentStatusChip(sale.paymentStatus)
                    Text(
                        text = "Grand Total (Paid): ${formatCurrency(sale.amountPaid)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Conditional UI for Unpaid Sales
                if (sale.paymentStatus.equals("unpaid", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    UpdatePaymentSection(sale = sale, newAmountPaid = newAmountPaid, onAmountChange = { newAmountPaid = it }){
                        val amount = newAmountPaid.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            onUpdatePayment(sale.id!!, amount)
                        }
                    }
                }

                // Footer Buttons
                Spacer(modifier = Modifier.height(32.dp))
                DialogButtons(sale, onDismiss, { onDelete(sale.id!!) }, { onMarkAsPaid(sale.id!!) })
            }
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = textAlign, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ProductsUsedTable(sale: Sale, products: List<Product>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Table Header
        Row(Modifier.padding(8.dp)) {
            Text("PRODUCT ID", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            Text("QUANTITY USED", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End)
        }
        Divider()
        // Table Body
        if (sale.productsUsed.isEmpty()) {
            Text("No products were used in this sale.", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
        } else {
            sale.productsUsed.forEach { saleProduct ->
                val product = products.find { it.id == saleProduct.productId }
                Row(Modifier.padding(8.dp)) {
                    Text(product?.id ?: saleProduct.productId, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(saleProduct.quantityUsed.toString(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun PaymentSummary(sale: Sale) {
    val balanceDue = sale.totalAmount - sale.amountPaid
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Base Amount:", style = MaterialTheme.typography.bodyMedium)
            Text(formatCurrency(sale.totalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Divider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Amount:", style = MaterialTheme.typography.bodyMedium)
            Text(formatCurrency(sale.totalAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        if (sale.paymentStatus.equals("unpaid", ignoreCase = true)) {
            Divider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Amount Paid:", style = MaterialTheme.typography.bodyMedium)
                Text(formatCurrency(sale.amountPaid), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Divider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Balance Due:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(formatCurrency(balanceDue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentStatusChip(status: String) {
    val isPaid = status.equals("paid", ignoreCase = true)
    val backgroundColor = if (isPaid) Color(0xFFC8E6C9) else Color(0xFFFFECB3)
    val contentColor = if (isPaid) Color(0xFF2E7D32) else Color(0xFFF57F17)

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(status.uppercase(), style = MaterialTheme.typography.labelSmall, color = contentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun UpdatePaymentSection(
    sale: Sale,
    newAmountPaid: String,
    onAmountChange: (String) -> Unit,
    onUpdate: () -> Unit
) {
    Column {
        Text("Update Payment", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Current Amount Paid: ${formatCurrency(sale.amountPaid)}")
            Text("Balance Due: ${formatCurrency(sale.totalAmount - sale.amountPaid)}")
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newAmountPaid,
                onValueChange = onAmountChange,
                label = { Text("Enter new amount paid") },
                leadingIcon = { Text("₱") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onUpdate) {
                Text("Update")
            }
        }
    }
}

@Composable
private fun DialogButtons(sale: Sale, onDismiss: () -> Unit, onDelete: () -> Unit, onMarkAsPaid: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onDelete) {
            Text("Delete Sale", color = MaterialTheme.colorScheme.error)
        }
        Row {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Close")
            }
            if (sale.paymentStatus.equals("unpaid", true)) {
                Spacer(Modifier.width(8.dp))
                Button(onClick = onMarkAsPaid) {
                    Text("Mark as Paid")
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return "₱${NumberFormat.getNumberInstance(Locale("en", "PH")).format(amount)}"
} 