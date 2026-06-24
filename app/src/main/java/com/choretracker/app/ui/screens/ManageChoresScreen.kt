package com.choretracker.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.choretracker.app.data.Chore
import com.choretracker.app.viewmodel.ChoreViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageChoresScreen(viewModel: ChoreViewModel) {
    val allChores by viewModel.allChores.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Chore?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val groupedChores = remember(allChores) {
        allChores.groupBy { it.category }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.getExportJson()
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        os.write(json.toByteArray())
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Export failed: ${e.message}")
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(it)?.use { input ->
                        input.bufferedReader().readText()
                    } ?: return@launch
                    viewModel.importChores(json) { added, skipped ->
                        scope.launch {
                            val message = if (added >= 0)
                                "Imported $added chore(s), $skipped skipped"
                            else
                                "Import failed: invalid file format"
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Import failed: ${e.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Chores") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add chore") },
                                onClick = { showMenu = false; showAddDialog = true },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Export") },
                                onClick = { showMenu = false; exportLauncher.launch("chores_export.json") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Import") },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("application/json", "*/*"))
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (allChores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No chores yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add your first chore")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ManageCategorySection(
                    label = "Daily",
                    chores = groupedChores[ChoreViewModel.CATEGORY_DAILY] ?: emptyList(),
                    onDelete = { showDeleteConfirm = it }
                )
                ManageCategorySection(
                    label = "Weekly",
                    chores = groupedChores[ChoreViewModel.CATEGORY_WEEKLY] ?: emptyList(),
                    onDelete = { showDeleteConfirm = it }
                )
                ManageCategorySection(
                    label = "Biweekly",
                    chores = groupedChores[ChoreViewModel.CATEGORY_BIWEEKLY] ?: emptyList(),
                    onDelete = { showDeleteConfirm = it }
                )
                ManageCategorySection(
                    label = "Monthly",
                    chores = groupedChores[ChoreViewModel.CATEGORY_MONTHLY] ?: emptyList(),
                    onDelete = { showDeleteConfirm = it }
                )
            }
        }
    }

    if (showAddDialog) {
        AddChoreDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category ->
                viewModel.addChore(name, category)
                showAddDialog = false
            }
        )
    }

    showDeleteConfirm?.let { chore ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Chore") },
            text = { Text("Are you sure you want to delete \"${chore.name}\"? This will also remove its history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteChore(chore)
                    showDeleteConfirm = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun LazyListScope.ManageCategorySection(
    label: String,
    chores: List<Chore>,
    onDelete: (Chore) -> Unit
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chore.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDelete(chore) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChoreDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var choreName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ChoreViewModel.CATEGORY_DAILY) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Chore") },
        text = {
            Column {
                OutlinedTextField(
                    value = choreName,
                    onValueChange = { choreName = it },
                    label = { Text("Chore name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = ChoreViewModel.categoryDisplayName(selectedCategory),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ChoreViewModel.CATEGORIES.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(ChoreViewModel.categoryDisplayName(category)) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(choreName, selectedCategory) },
                enabled = choreName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
