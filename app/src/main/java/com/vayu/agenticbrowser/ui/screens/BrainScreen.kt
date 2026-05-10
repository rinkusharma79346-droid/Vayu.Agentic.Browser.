package com.vayu.agenticbrowser.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vayu.agenticbrowser.brain.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainScreen(
    onBack: () -> Unit,
    agentLoop: AgentLoop
) {
    val context = LocalContext.current
    val state by agentLoop.state.collectAsState()
    val currentGoal by agentLoop.currentGoal.collectAsState()
    val stepLog by agentLoop.stepLog.collectAsState()
    val totalTokens by agentLoop.totalTokens.collectAsState()
    val totalSteps by agentLoop.totalSteps.collectAsState()
    val goalScheduler = remember { GoalScheduler.getInstance() }
    val scheduledGoals by goalScheduler.scheduledGoals.collectAsState()

    var goalInput by remember { mutableStateOf("") }
    var configExpanded by remember { mutableStateOf(false) }
    var scheduleExpanded by remember { mutableStateOf(false) }
    var scheduleGoalText by remember { mutableStateOf("") }
    var scheduleDelayMinutes by remember { mutableStateOf("60") }

    val config = remember { agentLoop.getConfig() }
    var provider by remember { mutableStateOf(config.provider) }
    var apiKey by remember { mutableStateOf(config.apiKey) }
    var baseUrl by remember { mutableStateOf(config.baseUrl) }
    var model by remember { mutableStateOf(config.model) }
    var maxTokens by remember { mutableStateOf(config.maxTokens.toString()) }
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VAYU Brain") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = when (state) {
                        AgentState.THINKING -> MaterialTheme.colorScheme.tertiaryContainer
                        AgentState.EXECUTING -> MaterialTheme.colorScheme.primaryContainer
                        AgentState.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                        AgentState.FAILED -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // State indicator
            StateIndicator(state, currentGoal, totalSteps, totalTokens)

            // Goal input
            OutlinedTextField(
                value = goalInput,
                onValueChange = { goalInput = it },
                label = { Text("Enter your goal...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = state == AgentState.IDLE
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (goalInput.isNotBlank() && state == AgentState.IDLE) {
                            agentLoop.runGoal(goalInput)
                        }
                    },
                    enabled = goalInput.isNotBlank() && state == AgentState.IDLE,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run Goal")
                }

                if (state != AgentState.IDLE) {
                    OutlinedButton(
                        onClick = { agentLoop.stop() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop")
                    }
                }
            }

            HorizontalDivider()

            // Step log
            Text(
                text = "Step Log (${stepLog.size} steps)",
                style = MaterialTheme.typography.titleSmall
            )

            if (stepLog.isEmpty()) {
                Text(
                    text = "No steps yet. Enter a goal and click Run.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(stepLog) { step ->
                        StepCard(step)
                    }
                }
            }

            HorizontalDivider()

            // Configuration section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Configuration", style = MaterialTheme.typography.titleSmall)
                }
                IconButton(onClick = { configExpanded = !configExpanded }) {
                    Icon(
                        if (configExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (configExpanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = configExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Provider selection
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LlmProvider.entries.forEach { p ->
                            FilterChip(
                                selected = provider == p,
                                onClick = {
                                    provider = p
                                    val defaults = BrainConfig.providerDefaults[p]
                                    if (defaults != null) {
                                        if (baseUrl.isBlank()) baseUrl = defaults.first
                                        if (model.isBlank()) model = defaults.second
                                    }
                                },
                                label = { Text(p.name, fontSize = 12.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        visualTransformation = if (showApiKey) VisualTransformation.None
                                                else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    if (showApiKey) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = "Toggle"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = maxTokens,
                        onValueChange = { maxTokens = it },
                        label = { Text("Max Tokens") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val newConfig = BrainConfig(
                                provider = provider,
                                apiKey = apiKey,
                                baseUrl = baseUrl,
                                model = model,
                                maxTokens = maxTokens.toIntOrNull() ?: 8192,
                                enabled = apiKey.isNotBlank()
                            )
                            agentLoop.updateConfig(newConfig)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Configuration")
                    }
                }
            }

            HorizontalDivider()

            // Schedule section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Scheduled Goals", style = MaterialTheme.typography.titleSmall)
                }
                IconButton(onClick = { scheduleExpanded = !scheduleExpanded }) {
                    Icon(
                        if (scheduleExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (scheduleExpanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = scheduleExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scheduleGoalText,
                        onValueChange = { scheduleGoalText = it },
                        label = { Text("Goal to schedule") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = scheduleDelayMinutes,
                            onValueChange = { scheduleDelayMinutes = it },
                            label = { Text("Delay (minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (scheduleGoalText.isNotBlank()) {
                                    val delayMs = (scheduleDelayMinutes.toLongOrNull() ?: 60) * 60_000L
                                    goalScheduler.scheduleGoal(
                                        goal = scheduleGoalText,
                                        scheduledAt = System.currentTimeMillis() + delayMs
                                    )
                                    scheduleGoalText = ""
                                }
                            },
                            enabled = scheduleGoalText.isNotBlank()
                        ) {
                            Text("Schedule")
                        }
                    }

                    // List scheduled goals
                    scheduledGoals.filter { !it.completed }.forEach { goal ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goal.goal,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "Run at: ${formatDate(goal.scheduledAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { goalScheduler.deleteGoal(goal.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StateIndicator(
    state: AgentState,
    currentGoal: String?,
    totalSteps: Int,
    totalTokens: Int
) {
    val (color, icon, label) = when (state) {
        AgentState.IDLE -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            Icons.Default.HourglassEmpty,
            "Idle"
        )
        AgentState.THINKING -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.Psychology,
            "Thinking..."
        )
        AgentState.EXECUTING -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Default.Build,
            "Executing..."
        )
        AgentState.WAITING -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            Icons.Default.HourglassTop,
            "Waiting..."
        )
        AgentState.COMPLETED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Default.CheckCircle,
            "Completed"
        )
        AgentState.FAILED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Default.Error,
            "Failed"
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Text(text = label, style = MaterialTheme.typography.titleMedium)
            }

            if (currentGoal != null) {
                Text(
                    text = "Goal: $currentGoal",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Steps: $totalSteps",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Tokens: $totalTokens",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun StepCard(step: AgentStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                step.tool != null -> MaterialTheme.colorScheme.secondaryContainer
                step.thought != null -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Thought
            if (step.thought != null) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = step.thought,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3
                    )
                }
            }

            // Tool call
            if (step.tool != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = step.tool,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (step.args != null) {
                    Text(
                        text = step.args.take(150),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.padding(start = 18.dp)
                    )
                }
            }

            // Result
            if (step.result != null) {
                Text(
                    text = "Result: ${step.result.take(200)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    modifier = Modifier.padding(start = 18.dp, top = 2.dp)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
