package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.engine.DomController;
import com.vayu.agenticbrowser.engine.FormDetector;
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
public final class AppModule_ProvideFormDetectorFactory implements Factory<FormDetector> {
  private final Provider<DomController> domControllerProvider;

  public AppModule_ProvideFormDetectorFactory(Provider<DomController> domControllerProvider) {
    this.domControllerProvider = domControllerProvider;
  }

  @Override
  public FormDetector get() {
    return provideFormDetector(domControllerProvider.get());
  }

  public static AppModule_ProvideFormDetectorFactory create(
      Provider<DomController> domControllerProvider) {
    return new AppModule_ProvideFormDetectorFactory(domControllerProvider);
  }

  public static FormDetector provideFormDetector(DomController domController) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFormDetector(domController));
  }
}
