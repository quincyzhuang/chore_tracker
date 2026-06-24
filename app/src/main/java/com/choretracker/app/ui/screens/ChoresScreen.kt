package com.choretracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.choretracker.app.data.Chore
import com.choretracker.app.data.ChoreCompletion
import com.choretracker.app.viewmodel.ChoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoresScreen(viewModel: ChoreViewModel) {
    val allChores by viewModel.allChores.collectAsState(initial = emptyList())
    val todayCompletions by viewModel.getTodayCompletions().collectAsState(initial = emptyList())
    val weekCompletions by viewModel.getWeekCompletions().collectAsState(initial = emptyList())
    val biweekCompletions by viewModel.getBiweekCompletions().collectAsState(initial = emptyList())
    val monthCompletions by viewModel.getMonthCompletions().collectAsState(initial = emptyList())

    val groupedChores = remember(allChores) {
        allChores.groupBy { it.category }
    }

    val doneToday = remember(todayCompletions) { todayCompletions.map { it.choreName }.toSet() }
    val doneThisWeek = remember(weekCompletions) { weekCompletions.map { it.choreName }.toSet() }
    val doneThisBiweek = remember(biweekCompletions) { biweekCompletions.map { it.choreName }.toSet() }
    val doneThisMonth = remember(monthCompletions) { monthCompletions.map { it.choreName }.toSet() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chores") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (allChores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No chores yet. Add some in Manage!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                choreCategorySection(
                    label = "Daily",
                    chores = groupedChores[ChoreViewModel.CATEGORY_DAILY] ?: emptyList(),
                    doneSet = doneToday,
                    completions = todayCompletions,
                    onCheck = { viewModel.completeChore(it) },
                    onUncheck = { viewModel.undoCompletion(it) }
                )
                choreCategorySection(
                    label = "Weekly",
                    chores = groupedChores[ChoreViewModel.CATEGORY_WEEKLY] ?: emptyList(),
                    doneSet = doneThisWeek,
                    completions = weekCompletions,
                    onCheck = { viewModel.completeChore(it) },
                    onUncheck = { viewModel.undoCompletion(it) }
                )
                choreCategorySection(
                    label = "Biweekly",
                    chores = groupedChores[ChoreViewModel.CATEGORY_BIWEEKLY] ?: emptyList(),
                    doneSet = doneThisBiweek,
                    completions = biweekCompletions,
                    onCheck = { viewModel.completeChore(it) },
                    onUncheck = { viewModel.undoCompletion(it) }
                )
                choreCategorySection(
                    label = "Monthly",
                    chores = groupedChores[ChoreViewModel.CATEGORY_MONTHLY] ?: emptyList(),
                    doneSet = doneThisMonth,
                    completions = monthCompletions,
                    onCheck = { viewModel.completeChore(it) },
                    onUncheck = { viewModel.undoCompletion(it) }
                )
            }
        }
    }
}

private fun LazyListScope.choreCategorySection(
    label: String,
    chores: List<Chore>,
    doneSet: Set<String>,
    completions: List<ChoreCompletion>,
    onCheck: (Chore) -> Unit,
    onUncheck: (ChoreCompletion) -> Unit
) {
    if (chores.isEmpty()) return

    item {
        Text(
            text = "$label Chores",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
    }

    items(chores) { chore ->
        val isDone = chore.name in doneSet
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDone)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            onClick = {
                if (isDone) {
                    completions.lastOrNull { it.choreName == chore.name }?.let(onUncheck)
                } else {
                    onCheck(chore)
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chore.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle
                    else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isDone) "Done" else "Not done",
                    tint = if (isDone) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
