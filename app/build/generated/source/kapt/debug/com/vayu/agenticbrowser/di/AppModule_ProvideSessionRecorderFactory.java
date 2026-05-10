package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.agent.SessionRecorder;
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
public final class AppModule_ProvideSessionRecorderFactory implements Factory<SessionRecorder> {
  @Override
  public SessionRecorder get() {
    return provideSessionRecorder();
  }

  public static AppModule_ProvideSessionRecorderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SessionRecorder provideSessionRecorder() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSessionRecorder());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideSessionRecorderFactory INSTANCE = new AppModule_ProvideSessionRecorderFactory();
  }
}
