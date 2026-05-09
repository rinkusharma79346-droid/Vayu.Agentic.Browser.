package com.vayu.agenticbrowser.agent

import android.content.Context
import com.vayu.agenticbrowser.common.Logger
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SessionRecorder private constructor() {

    private var context: Context? = null
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val prefsName = "vayu_session_recordings"
    private val keyRecordings = "recordings_json"

    private var currentRecording: MutableList<RecordedCommand>? = null
    private var currentRecordingName: String? = null
    private var recordingStartTime: Long = 0L

    private val _isRecording = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isRecording: kotlinx.coroutines.flow.StateFlow<Boolean> = _isRecording

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun startRecording(name: String) {
        currentRecordingName = name
        currentRecording = mutableListOf()
        recordingStartTime = System.currentTimeMillis()
        _isRecording.value = true
        Logger.i("Started recording: $name")
    }

    fun stopRecording(): Recording {
        val commands = currentRecording?.toList() ?: emptyList()
        val name = currentRecordingName ?: "Untitled"
        val recording = Recording(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            commands = commands,
            createdAt = System.currentTimeMillis()
        )

        _isRecording.value = false
        currentRecording = null
        currentRecordingName = null

        saveRecording(recording)
        Logger.i("Stopped recording: $name with ${commands.size} commands")
        return recording
    }

    fun recordCommand(tool: String, args: Map<String, String>) {
        if (!_isRecording.value) return
        currentRecording?.add(
            RecordedCommand(
                tool = tool,
                args = args,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun listRecordings(): List<Recording> {
        val ctx = context ?: return emptyList()
        val prefs = ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(keyRecordings, null) ?: return emptyList()

        return try {
            json.decodeFromString<List<Recording>>(jsonStr)
        } catch (e: Exception) {
            Logger.e("Failed to load recordings", e)
            emptyList()
        }
    }

    fun deleteRecording(id: String) {
        val recordings = listRecordings().toMutableList()
        recordings.removeAll { it.id == id }
        saveRecordingsList(recordings)
        Logger.i("Deleted recording: $id")
    }

    suspend fun replayRecording(
        id: String,
        toolExecutor: suspend (tool: String, args: Map<String, String>) -> String
    ): String {
        val recording = listRecordings().find { it.id == id }
            ?: return """{"error":"Recording $id not found"}"""

        val results = mutableListOf<ReplayResult>()
        var lastTimestamp = 0L

        for (command in recording.commands) {
            if (lastTimestamp > 0L) {
                val gap = command.timestamp - lastTimestamp
                val cappedGap = gap.coerceAtMost(2_000L)
                if (cappedGap > 0) {
                    delay(cappedGap)
                }
            }

            val result = try {
                toolExecutor(command.tool, command.args)
            } catch (e: Exception) {
                """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
            }

            results.add(
                ReplayResult(
                    tool = command.tool,
                    args = command.args,
                    result = result,
                    success = !result.contains("\"error\"")
                )
            )

            lastTimestamp = command.timestamp
        }

        val summary = ReplaySummary(
            recordingId = id,
            recordingName = recording.name,
            totalCommands = recording.commands.size,
            successfulCommands = results.count { it.success },
            failedCommands = results.count { !it.success },
            results = results
        )

        return json.encodeToString(summary)
    }

    private fun saveRecording(recording: Recording) {
        val recordings = listRecordings().toMutableList()
        recordings.add(recording)
        saveRecordingsList(recordings)
    }

    private fun saveRecordingsList(recordings: List<Recording>) {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(keyRecordings, json.encodeToString(recordings))
            .apply()
    }

    @Serializable
    data class RecordedCommand(
        val tool: String,
        val args: Map<String, String>,
        val timestamp: Long
    )

    @Serializable
    data class Recording(
        val id: String,
        val name: String,
        val commands: List<RecordedCommand>,
        val createdAt: Long
    )

    @Serializable
    data class ReplayResult(
        val tool: String,
        val args: Map<String, String>,
        val result: String,
        val success: Boolean
    )

    @Serializable
    data class ReplaySummary(
        val recordingId: String,
        val recordingName: String,
        val totalCommands: Int,
        val successfulCommands: Int,
        val failedCommands: Int,
        val results: List<ReplayResult>
    )

    companion object {
        @Volatile
        private var instance: SessionRecorder? = null

        fun getInstance(): SessionRecorder {
            return instance ?: synchronized(this) {
                instance ?: SessionRecorder().also { instance = it }
            }
        }
    }
}
