package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.vault.ProfileManager;
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
public final class AppModule_ProvideProfileManagerFactory implements Factory<ProfileManager> {
  @Override
  public ProfileManager get() {
    return provideProfileManager();
  }

  public static AppModule_ProvideProfileManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ProfileManager provideProfileManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideProfileManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideProfileManagerFactory INSTANCE = new AppModule_ProvideProfileManagerFactory();
  }
}
