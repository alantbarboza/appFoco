package com.example.appfoco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfoco.ui.viewmodel.OfensivasViewModel
import com.example.appfoco.utils.BackupManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: OfensivasViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val ofensivas by viewModel.ofensivas.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val maxHighestStreak by viewModel.maxHighestStreak.collectAsState()
    val extinguishedCount by viewModel.extinguishedCount.collectAsState()

    var isDevModeEnabled by remember(settings) { mutableStateOf(settings?.isDevModeActive == true) }
    var selectedDevOfensivaId by remember { mutableStateOf<Long?>(null) }
    var manualStreakInput by remember { mutableStateOf("") }
    var manualDaysToAdvanceInput by remember { mutableStateOf("1") }

    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showViewJsonDialog by remember { mutableStateOf(false) }
    var currentJsonData by remember { mutableStateOf("") }

    val activeDevOfensiva = ofensivas.find { it.id == selectedDevOfensivaId } ?: ofensivas.firstOrNull()

    val highestStreakLabel = if (maxHighestStreak <= 1) "dia" else "dias"

    val bodyFontSize = 14.sp
    val bodyFontWeight = FontWeight.Normal
    val bodyTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção 1: Histórico
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Histórico", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Maior Ofensiva: $maxHighestStreak $highestStreakLabel", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                        Text("Perca de ofensiva: $extinguishedCount", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                        Text("Data Atual: $currentDate", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                    }
                }
            }

            // Seção 2: Dados
            item {
                Text("Dados", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        val result = BackupManager.exportBackup(context)
                                        if (result.isSuccess) {
                                            val file = result.getOrNull()
                                            Toast.makeText(context, "Backup exportado para Downloads: ${file?.name}", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Erro ao exportar backup: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text("Exportar (Baixar um arquivo backup.txt)", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                        }

                        HorizontalDivider()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showImportDialog = true }
                                .padding(vertical = 10.dp)
                        ) {
                            Text("Importar (Enviar um JSON)", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                        }

                        HorizontalDivider()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        currentJsonData = BackupManager.getCurrentJsonData(context)
                                        showViewJsonDialog = true
                                    }
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text("Visualizar JSON atual (Copiar dados)", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                        }

                        HorizontalDivider()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showResetDialog = true }
                                .padding(vertical = 10.dp)
                        ) {
                            Text("Resetar Aplicativo", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Seção 3: Desenvolvedor
            item {
                Text("Desenvolvedor", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo Desenvolvedor", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                            Switch(
                                checked = isDevModeEnabled,
                                onCheckedChange = {
                                    isDevModeEnabled = it
                                    viewModel.setDevModeActive(it)
                                }
                            )
                        }

                        if (isDevModeEnabled) {
                            HorizontalDivider()

                            if (ofensivas.isEmpty()) {
                                Text("Nenhuma ofensiva disponível.", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Selecione a ofensiva:", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ofensivas.forEach { of ->
                                        FilterChip(
                                            selected = activeDevOfensiva?.id == of.id,
                                            onClick = { selectedDevOfensivaId = of.id },
                                            label = { Text(of.name) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text("Simulação de tempo:", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = manualDaysToAdvanceInput,
                                        onValueChange = { manualDaysToAdvanceInput = it },
                                        placeholder = null,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            val days = manualDaysToAdvanceInput.toIntOrNull()
                                            if (days != null && days > 0) {
                                                viewModel.advanceDays(days)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Avançar")
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                if (activeDevOfensiva != null) {
                                    Text("Simulação de ofensiva:", fontSize = bodyFontSize, fontWeight = bodyFontWeight, color = bodyTextColor)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = manualStreakInput,
                                            onValueChange = { manualStreakInput = it },
                                            placeholder = null,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                val days = manualStreakInput.toIntOrNull()
                                                if (days != null) {
                                                    viewModel.setOfensivaStreak(activeDevOfensiva.id, days)
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Aplicar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Import JSON Dialog
    if (showImportDialog) {
        var jsonInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Importar Backup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Você poderá importar as tarefas, hábitos, proibições, ofensivas e histórico colando o código JSON no campo abaixo.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        placeholder = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = {
                            jsonInput = BackupManager.getJsonTemplate()
                        }) {
                            Text("Ver Modelo JSON", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = BackupManager.importBackupFromJsonString(context, jsonInput)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Backup importado com sucesso!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Erro ao importar: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = jsonInput.isNotBlank()
                ) { Text("Importar") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // View Current JSON Dialog
    if (showViewJsonDialog) {
        AlertDialog(
            onDismissRequest = { showViewJsonDialog = false },
            title = { Text("JSON Atual do App") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Você pode rolar, selecionar e copiar o JSON abaixo:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = currentJsonData,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(currentJsonData))
                    Toast.makeText(context, "JSON copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                    showViewJsonDialog = false
                }) {
                    Text("Copiar Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showViewJsonDialog = false }) { Text("Fechar") }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = null,
            text = { Text("Deseja realmente fazer o reset?", fontSize = 16.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, Resetar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
