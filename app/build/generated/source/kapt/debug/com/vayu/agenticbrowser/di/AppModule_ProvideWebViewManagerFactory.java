package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.engine.WebViewManager;
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
public final class AppModule_ProvideWebViewManagerFactory implements Factory<WebViewManager> {
  @Override
  public WebViewManager get() {
    return provideWebViewManager();
  }

  public static AppModule_ProvideWebViewManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WebViewManager provideWebViewManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWebViewManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideWebViewManagerFactory INSTANCE = new AppModule_ProvideWebViewManagerFactory();
  }
}
