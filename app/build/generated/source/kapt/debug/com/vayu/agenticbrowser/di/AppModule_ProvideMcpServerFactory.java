package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.agent.McpServer;
import com.vayu.agenticbrowser.agent.SessionRecorder;
import com.vayu.agenticbrowser.common.NetworkMonitor;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.engine.DialogController;
import com.vayu.agenticbrowser.engine.DomController;
import com.vayu.agenticbrowser.engine.FormDetector;
import com.vayu.agenticbrowser.engine.WaitController;
import com.vayu.agenticbrowser.plugins.PluginRegistry;
import com.vayu.agenticbrowser.tabs.TabManager;
import com.vayu.agenticbrowser.tunnel.TunnelManager;
import com.vayu.agenticbrowser.vault.BiometricAuth;
import com.vayu.agenticbrowser.vault.CredentialVault;
import com.vayu.agenticbrowser.vault.ProfileManager;
import com.vayu.agenticbrowser.vault.SmsOtpReader;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideMcpServerFactory implements Factory<McpServer> {
  private final Provider<DomController> domControllerProvider;

  private final Provider<TabManager> tabManagerProvider;

  private final Provider<VayuDownloadManager> downloadManagerProvider;

  private final Provider<WaitController> waitControllerProvider;

  private final Provider<CredentialVault> credentialVaultProvider;

  private final Provider<ProfileManager> profileManagerProvider;

  private final Provider<BiometricAuth> biometricAuthProvider;

  private final Provider<FormDetector> formDetectorProvider;

  private final Provider<DialogController> dialogControllerProvider;

  private final Provider<SmsOtpReader> smsOtpReaderProvider;

  private final Provider<PluginRegistry> pluginRegistryProvider;

  private final Provider<TunnelManager> tunnelManagerProvider;

  private final Provider<SessionRecorder> sessionRecorderProvider;

  private final Provider<NetworkMonitor> networkMonitorProvider;

  public AppModule_ProvideMcpServerFactory(Provider<DomController> domControllerProvider,
      Provider<TabManager> tabManagerProvider,
      Provider<VayuDownloadManager> downloadManagerProvider,
      Provider<WaitController> waitControllerProvider,
      Provider<CredentialVault> credentialVaultProvider,
      Provider<ProfileManager> profileManagerProvider,
      Provider<BiometricAuth> biometricAuthProvider, Provider<FormDetector> formDetectorProvider,
      Provider<DialogController> dialogControllerProvider,
      Provider<SmsOtpReader> smsOtpReaderProvider, Provider<PluginRegistry> pluginRegistryProvider,
      Provider<TunnelManager> tunnelManagerProvider,
      Provider<SessionRecorder> sessionRecorderProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    this.domControllerProvider = domControllerProvider;
    this.tabManagerProvider = tabManagerProvider;
    this.downloadManagerProvider = downloadManagerProvider;
    this.waitControllerProvider = waitControllerProvider;
    this.credentialVaultProvider = credentialVaultProvider;
    this.profileManagerProvider = profileManagerProvider;
    this.biometricAuthProvider = biometricAuthProvider;
    this.formDetectorProvider = formDetectorProvider;
    this.dialogControllerProvider = dialogControllerProvider;
    this.smsOtpReaderProvider = smsOtpReaderProvider;
    this.pluginRegistryProvider = pluginRegistryProvider;
    this.tunnelManagerProvider = tunnelManagerProvider;
    this.sessionRecorderProvider = sessionRecorderProvider;
    this.networkMonitorProvider = networkMonitorProvider;
  }

  @Override
  public McpServer get() {
    return provideMcpServer(domControllerProvider.get(), tabManagerProvider.get(), downloadManagerProvider.get(), waitControllerProvider.get(), credentialVaultProvider.get(), profileManagerProvider.get(), biometricAuthProvider.get(), formDetectorProvider.get(), dialogControllerProvider.get(), smsOtpReaderProvider.get(), pluginRegistryProvider.get(), tunnelManagerProvider.get(), sessionRecorderProvider.get(), networkMonitorProvider.get());
  }

  public static AppModule_ProvideMcpServerFactory create(
      Provider<DomController> domControllerProvider, Provider<TabManager> tabManagerProvider,
      Provider<VayuDownloadManager> downloadManagerProvider,
      Provider<WaitController> waitControllerProvider,
      Provider<CredentialVault> credentialVaultProvider,
      Provider<ProfileManager> profileManagerProvider,
      Provider<BiometricAuth> biometricAuthProvider, Provider<FormDetector> formDetectorProvider,
      Provider<DialogController> dialogControllerProvider,
      Provider<SmsOtpReader> smsOtpReaderProvider, Provider<PluginRegistry> pluginRegistryProvider,
      Provider<TunnelManager> tunnelManagerProvider,
      Provider<SessionRecorder> sessionRecorderProvider,
      Provider<NetworkMonitor> networkMonitorProvider) {
    return new AppModule_ProvideMcpServerFactory(domControllerProvider, tabManagerProvider, downloadManagerProvider, waitControllerProvider, credentialVaultProvider, profileManagerProvider, biometricAuthProvider, formDetectorProvider, dialogControllerProvider, smsOtpReaderProvider, pluginRegistryProvider, tunnelManagerProvider, sessionRecorderProvider, networkMonitorProvider);
  }

  public static McpServer provideMcpServer(DomController domController, TabManager tabManager,
      VayuDownloadManager downloadManager, WaitController waitController,
      CredentialVault credentialVault, ProfileManager profileManager, BiometricAuth biometricAuth,
      FormDetector formDetector, DialogController dialogController, SmsOtpReader smsOtpReader,
      PluginRegistry pluginRegistry, TunnelManager tunnelManager, SessionRecorder sessionRecorder,
      NetworkMonitor networkMonitor) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMcpServer(domController, tabManager, downloadManager, waitController, credentialVault, profileManager, biometricAuth, formDetector, dialogController, smsOtpReader, pluginRegistry, tunnelManager, sessionRecorder, networkMonitor));
  }
}
