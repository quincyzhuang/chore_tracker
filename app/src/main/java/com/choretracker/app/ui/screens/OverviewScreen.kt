package com.choretracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.choretracker.app.data.Chore
import com.choretracker.app.viewmodel.ChoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(viewModel: ChoreViewModel) {
    val allChores by viewModel.allChores.collectAsState(initial = emptyList())
    val todayCompletions by viewModel.getTodayCompletions().collectAsState(initial = emptyList())
    val weekCompletions by viewModel.getWeekCompletions().collectAsState(initial = emptyList())
    val biweekCompletions by viewModel.getBiweekCompletions().collectAsState(initial = emptyList())
    val monthCompletions by viewModel.getMonthCompletions().collectAsState(initial = emptyList())

    val pendingDaily = remember(allChores, todayCompletions) {
        viewModel.getPendingChores(allChores, todayCompletions, ChoreViewModel.CATEGORY_DAILY)
    }
    val pendingWeekly = remember(allChores, weekCompletions) {
        viewModel.getPendingChores(allChores, weekCompletions, ChoreViewModel.CATEGORY_WEEKLY)
    }
    val pendingBiweekly = remember(allChores, biweekCompletions) {
        viewModel.getPendingChores(allChores, biweekCompletions, ChoreViewModel.CATEGORY_BIWEEKLY)
    }
    val pendingMonthly = remember(allChores, monthCompletions) {
        viewModel.getPendingChores(allChores, monthCompletions, ChoreViewModel.CATEGORY_MONTHLY)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overview") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Text(
                        text = ChoreViewModel.APP_VERSION,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Today's Date",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PendingCategorySection(
                title = "Daily Chores",
                subtitle = "Not done today",
                chores = pendingDaily,
                color = MaterialTheme.colorScheme.primary
            )

            PendingCategorySection(
                title = "Weekly Chores",
                subtitle = "Not done this week",
                chores = pendingWeekly,
                color = MaterialTheme.colorScheme.tertiary
            )

            PendingCategorySection(
                title = "Biweekly Chores",
                subtitle = "Not done this period",
                chores = pendingBiweekly,
                color = MaterialTheme.colorScheme.secondary
            )

            PendingCategorySection(
                title = "Monthly Chores",
                subtitle = "Not done this month",
                chores = pendingMonthly,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PendingCategorySection(
    title: String,
    subtitle: String,
    chores: List<Chore>,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (chores.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (chores.isEmpty()) {
                Text(
                    text = "All done!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                chores.forEach { chore ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chore.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
