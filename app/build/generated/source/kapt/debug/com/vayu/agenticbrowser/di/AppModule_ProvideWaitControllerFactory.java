package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.engine.WaitController;
import com.vayu.agenticbrowser.tabs.TabManager;
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
public final class AppModule_ProvideWaitControllerFactory implements Factory<WaitController> {
  private final Provider<TabManager> tabManagerProvider;

  private final Provider<VayuDownloadManager> downloadManagerProvider;

  public AppModule_ProvideWaitControllerFactory(Provider<TabManager> tabManagerProvider,
      Provider<VayuDownloadManager> downloadManagerProvider) {
    this.tabManagerProvider = tabManagerProvider;
    this.downloadManagerProvider = downloadManagerProvider;
  }

  @Override
  public WaitController get() {
    return provideWaitController(tabManagerProvider.get(), downloadManagerProvider.get());
  }

  public static AppModule_ProvideWaitControllerFactory create(
      Provider<TabManager> tabManagerProvider,
      Provider<VayuDownloadManager> downloadManagerProvider) {
    return new AppModule_ProvideWaitControllerFactory(tabManagerProvider, downloadManagerProvider);
  }

  public static WaitController provideWaitController(TabManager tabManager,
      VayuDownloadManager downloadManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWaitController(tabManager, downloadManager));
  }
}
