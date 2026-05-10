package com.vayu.agenticbrowser.di;

import android.content.Context;
import com.vayu.agenticbrowser.agent.McpServer;
import com.vayu.agenticbrowser.brain.AgentLoop;
import com.vayu.agenticbrowser.brain.BrainClient;
import com.vayu.agenticbrowser.brain.GoalScheduler;
import com.vayu.agenticbrowser.plugins.PluginRegistry;
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
public final class AppModule_ProvideAgentLoopFactory implements Factory<AgentLoop> {
  private final Provider<Context> ctxProvider;

  private final Provider<BrainClient> brainClientProvider;

  private final Provider<PluginRegistry> pluginRegistryProvider;

  private final Provider<McpServer> mcpServerProvider;

  private final Provider<GoalScheduler> goalSchedulerProvider;

  public AppModule_ProvideAgentLoopFactory(Provider<Context> ctxProvider,
      Provider<BrainClient> brainClientProvider, Provider<PluginRegistry> pluginRegistryProvider,
      Provider<McpServer> mcpServerProvider, Provider<GoalScheduler> goalSchedulerProvider) {
    this.ctxProvider = ctxProvider;
    this.brainClientProvider = brainClientProvider;
    this.pluginRegistryProvider = pluginRegistryProvider;
    this.mcpServerProvider = mcpServerProvider;
    this.goalSchedulerProvider = goalSchedulerProvider;
  }

  @Override
  public AgentLoop get() {
    return provideAgentLoop(ctxProvider.get(), brainClientProvider.get(), pluginRegistryProvider.get(), mcpServerProvider.get(), goalSchedulerProvider.get());
  }

  public static AppModule_ProvideAgentLoopFactory create(Provider<Context> ctxProvider,
      Provider<BrainClient> brainClientProvider, Provider<PluginRegistry> pluginRegistryProvider,
      Provider<McpServer> mcpServerProvider, Provider<GoalScheduler> goalSchedulerProvider) {
    return new AppModule_ProvideAgentLoopFactory(ctxProvider, brainClientProvider, pluginRegistryProvider, mcpServerProvider, goalSchedulerProvider);
  }

  public static AgentLoop provideAgentLoop(Context ctx, BrainClient brainClient,
      PluginRegistry pluginRegistry, McpServer mcpServer, GoalScheduler goalScheduler) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAgentLoop(ctx, brainClient, pluginRegistry, mcpServer, goalScheduler));
  }
}
