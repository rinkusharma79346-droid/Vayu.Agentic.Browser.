package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.plugins.PluginRegistry;
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
public final class AppModule_ProvidePluginRegistryFactory implements Factory<PluginRegistry> {
  @Override
  public PluginRegistry get() {
    return providePluginRegistry();
  }

  public static AppModule_ProvidePluginRegistryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PluginRegistry providePluginRegistry() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePluginRegistry());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvidePluginRegistryFactory INSTANCE = new AppModule_ProvidePluginRegistryFactory();
  }
}
