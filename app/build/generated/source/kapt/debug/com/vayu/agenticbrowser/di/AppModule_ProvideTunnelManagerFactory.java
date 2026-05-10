package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.tunnel.TunnelManager;
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
public final class AppModule_ProvideTunnelManagerFactory implements Factory<TunnelManager> {
  @Override
  public TunnelManager get() {
    return provideTunnelManager();
  }

  public static AppModule_ProvideTunnelManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TunnelManager provideTunnelManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTunnelManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideTunnelManagerFactory INSTANCE = new AppModule_ProvideTunnelManagerFactory();
  }
}
