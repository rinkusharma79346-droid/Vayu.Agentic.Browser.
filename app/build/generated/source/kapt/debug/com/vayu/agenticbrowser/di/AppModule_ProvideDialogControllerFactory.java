package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.engine.DialogController;
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
public final class AppModule_ProvideDialogControllerFactory implements Factory<DialogController> {
  @Override
  public DialogController get() {
    return provideDialogController();
  }

  public static AppModule_ProvideDialogControllerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DialogController provideDialogController() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDialogController());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideDialogControllerFactory INSTANCE = new AppModule_ProvideDialogControllerFactory();
  }
}
