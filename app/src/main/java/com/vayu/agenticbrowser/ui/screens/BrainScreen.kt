package com.vayu.agenticbrowser.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vayu.agenticbrowser.brain.*
import kotlinx.coroutines.delay
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

    // Workflow engine
    val workflowEngine = remember { com.vayu.agenticbrowser.brain.WorkflowEngine(context, agentLoop) }
    val workflows by workflowEngine.workflows.collectAsState()

    var goalInput by remember { mutableStateOf("") }
    var configExpanded by remember { mutableStateOf(false) }
    var scheduleExpanded by remember { mutableStateOf(false) }
    var scheduleGoalText by remember { mutableStateOf("") }
    var scheduleDelayMinutes by remember { mutableStateOf("60") }
    var workflowPickerExpanded by remember { mutableStateOf(false) }

    val config = remember { agentLoop.getConfig() }
    var provider by remember { mutableStateOf(config.provider) }
    var apiKey by remember { mutableStateOf(config.apiKey) }
    var baseUrl by remember { mutableStateOf(config.baseUrl) }
    var model by remember { mutableStateOf(config.model) }
    var maxTokens by remember { mutableStateOf(config.maxTokens.toString()) }
    var showApiKey by remember { mutableStateOf(false) }

    // Elapsed time counter
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    val isRunning = state == AgentState.THINKING || state == AgentState.EXECUTING || state == AgentState.WAITING
    LaunchedEffect(isRunning) {
        elapsedSeconds = 0
        while (isRunning) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Auto-scroll step log
    val listState = rememberLazyListState()
    LaunchedEffect(stepLog.size) {
        if (stepLog.isNotEmpty()) {
            listState.animateScrollToItem(stepLog.size - 1)
        }
    }

    // Thinking animation
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val thinkingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinkingAlpha"
    )

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
            // ===== Top Section — State Indicator =====
            StateIndicator(state, currentGoal, totalSteps, totalTokens, elapsedSeconds, thinkingAlpha)

            // ===== Goal Input =====
            OutlinedTextField(
                value = goalInput,
                onValueChange = { goalInput = it },
                label = { Text("Enter a goal for VAYU...") },
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
                    Text("Run")
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
                        Text("Cancel")
                    }
                }
            }

            // ===== Workflow Picker =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Workflows", style = MaterialTheme.typography.titleSmall)
                }
                IconButton(onClick = { workflowPickerExpanded = !workflowPickerExpanded }) {
                    Icon(
                        if (workflowPickerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (workflowPickerExpanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = workflowPickerExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    workflows.forEach { workflow ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (workflow.isBuiltIn)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = workflow.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (workflow.isBuiltIn) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = MaterialTheme.shapes.extraSmall,
                                                color = MaterialTheme.colorScheme.tertiary
                                            ) {
                                                Text(
                                                    text = "built-in",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onTertiary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = workflow.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row {
                                    // Fill goal field
                                    IconButton(
                                        onClick = { goalInput = workflow.goalPrompt },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy to goal",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    // Run directly
                                    IconButton(
                                        onClick = {
                                            if (state == AgentState.IDLE) {
                                                agentLoop.runGoal(workflow.goalPrompt)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp),
                                        enabled = state == AgentState.IDLE
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Run workflow",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    // Delete (only user workflows)
                                    if (!workflow.isBuiltIn) {
                                        IconButton(
                                            onClick = { workflowEngine.deleteWorkflow(workflow.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(16.dp),
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

            HorizontalDivider()

            // ===== Middle Section — Live Step Log =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Step Log (${stepLog.size} steps)",
                    style = MaterialTheme.typography.titleSmall
                )
                if (state == AgentState.THINKING) {
                    Text(
                        text = "Thinking...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.alpha(thinkingAlpha)
                    )
                }
            }

            if (stepLog.isEmpty()) {
                Text(
                    text = "No steps yet. Enter a goal and click Run.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    state = listState
                ) {
                    items(stepLog) { step ->
                        StepCard(step)
                    }
                }
            }

            HorizontalDivider()

            // ===== Bottom Section — Status Bar =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // State color indicator
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = when (state) {
                        AgentState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
                        AgentState.THINKING -> MaterialTheme.colorScheme.tertiary
                        AgentState.EXECUTING -> MaterialTheme.colorScheme.primary
                        AgentState.WAITING -> MaterialTheme.colorScheme.secondary
                        AgentState.COMPLETED -> MaterialTheme.colorScheme.primary
                        AgentState.FAILED -> MaterialTheme.colorScheme.error
                    }
                ) {}

                Text(
                    text = state.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (state) {
                        AgentState.FAILED -> MaterialTheme.colorScheme.error
                        AgentState.COMPLETED -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = "Step $totalSteps / 50",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Elapsed time
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                Text(
                    text = String.format("%d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${totalTokens} tokens",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // ===== Configuration section =====
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

                    if (provider == LlmProvider.CUSTOM) {
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

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

            // ===== Schedule section =====
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
    totalTokens: Int,
    elapsedSeconds: Int,
    thinkingAlpha: Float
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
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).let { mod ->
                        if (state == AgentState.THINKING) mod.alpha(thinkingAlpha) else mod
                    }
                )
                Text(text = label, style = MaterialTheme.typography.titleMedium)
            }

            if (currentGoal != null) {
                Text(
                    text = "Goal: ${currentGoal.take(100)}${if (currentGoal.length > 100) "..." else ""}",
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
                    text = "Steps: $totalSteps / 50",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Tokens: $totalTokens",
                    style = MaterialTheme.typography.labelSmall
                )
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                Text(
                    text = String.format("Time: %d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun StepCard(step: AgentStep) {
    var argsExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                step.result != null && step.result.startsWith("Error") -> MaterialTheme.colorScheme.errorContainer
                step.tool != null -> MaterialTheme.colorScheme.secondaryContainer
                step.thought != null -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Step number
            Text(
                text = "#${step.index}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

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
                        maxLines = if (argsExpanded) Int.MAX_VALUE else 3
                    )
                }
            }

            // Tool call — with cyan chip
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
                    // Cyan chip for tool name
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = step.tool,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Arguments (collapsed JSON, expandable)
                if (step.args != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, top = 2.dp)
                            .clickable { argsExpanded = !argsExpanded },
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (argsExpanded) step.args else step.args.take(80) + if (step.args.length > 80) "..." else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (argsExpanded) Int.MAX_VALUE else 2
                        )
                        if (step.args.length > 80) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                if (argsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Result summary (green=success, red=error)
            if (step.result != null) {
                val isError = step.result.startsWith("Error") || step.result.startsWith("""{"error"""")
                Row(
                    modifier = Modifier.padding(start = 18.dp, top = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = step.result.take(200),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }
            }

            // Timestamp
            Text(
                text = formatDate(step.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(
        androidx.compose.foundation.clickable(onClick = onClick)
    )

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
