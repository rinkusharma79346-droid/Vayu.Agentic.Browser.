package com.vayu.agenticbrowser.brain;

import android.content.Context;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.Serializable;
import com.vayu.agenticbrowser.plugins.PluginRegistry;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008a\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001Bq\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012R\u0010\b\u001aN\b\u0001\u0012\u0013\u0012\u00110\n\u00a2\u0006\f\b\u000b\u0012\b\b\f\u0012\u0004\b\b(\r\u0012\u001f\u0012\u001d\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\u000e\u00a2\u0006\f\b\u000b\u0012\b\b\f\u0012\u0004\b\b(\u000f\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u00010\t\u00a2\u0006\u0002\u0010\u0011J\u0010\u00100\u001a\u0002012\u0006\u00102\u001a\u00020\u0018H\u0002J\u0016\u00103\u001a\u0002012\u0006\u00104\u001a\u00020\nH\u0082@\u00a2\u0006\u0002\u00105J\u0006\u00106\u001a\u00020\u001dJ\u0012\u00107\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\u000eJ\b\u00108\u001a\u000209H\u0002J\u001c\u0010:\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\u000e2\u0006\u0010;\u001a\u00020\nH\u0002J\u000e\u0010<\u001a\u0002012\u0006\u00104\u001a\u00020\nJ\u0006\u0010=\u001a\u000201J\u000e\u0010>\u001a\u0002012\u0006\u0010?\u001a\u00020\u001dJ\u001c\u0010@\u001a\u0002012\u0012\u0010A\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00180BH\u0002R\u0016\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\u00170\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0010\u0010\"\u001a\u0004\u0018\u00010#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020%X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\u001aX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00150\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010!R\u001d\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\u00170\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010!R\\\u0010\b\u001aN\b\u0001\u0012\u0013\u0012\u00110\n\u00a2\u0006\f\b\u000b\u0012\b\b\f\u0012\u0004\b\b(\r\u0012\u001f\u0012\u001d\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\n0\u000e\u00a2\u0006\f\b\u000b\u0012\b\b\f\u0012\u0004\b\b(\u000f\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u00010\tX\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010+R\u0017\u0010,\u001a\b\u0012\u0004\u0012\u00020\u001a0\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010!R\u0017\u0010.\u001a\b\u0012\u0004\u0012\u00020\u001a0\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010!\u00a8\u0006C"}, d2 = {"Lcom/vayu/agenticbrowser/brain/AgentLoop;", "", "context", "Landroid/content/Context;", "brainClient", "Lcom/vayu/agenticbrowser/brain/BrainClient;", "pluginRegistry", "Lcom/vayu/agenticbrowser/plugins/PluginRegistry;", "toolExecutor", "Lkotlin/Function3;", "", "Lkotlin/ParameterName;", "name", "tool", "", "args", "Lkotlin/coroutines/Continuation;", "(Landroid/content/Context;Lcom/vayu/agenticbrowser/brain/BrainClient;Lcom/vayu/agenticbrowser/plugins/PluginRegistry;Lkotlin/jvm/functions/Function3;)V", "_currentGoal", "Lkotlinx/coroutines/flow/MutableStateFlow;", "_state", "Lcom/vayu/agenticbrowser/brain/AgentState;", "_stepLog", "", "Lcom/vayu/agenticbrowser/brain/AgentStep;", "_totalSteps", "", "_totalTokens", "config", "Lcom/vayu/agenticbrowser/brain/BrainConfig;", "currentGoal", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentGoal", "()Lkotlinx/coroutines/flow/StateFlow;", "job", "Lkotlinx/coroutines/Job;", "json", "Lkotlinx/serialization/json/Json;", "maxIterations", "state", "getState", "stepLog", "getStepLog", "Lkotlin/jvm/functions/Function3;", "totalSteps", "getTotalSteps", "totalTokens", "getTotalTokens", "addStep", "", "step", "executeGoal", "goal", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getConfig", "getStats", "isActive", "", "parseArguments", "argsJson", "runGoal", "stop", "updateConfig", "newConfig", "updateLastStep", "transform", "Lkotlin/Function1;", "app_debug"})
public final class AgentLoop {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.vayu.agenticbrowser.brain.BrainClient brainClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.vayu.agenticbrowser.plugins.PluginRegistry pluginRegistry = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function3<java.lang.String, java.util.Map<java.lang.String, java.lang.String>, kotlin.coroutines.Continuation<? super java.lang.String>, java.lang.Object> toolExecutor = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.vayu.agenticbrowser.brain.AgentState> _state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.vayu.agenticbrowser.brain.AgentState> state = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _currentGoal = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> currentGoal = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.vayu.agenticbrowser.brain.AgentStep>> _stepLog = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.brain.AgentStep>> stepLog = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _totalTokens = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> totalTokens = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _totalSteps = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> totalSteps = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job job;
    @org.jetbrains.annotations.NotNull()
    private com.vayu.agenticbrowser.brain.BrainConfig config;
    private final int maxIterations = 50;
    
    public AgentLoop(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.BrainClient brainClient, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.plugins.PluginRegistry pluginRegistry, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function3<? super java.lang.String, ? super java.util.Map<java.lang.String, java.lang.String>, ? super kotlin.coroutines.Continuation<? super java.lang.String>, ? extends java.lang.Object> toolExecutor) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.vayu.agenticbrowser.brain.AgentState> getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getCurrentGoal() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.brain.AgentStep>> getStepLog() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getTotalTokens() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getTotalSteps() {
        return null;
    }
    
    public final void updateConfig(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.BrainConfig newConfig) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.BrainConfig getConfig() {
        return null;
    }
    
    public final void runGoal(@org.jetbrains.annotations.NotNull()
    java.lang.String goal) {
    }
    
    public final void stop() {
    }
    
    private final java.lang.Object executeGoal(java.lang.String goal, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.util.Map<java.lang.String, java.lang.String> parseArguments(java.lang.String argsJson) {
        return null;
    }
    
    private final boolean isActive() {
        return false;
    }
    
    private final void addStep(com.vayu.agenticbrowser.brain.AgentStep step) {
    }
    
    private final void updateLastStep(kotlin.jvm.functions.Function1<? super com.vayu.agenticbrowser.brain.AgentStep, com.vayu.agenticbrowser.brain.AgentStep> transform) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Object> getStats() {
        return null;
    }
}