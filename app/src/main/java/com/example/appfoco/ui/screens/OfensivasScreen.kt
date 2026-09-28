package com.example.appfoco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfoco.data.entity.OfensivaEntity
import com.example.appfoco.ui.theme.DarkCard
import com.example.appfoco.ui.viewmodel.OfensivasViewModel
import com.example.appfoco.widget.FocoAppWidgetProvider

@Composable
fun OfensivasScreen(
    viewModel: OfensivasViewModel,
    onNavigateToEdit: () -> Unit
) {
    val context = LocalContext.current
    val ofensivas by viewModel.ofensivas.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var ofensivaToDelete by remember { mutableStateOf<OfensivaEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Ofensiva")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ofensivas.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            } else {
                items(ofensivas) { ofensiva ->
                    val isDoneToday = ofensiva.isAlive && ofensiva.lastProgressDate == currentDate
                    val fireIcon = if (isDoneToday) "🔥 " else ""
                    val dayLabel = if (ofensiva.streakCount <= 1) "dia" else "dias"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${fireIcon}Ofensiva: ${ofensiva.streakCount} $dayLabel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ofensiva.name,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    viewModel.setEditingOfensivaId(ofensiva.id)
                                    onNavigateToEdit()
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Configurar Ofensiva",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = {
                                    if (FocoAppWidgetProvider.isOfensivaUsedByAnyWidget(context, ofensiva.id)) {
                                        Toast.makeText(
                                            context,
                                            "Não é possível apagar uma ofensiva com widget ativo. Remova o widget da tela inicial primeiro.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        ofensivaToDelete = ofensiva
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Apagar Ofensiva",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var initialItemName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf("Hábito") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = null,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nova Ofensiva", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("É obrigatório criar pelo menos 1 item inicial para a ofensiva.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da Ofensiva") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Hábito", "Tarefa", "Proibido").forEach { type ->
                            Box(modifier = Modifier.weight(1f)) {
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    label = { Text(type, maxLines = 1, fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = initialItemName,
                        onValueChange = { initialItemName = it },
                        label = { Text("Nome do(a) $selectedType Inicial") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && initialItemName.isNotBlank()) {
                            viewModel.createOfensiva(name, initialItemName, selectedType)
                            showCreateDialog = false
                        }
                    },
                    enabled = name.isNotBlank() && initialItemName.isNotBlank()
                ) { Text("Criar") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (ofensivaToDelete != null) {
        AlertDialog(
            onDismissRequest = { ofensivaToDelete = null },
            title = null,
            text = {
                Text("Deseja realmente apagar a ofensiva?", fontSize = 16.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        ofensivaToDelete?.let { viewModel.deleteOfensiva(it) }
                        ofensivaToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, Apagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { ofensivaToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
