package com.vayu.agenticbrowser.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vayu.agenticbrowser.brain.*
import com.vayu.agenticbrowser.ui.theme.*
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

    val workflowEngine = remember { WorkflowEngine(context, agentLoop) }
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

    // Pulse for active state
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // State color mapping
    val stateColor = when (state) {
        AgentState.IDLE -> VayuOnSurfaceDim
        AgentState.THINKING -> VayuViolet
        AgentState.EXECUTING -> VayuCyan
        AgentState.WAITING -> VayuTeal
        AgentState.COMPLETED -> VayuSuccess
        AgentState.FAILED -> VayuError
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = VayuViolet,
                            modifier = Modifier
                                .size(24.dp)
                                .let { if (state == AgentState.THINKING) it.alpha(thinkingAlpha) else it }
                        )
                        Text(
                            "VAYU Brain",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(VayuViolet, VayuCyan)
                                )
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VayuCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VayuSurfaceDark
                )
            )
        },
        containerColor = VayuSurfaceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ===== State Indicator — Immersive Card =====
            ImmersiveStateCard(state, currentGoal, totalSteps, totalTokens, elapsedSeconds, thinkingAlpha, pulseAlpha)

            // ===== Goal Input — Glowing Input Field =====
            OutlinedTextField(
                value = goalInput,
                onValueChange = { goalInput = it },
                label = { Text("Enter a goal for VAYU...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = state == AgentState.IDLE,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VayuViolet,
                    unfocusedBorderColor = VayuOnSurfaceDim,
                    focusedContainerColor = VayuSurfaceElevated,
                    unfocusedContainerColor = VayuSurfaceElevated,
                    cursorColor = VayuCyan,
                    focusedTextColor = VayuOnSurface,
                    unfocusedTextColor = VayuOnSurface,
                    focusedLabelColor = VayuViolet,
                    unfocusedLabelColor = VayuOnSurfaceVariant
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (state == AgentState.IDLE) VayuViolet else VayuOnSurfaceDim,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            // Run / Cancel buttons
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
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VayuViolet,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run Goal", fontWeight = FontWeight.SemiBold)
                }

                if (state != AgentState.IDLE) {
                    OutlinedButton(
                        onClick = { agentLoop.stop() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = VayuError
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VayuError.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ===== Workflow Picker =====
            ImmersiveSectionHeader(
                title = "Workflows",
                icon = Icons.Default.AutoAwesome,
                iconColor = VayuTeal,
                expanded = workflowPickerExpanded,
                onToggle = { workflowPickerExpanded = !workflowPickerExpanded }
            )

            AnimatedVisibility(visible = workflowPickerExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    workflows.forEach { workflow ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = VayuSurfaceCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (workflow.isBuiltIn) {
                                androidx.compose.foundation.BorderStroke(1.dp, VayuTeal.copy(alpha = 0.3f))
                            } else null
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
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = VayuOnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (workflow.isBuiltIn) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = VayuTeal.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "built-in",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VayuTeal,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = workflow.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VayuOnSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { goalInput = workflow.goalPrompt },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy to goal",
                                            modifier = Modifier.size(16.dp),
                                            tint = VayuCyan
                                        )
                                    }
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
                                            tint = VayuViolet
                                        )
                                    }
                                    if (!workflow.isBuiltIn) {
                                        IconButton(
                                            onClick = { workflowEngine.deleteWorkflow(workflow.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(16.dp),
                                                tint = VayuError
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = VayuOnSurfaceDim.copy(alpha = 0.3f))

            // ===== Live Step Log =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.ListAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = VayuCyan
                    )
                    Text(
                        text = "Step Log (${stepLog.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = VayuOnSurface
                    )
                }
                if (state == AgentState.THINKING) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(VayuViolet)
                                .alpha(thinkingAlpha)
                        )
                        Text(
                            text = "Thinking...",
                            style = MaterialTheme.typography.labelSmall,
                            color = VayuViolet,
                            modifier = Modifier.alpha(thinkingAlpha)
                        )
                    }
                }
            }

            if (stepLog.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = VayuOnSurfaceDim.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No steps yet. Enter a goal and click Run.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VayuOnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    state = listState
                ) {
                    items(stepLog) { step ->
                        ImmersiveStepCard(step)
                    }
                }
            }

            HorizontalDivider(color = VayuOnSurfaceDim.copy(alpha = 0.3f))

            // ===== Bottom Status Bar =====
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = VayuSurfaceCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // State indicator with color
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(stateColor)
                                .let { if (isRunning) it.alpha(pulseAlpha) else it }
                        )
                        Text(
                            text = state.name,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = stateColor
                        )
                    }

                    Text(
                        text = "Step $totalSteps / 50",
                        style = MaterialTheme.typography.labelSmall,
                        color = VayuOnSurfaceVariant
                    )

                    val minutes = elapsedSeconds / 60
                    val seconds = elapsedSeconds % 60
                    Text(
                        text = String.format("%d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.labelSmall,
                        color = VayuCyan
                    )

                    Text(
                        text = "${totalTokens} tokens",
                        style = MaterialTheme.typography.labelSmall,
                        color = VayuOnSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = VayuOnSurfaceDim.copy(alpha = 0.3f))

            // ===== Configuration Section =====
            ImmersiveSectionHeader(
                title = "Configuration",
                icon = Icons.Default.Settings,
                iconColor = VayuOnSurfaceVariant,
                expanded = configExpanded,
                onToggle = { configExpanded = !configExpanded }
            )

            AnimatedVisibility(visible = configExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Provider selection chips
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
                                label = { Text(p.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VayuViolet.copy(alpha = 0.2f),
                                    selectedLabelColor = VayuViolet
                                )
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
                                    contentDescription = "Toggle",
                                    tint = VayuCyan
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VayuViolet,
                            unfocusedBorderColor = VayuOnSurfaceDim,
                            focusedContainerColor = VayuSurfaceElevated,
                            unfocusedContainerColor = VayuSurfaceElevated,
                            cursorColor = VayuCyan
                        )
                    )

                    if (provider == LlmProvider.CUSTOM) {
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VayuViolet,
                                unfocusedBorderColor = VayuOnSurfaceDim,
                                focusedContainerColor = VayuSurfaceElevated,
                                unfocusedContainerColor = VayuSurfaceElevated,
                                cursorColor = VayuCyan
                            )
                        )
                    }

                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VayuViolet,
                            unfocusedBorderColor = VayuOnSurfaceDim,
                            focusedContainerColor = VayuSurfaceElevated,
                            unfocusedContainerColor = VayuSurfaceElevated,
                            cursorColor = VayuCyan
                        )
                    )

                    OutlinedTextField(
                        value = maxTokens,
                        onValueChange = { maxTokens = it },
                        label = { Text("Max Tokens") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VayuViolet,
                            unfocusedBorderColor = VayuOnSurfaceDim,
                            focusedContainerColor = VayuSurfaceElevated,
                            unfocusedContainerColor = VayuSurfaceElevated,
                            cursorColor = VayuCyan
                        )
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VayuViolet,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Configuration", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HorizontalDivider(color = VayuOnSurfaceDim.copy(alpha = 0.3f))

            // ===== Schedule Section =====
            ImmersiveSectionHeader(
                title = "Scheduled Goals",
                icon = Icons.Default.Schedule,
                iconColor = VayuTeal,
                expanded = scheduleExpanded,
                onToggle = { scheduleExpanded = !scheduleExpanded }
            )

            AnimatedVisibility(visible = scheduleExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scheduleGoalText,
                        onValueChange = { scheduleGoalText = it },
                        label = { Text("Goal to schedule") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VayuTeal,
                            unfocusedBorderColor = VayuOnSurfaceDim,
                            focusedContainerColor = VayuSurfaceElevated,
                            unfocusedContainerColor = VayuSurfaceElevated,
                            cursorColor = VayuCyan
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = scheduleDelayMinutes,
                            onValueChange = { scheduleDelayMinutes = it },
                            label = { Text("Delay (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VayuTeal,
                                unfocusedBorderColor = VayuOnSurfaceDim,
                                focusedContainerColor = VayuSurfaceElevated,
                                unfocusedContainerColor = VayuSurfaceElevated,
                                cursorColor = VayuCyan
                            )
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
                            enabled = scheduleGoalText.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VayuTeal,
                                contentColor = VayuNavy
                            )
                        ) {
                            Text("Schedule", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    scheduledGoals.filter { !it.completed }.forEach { goal ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = VayuSurfaceCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goal.goal,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VayuOnSurface,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "Run at: ${formatDate(goal.scheduledAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VayuTeal
                                    )
                                }
                                IconButton(onClick = { goalScheduler.deleteGoal(goal.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                                        tint = VayuError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ImmersiveStateCard(
    state: AgentState,
    currentGoal: String?,
    totalSteps: Int,
    totalTokens: Int,
    elapsedSeconds: Int,
    thinkingAlpha: Float,
    pulseAlpha: Float
) {
    val stateColor = when (state) {
        AgentState.IDLE -> VayuOnSurfaceDim
        AgentState.THINKING -> VayuViolet
        AgentState.EXECUTING -> VayuCyan
        AgentState.WAITING -> VayuTeal
        AgentState.COMPLETED -> VayuSuccess
        AgentState.FAILED -> VayuError
    }

    val (icon, label) = when (state) {
        AgentState.IDLE -> Icons.Default.HourglassEmpty to "Idle"
        AgentState.THINKING -> Icons.Default.Psychology to "Thinking..."
        AgentState.EXECUTING -> Icons.Default.Build to "Executing..."
        AgentState.WAITING -> Icons.Default.HourglassTop to "Waiting..."
        AgentState.COMPLETED -> Icons.Default.CheckCircle to "Completed"
        AgentState.FAILED -> Icons.Default.Error to "Failed"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = stateColor.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, stateColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(stateColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .let { if (state == AgentState.THINKING) it.alpha(thinkingAlpha) else it },
                        tint = stateColor
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                )
            }

            if (currentGoal != null) {
                Text(
                    text = currentGoal.take(100) + if (currentGoal.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = VayuOnSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 6.dp, start = 40.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusBadge("Steps", "$totalSteps/50", VayuCyan)
                StatusBadge("Tokens", "$totalTokens", VayuTeal)
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                StatusBadge("Time", String.format("%d:%02d", minutes, seconds), VayuViolet)
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VayuOnSurfaceDim,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ImmersiveStepCard(step: AgentStep) {
    var argsExpanded by remember { mutableStateOf(false) }

    val cardColor = when {
        step.result != null && step.result.startsWith("Error") -> VayuError.copy(alpha = 0.08f)
        step.tool != null -> VayuCyan.copy(alpha = 0.06f)
        step.thought != null -> VayuViolet.copy(alpha = 0.06f)
        else -> VayuSurfaceCard
    }

    val borderColor = when {
        step.result != null && step.result.startsWith("Error") -> VayuError.copy(alpha = 0.2f)
        step.tool != null -> VayuCyan.copy(alpha = 0.15f)
        step.thought != null -> VayuViolet.copy(alpha = 0.15f)
        else -> VayuOnSurfaceDim.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "#${step.index}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = VayuOnSurfaceDim
                )

                if (step.thought != null) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = VayuViolet
                    )
                    Text(
                        text = step.thought,
                        style = MaterialTheme.typography.bodySmall,
                        color = VayuOnSurface,
                        maxLines = if (argsExpanded) Int.MAX_VALUE else 2,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (step.tool != null) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = VayuCyan
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = VayuCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = step.tool,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = VayuCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Arguments (collapsed JSON, expandable)
            if (step.args != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 2.dp)
                        .clickable { argsExpanded = !argsExpanded },
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (argsExpanded) step.args else step.args.take(80) + if (step.args.length > 80) "..." else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = VayuOnSurfaceVariant,
                        maxLines = if (argsExpanded) Int.MAX_VALUE else 2
                    )
                    if (step.args.length > 80) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            if (argsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = VayuOnSurfaceVariant
                        )
                    }
                }
            }

            // Result summary
            if (step.result != null) {
                val isError = step.result.startsWith("Error") || step.result.startsWith("""{"error"""")
                Row(
                    modifier = Modifier.padding(start = 24.dp, top = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (isError) VayuError else VayuSuccess
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = step.result.take(200),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isError) VayuError else VayuOnSurfaceVariant,
                        maxLines = 3
                    )
                }
            }

            // Timestamp
            Text(
                text = formatDate(step.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = VayuOnSurfaceDim,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ImmersiveSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = VayuOnSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = VayuOnSurfaceVariant
        )
    }
}



private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
