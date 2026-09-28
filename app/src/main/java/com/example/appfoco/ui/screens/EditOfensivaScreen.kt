package com.example.appfoco.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfoco.R
import com.example.appfoco.data.entity.ForbiddenRuleEntity
import com.example.appfoco.data.entity.HabitEntity
import com.example.appfoco.data.entity.TaskEntity
import com.example.appfoco.ui.theme.DarkCard
import com.example.appfoco.ui.viewmodel.OfensivasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOfensivaScreen(
    viewModel: OfensivasViewModel,
    onBack: () -> Unit
) {
    val ofensivas by viewModel.ofensivas.collectAsState()
    val editingId by viewModel.editingOfensivaId.collectAsState()
    val ofensiva = ofensivas.find { it.id == editingId } ?: ofensivas.firstOrNull()

    val habits by viewModel.habitsForEditing.collectAsState()
    val tasks by viewModel.tasksForEditing.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val rules by viewModel.rulesForEditing.collectAsState()
    val ruleLogs by viewModel.ruleLogs.collectAsState()

    var name by remember(ofensiva) { mutableStateOf(ofensiva?.name ?: "") }
    var showAddDialog by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }

    var itemToDelete by remember { mutableStateOf<Any?>(null) }

    var editingHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var editingRule by remember { mutableStateOf<ForbiddenRuleEntity?>(null) }

    Scaffold { padding ->
        if (ofensiva == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Ofensiva não encontrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                onBack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_splash_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier.size(96.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.updateOfensivaName(ofensiva, it)
                        },
                        label = { Text("Nome da Ofensiva") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(habits) { habit ->
                    val log = habitLogs.find { it.habitId == habit.id }
                    val isCompleted = log?.isCompleted == true
                    Card(colors = CardDefaults.cardColors(containerColor = DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isCompleted,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleHabitLog(habit, checked)
                                    }
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(habit.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Hábito", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("Dias: ${formatDaysOfWeek(habit.daysOfWeek)}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { editingHabit = habit }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { itemToDelete = habit }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                items(tasks) { task ->
                    Card(colors = CardDefaults.cardColors(containerColor = DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleTask(task, checked)
                                    }
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(task.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Tarefa", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { editingTask = task }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { itemToDelete = task }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                items(rules) { rule ->
                    val rLog = ruleLogs.find { it.ruleId == rule.id }
                    val isBroken = rLog?.isBroken == true
                    Card(colors = CardDefaults.cardColors(containerColor = DarkCard), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isBroken,
                                    onCheckedChange = { broken ->
                                        viewModel.toggleRuleLog(rule, broken)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(rule.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Proibido", fontSize = 9.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(if (isBroken) "⚠️ Quebrado hoje (Fogo apagado)" else "Seguindo regra", fontSize = 9.sp, color = if (isBroken) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { editingRule = rule }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { itemToDelete = rule }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    val hasItems = habits.isNotEmpty() || tasks.isNotEmpty() || rules.isNotEmpty()
                    if (!hasItems) {
                        Text(
                            text = "⚠️ Adicione pelo menos 1 hábito, tarefa ou proibido para concluir.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (hasItems && !isNavigating) {
                                isNavigating = true
                                onBack()
                            }
                        },
                        enabled = hasItems,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Concluir")
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = null,
            text = {
                Text("Deseja realmente apagar este item?", fontSize = 16.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (val item = itemToDelete) {
                            is HabitEntity -> viewModel.deleteHabit(item)
                            is TaskEntity -> viewModel.deleteTask(item)
                            is ForbiddenRuleEntity -> viewModel.deleteRule(item)
                        }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, Apagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddDialog) {
        var itemName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf("Hábito") }
        var selectedDays by remember { mutableStateOf(setOf("2", "3", "4", "5", "6")) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = null,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hábito", "Tarefa", "Proibido").forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    val descriptionText = when (selectedType) {
                        "Hábito" -> "Ações recorrentes que se repetem nos dias selecionados da semana e mantêm sua ofensiva ativa."
                        "Tarefa" -> "Atividades pontuais agendadas apenas para o dia de hoje."
                        "Proibido" -> "Coisas que você quer evitar ou parar de fazer para não apagar seu fogo."
                        else -> ""
                    }
                    Text(
                        text = descriptionText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    if (selectedType == "Hábito") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Dias ativos:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        val daysMap = mapOf(
                            "1" to "Dom", "2" to "Seg", "3" to "Ter", "4" to "Qua",
                            "5" to "Qui", "6" to "Sex", "7" to "Sáb"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("1", "2", "3", "4").forEach { key ->
                                    val label = daysMap[key] ?: ""
                                    val isSelected = selectedDays.contains(key)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedDays = if (isSelected) selectedDays - key else selectedDays + key
                                        },
                                        label = { Text(label, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("5", "6", "7").forEach { key ->
                                    val label = daysMap[key] ?: ""
                                    val isSelected = selectedDays.contains(key)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedDays = if (isSelected) selectedDays - key else selectedDays + key
                                        },
                                        label = { Text(label, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemName.isNotBlank() && ofensiva != null) {
                            when (selectedType) {
                                "Hábito" -> {
                                    if (selectedDays.isNotEmpty()) {
                                        val daysStr = selectedDays.sortedBy { it.toInt() }.joinToString(",")
                                        viewModel.addHabit(ofensiva.id, itemName, null, daysStr)
                                    }
                                }
                                "Tarefa" -> {
                                    viewModel.addTask(ofensiva.id, itemName, null)
                                }
                                "Proibido" -> {
                                    viewModel.addRule(ofensiva.id, itemName, null)
                                }
                            }
                            showAddDialog = false
                        }
                    },
                    enabled = itemName.isNotBlank()
                ) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (editingHabit != null) {
        var editName by remember(editingHabit) { mutableStateOf(editingHabit?.name ?: "") }
        var editDays by remember(editingHabit) { mutableStateOf(editingHabit?.daysOfWeek?.split(",")?.toSet() ?: setOf("2", "3", "4", "5", "6")) }

        AlertDialog(
            onDismissRequest = { editingHabit = null },
            title = null,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Editar Hábito", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome do Hábito") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Dias ativos:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    val daysMap = mapOf(
                        "1" to "Dom", "2" to "Seg", "3" to "Ter", "4" to "Qua",
                        "5" to "Qui", "6" to "Sex", "7" to "Sáb"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("1", "2", "3", "4").forEach { key ->
                                val label = daysMap[key] ?: ""
                                val isSelected = editDays.contains(key)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        editDays = if (isSelected) editDays - key else editDays + key
                                    },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("5", "6", "7").forEach { key ->
                                val label = daysMap[key] ?: ""
                                val isSelected = editDays.contains(key)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        editDays = if (isSelected) editDays - key else editDays + key
                                    },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && editDays.isNotEmpty() && editingHabit != null) {
                            val daysStr = editDays.sortedBy { it.toInt() }.joinToString(",")
                            viewModel.updateHabit(editingHabit!!.copy(name = editName, daysOfWeek = daysStr))
                            editingHabit = null
                        }
                    },
                    enabled = editName.isNotBlank() && editDays.isNotEmpty()
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editingHabit = null }) { Text("Cancelar") }
            }
        )
    }

    if (editingTask != null) {
        var editName by remember(editingTask) { mutableStateOf(editingTask?.name ?: "") }

        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = null,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Editar Tarefa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome da Tarefa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && editingTask != null) {
                            viewModel.updateTask(editingTask!!.copy(name = editName))
                            editingTask = null
                        }
                    },
                    enabled = editName.isNotBlank()
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editingTask = null }) { Text("Cancelar") }
            }
        )
    }

    if (editingRule != null) {
        var editName by remember(editingRule) { mutableStateOf(editingRule?.name ?: "") }

        AlertDialog(
            onDismissRequest = { editingRule = null },
            title = null,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Editar Regra Proibida", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome da Regra") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && editingRule != null) {
                            viewModel.updateRule(editingRule!!.copy(name = editName))
                            editingRule = null
                        }
                    },
                    enabled = editName.isNotBlank()
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editingRule = null }) { Text("Cancelar") }
            }
        )
    }
}

fun formatDaysOfWeek(daysStr: String): String {
    if (daysStr == "1,2,3,4,5,6,7") return "Todos os dias"
    val map = mapOf(
        "1" to "Dom", "2" to "Seg", "3" to "Ter", "4" to "Qua",
        "5" to "Qui", "6" to "Sex", "7" to "Sáb"
    )
    return daysStr.split(",").mapNotNull { map[it] }.joinToString(", ")
}
