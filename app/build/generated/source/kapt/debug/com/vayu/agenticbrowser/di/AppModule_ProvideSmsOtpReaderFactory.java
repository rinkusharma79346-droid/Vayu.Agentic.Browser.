package com.vayu.agenticbrowser.di;

import com.vayu.agenticbrowser.vault.SmsOtpReader;
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
public final class AppModule_ProvideSmsOtpReaderFactory implements Factory<SmsOtpReader> {
  @Override
  public SmsOtpReader get() {
    return provideSmsOtpReader();
  }

  public static AppModule_ProvideSmsOtpReaderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SmsOtpReader provideSmsOtpReader() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSmsOtpReader());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideSmsOtpReaderFactory INSTANCE = new AppModule_ProvideSmsOtpReaderFactory();
  }
}
