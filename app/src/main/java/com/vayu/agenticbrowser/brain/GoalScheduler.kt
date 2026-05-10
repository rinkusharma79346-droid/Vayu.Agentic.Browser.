package com.vayu.agenticbrowser.brain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class ScheduledGoal(
    val id: String,
    val goal: String,
    val scheduledAt: Long,
    val recurringIntervalMs: Long? = null,
    val completed: Boolean = false,
    val lastRunAt: Long? = null,
    val result: String? = null
)

class GoalScheduler private constructor() {

    private var context: Context? = null
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _scheduledGoals = MutableStateFlow<List<ScheduledGoal>>(emptyList())
    val scheduledGoals: StateFlow<List<ScheduledGoal>> = _scheduledGoals.asStateFlow()

    fun init(ctx: Context) {
        context = ctx.applicationContext
        loadGoals()
    }

    fun scheduleGoal(goal: String, scheduledAt: Long, recurringIntervalMs: Long? = null): ScheduledGoal {
        val scheduledGoal = ScheduledGoal(
            id = UUID.randomUUID().toString(),
            goal = goal,
            scheduledAt = scheduledAt,
            recurringIntervalMs = recurringIntervalMs
        )

        val goals = _scheduledGoals.value.toMutableList()
        goals.add(scheduledGoal)
        _scheduledGoals.value = goals
        saveGoals()

        // Set alarm
        setAlarm(scheduledGoal)

        Logger.i("GoalScheduler: Scheduled goal '$goal' at $scheduledAt")
        return scheduledGoal
    }

    fun cancelGoal(id: String) {
        val goals = _scheduledGoals.value.toMutableList()
        val goal = goals.find { it.id == id }

        if (goal != null) {
            cancelAlarm(goal)
            goals.remove(goal)
            _scheduledGoals.value = goals
            saveGoals()
            Logger.i("GoalScheduler: Cancelled goal $id")
        }
    }

    fun markCompleted(id: String, result: String) {
        val goals = _scheduledGoals.value.toMutableList()
        val index = goals.indexOfFirst { it.id == id }

        if (index != -1) {
            val goal = goals[index]
            if (goal.recurringIntervalMs != null) {
                // Reschedule recurring goal
                val nextRun = System.currentTimeMillis() + goal.recurringIntervalMs
                goals[index] = goal.copy(
                    lastRunAt = System.currentTimeMillis(),
                    result = result,
                    scheduledAt = nextRun
                )
                setAlarm(goals[index])
            } else {
                goals[index] = goal.copy(
                    completed = true,
                    lastRunAt = System.currentTimeMillis(),
                    result = result
                )
            }
            _scheduledGoals.value = goals
            saveGoals()
        }
    }

    fun listGoals(): List<ScheduledGoal> = _scheduledGoals.value

    fun deleteGoal(id: String) {
        val goals = _scheduledGoals.value.toMutableList()
        val goal = goals.find { it.id == id }
        if (goal != null) {
            cancelAlarm(goal)
            goals.remove(goal)
            _scheduledGoals.value = goals
            saveGoals()
        }
    }

    private fun setAlarm(goal: ScheduledGoal) {
        val ctx = context ?: return
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(ctx, GoalAlarmReceiver::class.java).apply {
            putExtra("goal_id", goal.id)
            putExtra("goal_text", goal.goal)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            goal.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            goal.scheduledAt,
            pendingIntent
        )

        Logger.i("GoalScheduler: Alarm set for goal ${goal.id} at ${goal.scheduledAt}")
    }

    private fun cancelAlarm(goal: ScheduledGoal) {
        val ctx = context ?: return
        val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(ctx, GoalAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            ctx,
            goal.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun loadGoals() {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("vayu_goal_scheduler", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("scheduled_goals", null) ?: return

        try {
            val goals = json.decodeFromString<List<ScheduledGoal>>(jsonStr)
            _scheduledGoals.value = goals.filter { !it.completed }
            Logger.i("GoalScheduler: Loaded ${goals.size} goals")
        } catch (e: Exception) {
            Logger.e("GoalScheduler: Failed to load goals", e)
        }
    }

    private fun saveGoals() {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences("vayu_goal_scheduler", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("scheduled_goals", json.encodeToString(_scheduledGoals.value))
            .apply()
    }

    companion object {
        @Volatile
        private var instance: GoalScheduler? = null

        fun getInstance(): GoalScheduler {
            return instance ?: synchronized(this) {
                instance ?: GoalScheduler().also { instance = it }
            }
        }
    }
}

class GoalAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalId = intent.getStringExtra("goal_id") ?: return
        val goalText = intent.getStringExtra("goal_text") ?: return

        Logger.i("GoalAlarmReceiver: Triggered goal $goalId: $goalText")

        // Start the foreground service which will trigger the agent loop
        val serviceIntent = Intent(context, com.vayu.agenticbrowser.McpForegroundService::class.java).apply {
            putExtra("trigger_goal", goalText)
            putExtra("goal_id", goalId)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
