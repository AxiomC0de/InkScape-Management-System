package com.joshua.inkscape.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.LineType
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import com.joshua.inkscape.viewmodels.DashboardViewModel
import com.joshua.inkscape.viewmodels.DashboardViewModelFactory
import com.joshua.inkscape.data.model.Activity
import java.text.NumberFormat
import java.util.Locale

enum class TimePeriod(val label: String) {
    SEVEN_DAYS("7 Days"),
    MONTH("Month"),
    YEAR("Year")
}

data class CategoryRanking(
    val name: String,
    val sales: Int,
    val percentage: Float
)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory())
) {
    val totalRevenue = viewModel.totalRevenue.collectAsState().value
    val totalSales = viewModel.totalSales.collectAsState().value
    val lowStockProducts = viewModel.lowStockProducts.collectAsState().value.size
    val recentActivities = viewModel.recentActivities.collectAsState().value
    
    var selectedPeriod by remember { mutableStateOf(TimePeriod.SEVEN_DAYS) }
    
    // Generate sample data based on selected period
    val chartData = generateChartData(selectedPeriod, totalSales, totalRevenue)
    
    val steps = chartData.size - 1
    val maxValue = chartData.maxOfOrNull { it.y } ?: 100f
    
    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp)
        .steps(steps)
        .bottomPadding(50.dp)
        .labelData { index ->
            when (selectedPeriod) {
                TimePeriod.SEVEN_DAYS -> "Day ${index + 1}"
                TimePeriod.MONTH -> "Week ${index + 1}"
                TimePeriod.YEAR -> "Month ${index + 1}"
            }
        }
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index -> 
            (index * (maxValue / 5)).toInt().toString() 
        }
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = chartData,
                    lineStyle = LineStyle(
                        color = MaterialTheme.colorScheme.primary,
                        lineType = LineType.Straight(isDotted = false)
                    ),
                    intersectionPoint = IntersectionPoint(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    selectionHighlightPoint = SelectionHighlightPoint(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    shadowUnderLine = ShadowUnderLine(
                        alpha = 0.5f,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    ),
                    selectionHighlightPopUp = SelectionHighlightPopUp()
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        backgroundColor = MaterialTheme.colorScheme.surface
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Header
        item {
            Text(
                text = "Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Statistics Cards
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DashboardCard("Total Revenue", "S/${NumberFormat.getNumberInstance(Locale.US).format(totalRevenue)}")
                    DashboardCard("Total Sales", totalSales.toString())
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DashboardCard("Low Stock", lowStockProducts.toString())
                }
            }
        }

        // Chart Section with Time Period Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Chart Title and Period Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sales Analytics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        TimePeriodSelector(
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = { selectedPeriod = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Line Chart
                    LineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        lineChartData = lineChartData
                    )
                }
            }
        }

        // Category Sales Ranking Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Category Sales Ranking",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Generate sample category data
                    val categoryRankings = generateCategoryRankings()
                    
                    categoryRankings.forEachIndexed { index, category ->
                        CategoryRankingItem(
                            rank = index + 1,
                            categoryName = category.name,
                            salesCount = category.sales,
                            percentage = category.percentage,
                            isTopCategory = index == 0
                        )
                        
                        if (index < categoryRankings.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // Recent Activity Section with Scrollable List
        item {
            RecentActivitySection(activities = recentActivities)
        }
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimePeriod.values().forEach { period ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (period == selectedPeriod) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = period.label,
                    color = if (period == selectedPeriod) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = if (period == selectedPeriod) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// Generate sample chart data based on selected time period
fun generateChartData(period: TimePeriod, totalSales: Int, totalRevenue: Double): List<Point> {
    return when (period) {
        TimePeriod.SEVEN_DAYS -> {
            // Generate 7 days of data
            (0..6).map { day ->
                Point(
                    x = day.toFloat(),
                    y = (totalSales * (0.7f + Math.random().toFloat() * 0.6f))
                )
            }
        }
        TimePeriod.MONTH -> {
            // Generate 4 weeks of data
            (0..3).map { week ->
                Point(
                    x = week.toFloat(),
                    y = (totalSales * (0.8f + Math.random().toFloat() * 0.4f))
                )
            }
        }
        TimePeriod.YEAR -> {
            // Generate 12 months of data
            (0..11).map { month ->
                Point(
                    x = month.toFloat(),
                    y = (totalSales * (0.6f + Math.random().toFloat() * 0.8f))
                )
            }
        }
    }
}

// Generate sample category ranking data
fun generateCategoryRankings(): List<CategoryRanking> {
    val categories = listOf(
        "Electronics" to 342,
        "Clothing" to 298,
        "Home & Garden" to 256,
        "Sports" to 189,
        "Books" to 134,
        "Beauty" to 98
    )
    
    val totalSales = categories.sumOf { it.second }
    
    return categories.map { (name, sales) ->
        CategoryRanking(
            name = name,
            sales = sales,
            percentage = (sales.toFloat() / totalSales) * 100f
        )
    }
}

@Composable
fun CategoryRankingItem(
    rank: Int,
    categoryName: String,
    salesCount: Int,
    percentage: Float,
    isTopCategory: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isTopCategory) 
                        Color(0xFFFFD700) // Gold for #1
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                color = if (isTopCategory) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Category Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = categoryName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$salesCount sales",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Percentage and Progress Bar
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${percentage.toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percentage / 100f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isTopCategory) 
                                Color(0xFFFFD700) 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<Activity>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with activity count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Activity count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${activities.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scrollable activities list with fixed height
            if (activities.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Fixed height to contain scrolling
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activities) { activity ->
                        ActivityItem(activity)
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent activities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Activity content
            Column(
                modifier = Modifier.weight(1f)
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 120.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
