package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.brain.GoalScheduler;
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
public final class AppModule_ProvideGoalSchedulerFactory implements Factory<GoalScheduler> {
  @Override
  public GoalScheduler get() {
    return provideGoalScheduler();
  }

  public static AppModule_ProvideGoalSchedulerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GoalScheduler provideGoalScheduler() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGoalScheduler());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideGoalSchedulerFactory INSTANCE = new AppModule_ProvideGoalSchedulerFactory();
  }
}
