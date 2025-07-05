package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarStyle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.joshua.inkscape.viewmodels.DashboardViewModel
import com.joshua.inkscape.viewmodels.DashboardViewModelFactory
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.joshua.inkscape.data.model.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory())
) {
    val totalRevenue = viewModel.totalRevenue.collectAsState().value
    val totalSales = viewModel.totalSales.collectAsState().value
    val lowStockProducts = viewModel.lowStockProducts.collectAsState().value.size
    val recentActivities = viewModel.recentActivities.collectAsState().value
    val mostSoldCategory = viewModel.mostSoldCategory.collectAsState().value
    val categoryRanking = viewModel.categoryRanking.collectAsState().value
    val salesTrend = viewModel.salesTrend.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Welcome back! Here's your business overview",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Stats Cards Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernStatsCard(
                title = "Total Revenue",
                value = "S/${NumberFormat.getNumberInstance(Locale.US).format(totalRevenue)}",
                subtitle = "This month",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            ModernStatsCard(
                title = "Total Sales",
                value = totalSales.toString(),
                subtitle = "Completed",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Cards Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernStatsCard(
                title = "Low Stock Alert",
                value = lowStockProducts.toString(),
                subtitle = "Items need restocking",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            ModernStatsCard(
                title = "Top Category",
                value = mostSoldCategory.first,
                subtitle = "S/${NumberFormat.getNumberInstance(Locale.US).format(mostSoldCategory.second)}",
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sales Trend Line Chart
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales Trend",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last 7 days",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (salesTrend.isNotEmpty()) {
                    CustomLineChart(
                        modifier = Modifier.fillMaxSize(),
                        salesData = salesTrend
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No sales data available",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Ranking Section
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Category Performance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (categoryRanking.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No sales data available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryRanking.size) { index ->
                            val category = categoryRanking[index]
                            CategoryRankingItem(
                                rank = index + 1,
                                categoryName = category.first,
                                salesAmount = category.second
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity Section
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recentActivities.isEmpty()) {
                        // Add some sample activities if none exist
                        val sampleActivities = listOf(
                            Activity(
                                id = "1",
                                details = "Added new product to inventory",
                                timestamp = "2024-01-15T10:30:00Z",
                                type = "product_add",
                                user = "user1"
                            ),
                            Activity(
                                id = "2",
                                details = "Completed sale #001",
                                timestamp = "2024-01-15T09:15:00Z",
                                type = "sale_complete",
                                user = "user1"
                            ),
                            Activity(
                                id = "3",
                                details = "Updated product pricing",
                                timestamp = "2024-01-15T08:45:00Z",
                                type = "product_update",
                                user = "user1"
                            )
                        )
                        items(sampleActivities) { activity ->
                            ActivityItem(activity)
                        }
                    } else {
                        items(recentActivities) { activity ->
                            ActivityItem(activity)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ModernStatsCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.height(120.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    modifier = Modifier.size(8.dp),
                    colors = CardDefaults.cardColors(containerColor = color),
                    shape = RoundedCornerShape(4.dp)
                ) {}
            }
            
            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryRankingItem(rank: Int, categoryName: String, salesAmount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank indicator
                Card(
                    modifier = Modifier.size(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = rank.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when (rank) {
                                1, 2, 3 -> Color.Black
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = categoryName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            
            Text(
                text = "S/${NumberFormat.getNumberInstance(Locale.US).format(salesAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = activity.details,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activity.timestamp,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomLineChart(
    modifier: Modifier = Modifier,
    salesData: Map<ZonedDateTime, Double>
) {
    val sortedEntries = salesData.entries.sortedBy { it.key }
    val maxValue = sortedEntries.maxOfOrNull { it.value } ?: 0.0
    val minValue = sortedEntries.minOfOrNull { it.value } ?: 0.0
    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            if (sortedEntries.isNotEmpty()) {
                drawLineChart(
                    entries = sortedEntries,
                    maxValue = maxValue,
                    minValue = minValue,
                    size = size
                )
            }
        }
        
        // Draw labels overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Y-axis labels
            for (i in 5 downTo 0) {
                val value = (maxValue * i / 5).toInt()
                Text(
                    text = if (value > 0) "S/$value" else "S/0",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            sortedEntries.forEachIndexed { index, entry ->
                if (index % max(1, sortedEntries.size / 4) == 0) {
                    Text(
                        text = entry.key.format(dateFormatter),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawLineChart(
    entries: List<Map.Entry<ZonedDateTime, Double>>,
    maxValue: Double,
    minValue: Double,
    size: Size
) {
    val padding = 32.dp.toPx()
    val chartWidth = size.width - (padding * 2)
    val chartHeight = size.height - (padding * 2)
    
    val valueRange = maxValue - minValue
    if (valueRange == 0.0 || entries.size < 2) return
    
    val path = Path()
    val gradientPath = Path()
    
    entries.forEachIndexed { index, entry ->
        val x = padding + (index.toFloat() / (entries.size - 1)) * chartWidth
        val y = padding + chartHeight - ((entry.value - minValue) / valueRange * chartHeight).toFloat()
        
        if (index == 0) {
            path.moveTo(x, y)
            gradientPath.moveTo(x, size.height - padding)
            gradientPath.lineTo(x, y)
        } else {
            path.lineTo(x, y)
            gradientPath.lineTo(x, y)
        }
        
        // Draw point
        drawCircle(
            color = androidx.compose.ui.graphics.Color(0xFF2196F3),
            radius = 3.dp.toPx(),
            center = Offset(x, y)
        )
    }
    
    // Close gradient path
    if (entries.isNotEmpty()) {
        val lastX = padding + chartWidth
        gradientPath.lineTo(lastX, size.height - padding)
        gradientPath.close()
    }
    
    // Draw gradient fill
    drawPath(
        path = gradientPath,
        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                androidx.compose.ui.graphics.Color(0xFF2196F3).copy(alpha = 0.3f),
                androidx.compose.ui.graphics.Color.Transparent
            )
        )
    )
    
    // Draw line
    drawPath(
        path = path,
        color = androidx.compose.ui.graphics.Color(0xFF2196F3),
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )
}
