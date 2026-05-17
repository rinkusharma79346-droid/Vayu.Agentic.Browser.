package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.tabs.TabManager;
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
public final class AppModule_ProvideTabManagerFactory implements Factory<TabManager> {
  @Override
  public TabManager get() {
    return provideTabManager();
  }

  public static AppModule_ProvideTabManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TabManager provideTabManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTabManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideTabManagerFactory INSTANCE = new AppModule_ProvideTabManagerFactory();
  }
}
