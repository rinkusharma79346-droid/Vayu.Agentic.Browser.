package com.vayu.agenticbrowser;

import com.vayu.agenticbrowser.agent.McpServer;
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
public final class McpForegroundService_MembersInjector implements MembersInjector<McpForegroundService> {
  private final Provider<McpServer> mcpServerProvider;

  public McpForegroundService_MembersInjector(Provider<McpServer> mcpServerProvider) {
    this.mcpServerProvider = mcpServerProvider;
  }

  public static MembersInjector<McpForegroundService> create(
      Provider<McpServer> mcpServerProvider) {
    return new McpForegroundService_MembersInjector(mcpServerProvider);
  }

  @Override
  public void injectMembers(McpForegroundService instance) {
    injectMcpServer(instance, mcpServerProvider.get());
  }

  @InjectedFieldSignature("com.vayu.agenticbrowser.McpForegroundService.mcpServer")
  public static void injectMcpServer(McpForegroundService instance, McpServer mcpServer) {
    instance.mcpServer = mcpServer;
  }
}
