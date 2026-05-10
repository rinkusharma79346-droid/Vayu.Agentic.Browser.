package com.vayu.agenticbrowser;

import com.vayu.agenticbrowser.agent.McpServer;
import com.vayu.agenticbrowser.agent.SessionRecorder;
import com.vayu.agenticbrowser.brain.AgentLoop;
import com.vayu.agenticbrowser.brain.GoalScheduler;
import com.vayu.agenticbrowser.brain.WorkflowEngine;
import com.vayu.agenticbrowser.common.NetworkMonitor;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.plugins.PluginRegistry;
import com.vayu.agenticbrowser.tabs.TabManager;
import com.vayu.agenticbrowser.tunnel.TunnelManager;
import com.vayu.agenticbrowser.vault.ProfileManager;
import com.vayu.agenticbrowser.vault.SmsOtpReader;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<McpServer> mcpServerProvider;

  private final Provider<TabManager> tabManagerProvider;

  private final Provider<VayuDownloadManager> downloadManagerProvider;

  private final Provider<ProfileManager> profileManagerProvider;

  private final Provider<SmsOtpReader> smsOtpReaderProvider;

  private final Provider<PluginRegistry> pluginRegistryProvider;

  private final Provider<TunnelManager> tunnelManagerProvider;

  private final Provider<SessionRecorder> sessionRecorderProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  private final Provider<AgentLoop> agentLoopProvider;

  private final Provider<GoalScheduler> goalSchedulerProvider;

  private final Provider<WorkflowEngine> workflowEngineProvider;

  public MainActivity_MembersInjector(Provider<McpServer> mcpServerProvider,
      Provider<TabManager> tabManagerProvider,
      Provider<VayuDownloadManager> downloadManagerProvider,
      Provider<ProfileManager> profileManagerProvider, Provider<SmsOtpReader> smsOtpReaderProvider,
      Provider<PluginRegistry> pluginRegistryProvider,
      Provider<TunnelManager> tunnelManagerProvider,
      Provider<SessionRecorder> sessionRecorderProvider,
      Provider<NetworkMonitor> networkMonitorProvider, Provider<AgentLoop> agentLoopProvider,
      Provider<GoalScheduler> goalSchedulerProvider,
      Provider<WorkflowEngine> workflowEngineProvider) {
    this.mcpServerProvider = mcpServerProvider;
    this.tabManagerProvider = tabManagerProvider;
    this.downloadManagerProvider = downloadManagerProvider;
    this.profileManagerProvider = profileManagerProvider;
    this.smsOtpReaderProvider = smsOtpReaderProvider;
    this.pluginRegistryProvider = pluginRegistryProvider;
    this.tunnelManagerProvider = tunnelManagerProvider;
    this.sessionRecorderProvider = sessionRecorderProvider;
    this.networkMonitorProvider = networkMonitorProvider;
    this.agentLoopProvider = agentLoopProvider;
    this.goalSchedulerProvider = goalSchedulerProvider;
    this.workflowEngineProvider = workflowEngineProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<McpServer> mcpServerProvider,
      Provider<TabManager> tabManagerProvider,
      Provider<VayuDownloadManager> downloadManagerProvider,
      Provider<ProfileManager> profileManagerProvider, Provider<SmsOtpReader> smsOtpReaderProvider,
      Provider<PluginRegistry> pluginRegistryProvider,
      Provider<TunnelManager> tunnelManagerProvider,
      Provider<SessionRecorder> sessionRecorderProvider,
      Provider<NetworkMonitor> networkMonitorProvider, Provider<AgentLoop> agentLoopProvider,
      Provider<GoalScheduler> goalSchedulerProvider,
      Provider<WorkflowEngine> workflowEngineProvider) {
    return new MainActivity_MembersInjector(mcpServerProvider, tabManagerProvider, downloadManagerProvider, profileManagerProvider, smsOtpReaderProvider, pluginRegistryProvider, tunnelManagerProvider, sessionRecorderProvider, networkMonitorProvider, agentLoopProvider, goalSchedulerProvider, workflowEngineProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectMcpServer(instance, mcpServerProvider.get());
    injectTabManager(instance, tabManagerProvider.get());
    injectDownloadManager(instance, downloadManagerProvider.get());
    injectProfileManager(instance, profileManagerProvider.get());
    injectSmsOtpReader(instance, smsOtpReaderProvider.get());
    injectPluginRegistry(instance, pluginRegistryProvider.get());
    injectTunnelManager(instance, tunnelManagerProvider.get());
    injectSessionRecorder(instance, sessionRecorderProvider.get());
    injectNetworkMonitor(instance, networkMonitorProvider.get());
    injectAgentLoop(instance, agentLoopProvider.get());
    injectGoalScheduler(instance, goalSchedulerProvider.get());
    injectWorkflowEngine(instance, workflowEngineProvider.get());
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.mcpServer")
  public static void injectMcpServer(MainActivity instance, McpServer mcpServer) {
    instance.mcpServer = mcpServer;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.tabManager")
  public static void injectTabManager(MainActivity instance, TabManager tabManager) {
    instance.tabManager = tabManager;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.downloadManager")
  public static void injectDownloadManager(MainActivity instance,
      VayuDownloadManager downloadManager) {
    instance.downloadManager = downloadManager;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.profileManager")
  public static void injectProfileManager(MainActivity instance, ProfileManager profileManager) {
    instance.profileManager = profileManager;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.smsOtpReader")
  public static void injectSmsOtpReader(MainActivity instance, SmsOtpReader smsOtpReader) {
    instance.smsOtpReader = smsOtpReader;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.pluginRegistry")
  public static void injectPluginRegistry(MainActivity instance, PluginRegistry pluginRegistry) {
    instance.pluginRegistry = pluginRegistry;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.tunnelManager")
  public static void injectTunnelManager(MainActivity instance, TunnelManager tunnelManager) {
    instance.tunnelManager = tunnelManager;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.sessionRecorder")
  public static void injectSessionRecorder(MainActivity instance, SessionRecorder sessionRecorder) {
    instance.sessionRecorder = sessionRecorder;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.networkMonitor")
  public static void injectNetworkMonitor(MainActivity instance, NetworkMonitor networkMonitor) {
    instance.networkMonitor = networkMonitor;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.agentLoop")
  public static void injectAgentLoop(MainActivity instance, AgentLoop agentLoop) {
    instance.agentLoop = agentLoop;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.goalScheduler")
  public static void injectGoalScheduler(MainActivity instance, GoalScheduler goalScheduler) {
    instance.goalScheduler = goalScheduler;
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.MainActivity.workflowEngine")
  public static void injectWorkflowEngine(MainActivity instance, WorkflowEngine workflowEngine) {
    instance.workflowEngine = workflowEngine;
  }
}
