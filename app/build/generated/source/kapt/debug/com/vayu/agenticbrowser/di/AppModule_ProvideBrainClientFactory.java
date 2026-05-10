package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.brain.BrainClient;
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
public final class AppModule_ProvideBrainClientFactory implements Factory<BrainClient> {
  @Override
  public BrainClient get() {
    return provideBrainClient();
  }

  public static AppModule_ProvideBrainClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BrainClient provideBrainClient() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBrainClient());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideBrainClientFactory INSTANCE = new AppModule_ProvideBrainClientFactory();
  }
}
