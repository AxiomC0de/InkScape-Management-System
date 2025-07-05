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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.joshua.inkscape.viewmodels.DashboardViewModel
import com.joshua.inkscape.viewmodels.DashboardViewModelFactory
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.joshua.inkscape.data.model.Activity
import androidx.compose.material3.MaterialTheme

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory())
) {
    val totalRevenue = viewModel.totalRevenue.collectAsState().value
    val totalSales = viewModel.totalSales.collectAsState().value
    val lowStockProducts = viewModel.lowStockProducts.collectAsState().value.size
    val recentActivities = viewModel.recentActivities.collectAsState().value
    val mostSoldCategory = viewModel.mostSoldCategory.collectAsState().value

    val sales = totalSales.toFloat()
    val lowStock = lowStockProducts.toFloat()

    val maxRange = (sales.coerceAtLeast(lowStock)) * 1.2f

    val barData = listOf(
        BarData(
            point = Point(0f, sales),
            label = "Sales",
            color = Color(0xFF2E8B57)
        ),
        BarData(
            point = Point(1f, lowStock),
            label = "Low Stock",
            color = Color(0xFFD2691E)
        )
    )

    val xAxisData = AxisData.Builder()
        .axisStepSize(30.dp)
        .steps(barData.size - 1)
        .bottomPadding(40.dp)
        .axisLabelAngle(20f)
        .labelData { index -> barData[index].label }
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .labelAndAxisLinePadding(20.dp)
        .axisOffset(20.dp)
        .labelData { index -> (index * (maxRange / 5)).toInt().toString() }
        .build()

    val barChartData = BarChartData(
        chartData = barData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        barStyle = BarStyle(
            paddingBetweenBars = 70.dp,
            barWidth = 70.dp
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            DashboardCard(
                "Top Category", 
                if (mostSoldCategory.first != "No Sales") {
                    "${mostSoldCategory.first}\nS/${NumberFormat.getNumberInstance(Locale.US).format(mostSoldCategory.second)}"
                } else {
                    "No Sales"
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        BarChart(modifier = Modifier.height(300.dp), barChartData = barChartData)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Recent Activity",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
                            userId = "user1"
                        ),
                        Activity(
                            id = "2",
                            details = "Completed sale #001",
                            timestamp = "2024-01-15T09:15:00Z",
                            type = "sale_complete",
                            userId = "user1"
                        ),
                        Activity(
                            id = "3",
                            details = "Updated product pricing",
                            timestamp = "2024-01-15T08:45:00Z",
                            type = "product_update",
                            userId = "user1"
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
}

@Composable
fun ActivityItem(activity: Activity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = activity.details, fontWeight = FontWeight.Bold)
            Text(text = activity.timestamp, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp)
        }
    }
}
