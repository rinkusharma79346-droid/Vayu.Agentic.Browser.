package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.common.NetworkMonitor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideNetworkMonitorFactory implements Factory<NetworkMonitor> {
  @Override
  public NetworkMonitor get() {
    return provideNetworkMonitor();
  }

  public static AppModule_ProvideNetworkMonitorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NetworkMonitor provideNetworkMonitor() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideNetworkMonitor());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideNetworkMonitorFactory INSTANCE = new AppModule_ProvideNetworkMonitorFactory();
  }
}
