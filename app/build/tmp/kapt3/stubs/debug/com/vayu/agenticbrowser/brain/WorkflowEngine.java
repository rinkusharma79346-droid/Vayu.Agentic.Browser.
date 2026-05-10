package com.vayu.agenticbrowser.brain;

import android.content.Context;
import com.vayu.agenticbrowser.common.Logger;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.Serializable;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000eJ\u0010\u0010\u0017\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0016\u001a\u00020\u000eJ\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\n0\tJ\u000e\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u0002J\b\u0010\u001a\u001a\u00020\u0015H\u0002J\b\u0010\u001b\u001a\u00020\u0015H\u0002J\u0016\u0010\u001c\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u000e\u0010\u001e\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020\nR\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u000eX\u0082D\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006 "}, d2 = {"Lcom/vayu/agenticbrowser/brain/WorkflowEngine;", "", "context", "Landroid/content/Context;", "agentLoop", "Lcom/vayu/agenticbrowser/brain/AgentLoop;", "(Landroid/content/Context;Lcom/vayu/agenticbrowser/brain/AgentLoop;)V", "_workflows", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/vayu/agenticbrowser/brain/Workflow;", "json", "Lkotlinx/serialization/json/Json;", "keyWorkflows", "", "prefsName", "workflows", "Lkotlinx/coroutines/flow/StateFlow;", "getWorkflows", "()Lkotlinx/coroutines/flow/StateFlow;", "deleteWorkflow", "", "id", "getWorkflow", "listWorkflows", "loadBuiltInWorkflows", "loadWorkflows", "persistWorkflows", "runWorkflow", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveWorkflow", "workflow", "app_debug"})
public final class WorkflowEngine {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.vayu.agenticbrowser.brain.AgentLoop agentLoop = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.serialization.json.Json json = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String prefsName = "vayu_workflows";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String keyWorkflows = "saved_workflows";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.vayu.agenticbrowser.brain.Workflow>> _workflows = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.brain.Workflow>> workflows = null;
    
    public WorkflowEngine(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.AgentLoop agentLoop) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vayu.agenticbrowser.brain.Workflow>> getWorkflows() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vayu.agenticbrowser.brain.Workflow> listWorkflows() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.vayu.agenticbrowser.brain.Workflow getWorkflow(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vayu.agenticbrowser.brain.Workflow saveWorkflow(@org.jetbrains.annotations.NotNull()
    com.vayu.agenticbrowser.brain.Workflow workflow) {
        return null;
    }
    
    public final void deleteWorkflow(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object runWorkflow(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final void loadWorkflows() {
    }
    
    private final java.util.List<com.vayu.agenticbrowser.brain.Workflow> loadBuiltInWorkflows() {
        return null;
    }
    
    private final void persistWorkflows() {
    }
}