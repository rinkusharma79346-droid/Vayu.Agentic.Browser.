package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.vault.BiometricAuth;
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
public final class AppModule_ProvideBiometricAuthFactory implements Factory<BiometricAuth> {
  @Override
  public BiometricAuth get() {
    return provideBiometricAuth();
  }

  public static AppModule_ProvideBiometricAuthFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BiometricAuth provideBiometricAuth() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBiometricAuth());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideBiometricAuthFactory INSTANCE = new AppModule_ProvideBiometricAuthFactory();
  }
}
