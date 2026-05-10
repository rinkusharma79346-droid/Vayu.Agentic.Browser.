package com.vayu.agenticbrowser.di;

import android.content.Context;
import com.vayu.agenticbrowser.agent.McpServer;
import com.vayu.agenticbrowser.brain.AgentLoop;
import com.vayu.agenticbrowser.brain.WorkflowEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideWorkflowEngineFactory implements Factory<WorkflowEngine> {
  private final Provider<Context> ctxProvider;

  private final Provider<AgentLoop> agentLoopProvider;

  private final Provider<McpServer> mcpServerProvider;

  public AppModule_ProvideWorkflowEngineFactory(Provider<Context> ctxProvider,
      Provider<AgentLoop> agentLoopProvider, Provider<McpServer> mcpServerProvider) {
    this.ctxProvider = ctxProvider;
    this.agentLoopProvider = agentLoopProvider;
    this.mcpServerProvider = mcpServerProvider;
  }

  @Override
  public WorkflowEngine get() {
    return provideWorkflowEngine(ctxProvider.get(), agentLoopProvider.get(), mcpServerProvider.get());
  }

  public static AppModule_ProvideWorkflowEngineFactory create(Provider<Context> ctxProvider,
      Provider<AgentLoop> agentLoopProvider, Provider<McpServer> mcpServerProvider) {
    return new AppModule_ProvideWorkflowEngineFactory(ctxProvider, agentLoopProvider, mcpServerProvider);
  }

  public static WorkflowEngine provideWorkflowEngine(Context ctx, AgentLoop agentLoop,
      McpServer mcpServer) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWorkflowEngine(ctx, agentLoop, mcpServer));
  }
}
