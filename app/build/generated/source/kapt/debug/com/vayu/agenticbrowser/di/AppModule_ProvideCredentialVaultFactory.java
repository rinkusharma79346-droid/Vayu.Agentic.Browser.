package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.engine.DomController;
import com.vayu.agenticbrowser.tabs.TabManager;
import com.vayu.agenticbrowser.vault.BiometricAuth;
import com.vayu.agenticbrowser.vault.CredentialVault;
import com.vayu.agenticbrowser.vault.ProfileManager;
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
public final class AppModule_ProvideCredentialVaultFactory implements Factory<CredentialVault> {
  private final Provider<ProfileManager> profileManagerProvider;

  private final Provider<BiometricAuth> biometricAuthProvider;

  private final Provider<DomController> domControllerProvider;

  private final Provider<TabManager> tabManagerProvider;

  public AppModule_ProvideCredentialVaultFactory(Provider<ProfileManager> profileManagerProvider,
      Provider<BiometricAuth> biometricAuthProvider, Provider<DomController> domControllerProvider,
      Provider<TabManager> tabManagerProvider) {
    this.profileManagerProvider = profileManagerProvider;
    this.biometricAuthProvider = biometricAuthProvider;
    this.domControllerProvider = domControllerProvider;
    this.tabManagerProvider = tabManagerProvider;
  }

  @Override
  public CredentialVault get() {
    return provideCredentialVault(profileManagerProvider.get(), biometricAuthProvider.get(), domControllerProvider.get(), tabManagerProvider.get());
  }

  public static AppModule_ProvideCredentialVaultFactory create(
      Provider<ProfileManager> profileManagerProvider,
      Provider<BiometricAuth> biometricAuthProvider, Provider<DomController> domControllerProvider,
      Provider<TabManager> tabManagerProvider) {
    return new AppModule_ProvideCredentialVaultFactory(profileManagerProvider, biometricAuthProvider, domControllerProvider, tabManagerProvider);
  }

  public static CredentialVault provideCredentialVault(ProfileManager profileManager,
      BiometricAuth biometricAuth, DomController domController, TabManager tabManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideCredentialVault(profileManager, biometricAuth, domController, tabManager));
  }
}
