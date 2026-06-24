package com.choretracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.choretracker.app.data.ChoreCompletion
import com.choretracker.app.viewmodel.ChoreViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: ChoreViewModel) {
    val scope = rememberCoroutineScope()
    val allCompletions by viewModel.allCompletions.collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf("Past 7 Days", "Past 30 Days", "All Time")

    val filteredCompletions = remember(allCompletions, selectedFilter) {
        val cutoff = when (selectedFilter) {
            0 -> System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            1 -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            else -> 0L
        }
        allCompletions.filter { it.completedDate >= cutoff }
    }

    val groupedByDate = remember(filteredCompletions) {
        filteredCompletions.groupBy { completion ->
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            sdf.format(Date(completion.completedDate))
        }.toSortedMap(Comparator.reverseOrder())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.cleanupOldHistory()
                        }
                    }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clean up old history",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterChipRow(
                filters = filters,
                selectedIndex = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            if (filteredCompletions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No completed chores in this period",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedByDate.forEach { (date, completions) ->
                        item {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(completions) { completion ->
                            HistoryItem(completion = completion, onUndo = {
                                viewModel.undoCompletion(completion)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    filters: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEachIndexed { index, filter ->
            FilterChip(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                label = { Text(filter) }
            )
        }
    }
}

@Composable
private fun HistoryItem(completion: ChoreCompletion, onUndo: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val categoryLabel = ChoreViewModel.categoryDisplayName(completion.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = completion.choreName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$categoryLabel - ${timeFormat.format(Date(completion.completedDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onUndo) {
                Text("Undo", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
