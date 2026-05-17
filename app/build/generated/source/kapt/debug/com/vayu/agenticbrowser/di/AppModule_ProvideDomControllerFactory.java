package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.engine.DomController;
import com.vayu.agenticbrowser.engine.WebViewManager;
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
public final class AppModule_ProvideDomControllerFactory implements Factory<DomController> {
  private final Provider<WebViewManager> webViewManagerProvider;

  private final Provider<TabManager> tabManagerProvider;

  public AppModule_ProvideDomControllerFactory(Provider<WebViewManager> webViewManagerProvider,
      Provider<TabManager> tabManagerProvider) {
    this.webViewManagerProvider = webViewManagerProvider;
    this.tabManagerProvider = tabManagerProvider;
  }

  @Override
  public DomController get() {
    return provideDomController(webViewManagerProvider.get(), tabManagerProvider.get());
  }

  public static AppModule_ProvideDomControllerFactory create(
      Provider<WebViewManager> webViewManagerProvider, Provider<TabManager> tabManagerProvider) {
    return new AppModule_ProvideDomControllerFactory(webViewManagerProvider, tabManagerProvider);
  }

  public static DomController provideDomController(WebViewManager webViewManager,
      TabManager tabManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDomController(webViewManager, tabManager));
  }
}
