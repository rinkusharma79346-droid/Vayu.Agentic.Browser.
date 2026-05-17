package com.vayu.agenticbrowser.brain;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.serialization.Serializable;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\t\n\u0002\b\u0005\b\u0007\u0018\u0000 #2\u00020\u0001:\u0001#B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0006H\u0002J\u000e\u0010\u0012\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0016\u001a\u00020\u00102\u0006\u0010\u0017\u001a\u00020\bJ\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005J\b\u0010\u0019\u001a\u00020\u0010H\u0002J\u0016\u0010\u001a\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u0014J\b\u0010\u001c\u001a\u00020\u0010H\u0002J\'\u0010\u001d\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u00142\u0006\u0010\u001e\u001a\u00020\u001f2\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u001f\u00a2\u0006\u0002\u0010!J\u0010\u0010\"\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0006H\u0002R\u001a\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006$"}, d2 = {"Lcom/vayu/agenticbrowser/brain/GoalScheduler;", "", "()V", "_scheduledGoals", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/vayu/agenticbrowser/brain/ScheduledGoal;", "context", "Landroid/content/Context;", "json", "Lkotlinx/serialization/json/Json;", "scheduledGoals", "Lkotlinx/coroutines/flow/StateFlow;", "getScheduledGoals", "()Lkotlinx/coroutines/flow/StateFlow;", "cancelAlarm", "", "goal", "cancelGoal", "id", "", "deleteGoal", "init", "ctx", "listGoals", "loadGoals", "markCompleted", "result", "saveGoals", "scheduleGoal", "scheduledAt", "", "recurringIntervalMs", "(Ljava/lang/String;JLjava/lang/Long;)Lcom/vayu/agenticbrowser/brain/ScheduledGoal;", "setAlarm", "Companion", "app_debug"})
public final class GoalScheduler {
    @org.jetbrains.annotations.Nullable()
    private android.content.Context context;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.vayu.agenticbrowser.brain.ScheduledGoal>> _scheduledGoals = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.brain.ScheduledGoal>> scheduledGoals = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.vayu.agenticbrowser.brain.GoalScheduler instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.brain.GoalScheduler.Companion Companion = null;
    
    private GoalScheduler() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.brain.ScheduledGoal>> getScheduledGoals() {
        return null;
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.ScheduledGoal scheduleGoal(@org.jetbrains.annotations.NotNull()
    java.lang.String goal, long scheduledAt, @org.jetbrains.annotations.Nullable()
    java.lang.Long recurringIntervalMs) {
        return null;
    }
    
    public final void cancelGoal(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    public final void markCompleted(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String result) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.brain.ScheduledGoal> listGoals() {
        return null;
    }
    
    public final void deleteGoal(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    private final void setAlarm(com.vayu.agenticbrowser.brain.ScheduledGoal goal) {
    }
    
    private final void cancelAlarm(com.vayu.agenticbrowser.brain.ScheduledGoal goal) {
    }
    
    private final void loadGoals() {
    }
    
    private final void saveGoals() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/vayu/agenticbrowser/brain/GoalScheduler$Companion;", "", "()V", "instance", "Lcom/vayu/agenticbrowser/brain/GoalScheduler;", "getInstance", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.vayu.agenticbrowser.brain.GoalScheduler getInstance() {
            return null;
        }
    }
}