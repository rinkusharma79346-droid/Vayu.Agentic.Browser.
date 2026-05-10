package com.vayu.agenticbrowser;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.vayu.agenticbrowser.agent.McpServer;
import com.vayu.agenticbrowser.agent.SessionRecorder;
import com.vayu.agenticbrowser.brain.AgentLoop;
import com.vayu.agenticbrowser.brain.BrainClient;
import com.vayu.agenticbrowser.brain.GoalScheduler;
import com.vayu.agenticbrowser.brain.WorkflowEngine;
import com.vayu.agenticbrowser.common.NetworkMonitor;
import com.vayu.agenticbrowser.di.AppModule_ProvideAgentLoopFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideBiometricAuthFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideBrainClientFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideCredentialVaultFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideDialogControllerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideDomControllerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideDownloadManagerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideFormDetectorFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideGoalSchedulerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideMcpServerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideNetworkMonitorFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvidePluginRegistryFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideProfileManagerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideSessionRecorderFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideSmsOtpReaderFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideTabManagerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideTunnelManagerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideWaitControllerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideWebViewManagerFactory;
import com.vayu.agenticbrowser.di.AppModule_ProvideWorkflowEngineFactory;
import com.vayu.agenticbrowser.downloads.VayuDownloadManager;
import com.vayu.agenticbrowser.engine.DialogController;
import com.vayu.agenticbrowser.engine.DomController;
import com.vayu.agenticbrowser.engine.FormDetector;
import com.vayu.agenticbrowser.engine.WaitController;
import com.vayu.agenticbrowser.engine.WebViewManager;
import com.vayu.agenticbrowser.plugins.PluginRegistry;
import com.vayu.agenticbrowser.tabs.TabManager;
import com.vayu.agenticbrowser.tunnel.TunnelManager;
import com.vayu.agenticbrowser.vault.BiometricAuth;
import com.vayu.agenticbrowser.vault.CredentialVault;
import com.vayu.agenticbrowser.vault.ProfileManager;
import com.vayu.agenticbrowser.vault.SmsOtpReader;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DaggerApp_HiltComponents_SingletonC {
  private DaggerApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public App_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements App_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public App_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements App_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public App_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements App_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public App_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements App_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public App_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements App_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public App_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements App_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public App_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements App_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public App_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends App_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends App_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends App_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends App_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(Collections.<Class<?>, Boolean>emptyMap(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return Collections.<Class<?>, Boolean>emptyMap();
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectMcpServer(instance, singletonCImpl.provideMcpServerProvider.get());
      MainActivity_MembersInjector.injectTabManager(instance, singletonCImpl.provideTabManagerProvider.get());
      MainActivity_MembersInjector.injectDownloadManager(instance, singletonCImpl.provideDownloadManagerProvider.get());
      MainActivity_MembersInjector.injectProfileManager(instance, singletonCImpl.provideProfileManagerProvider.get());
      MainActivity_MembersInjector.injectSmsOtpReader(instance, singletonCImpl.provideSmsOtpReaderProvider.get());
      MainActivity_MembersInjector.injectPluginRegistry(instance, singletonCImpl.providePluginRegistryProvider.get());
      MainActivity_MembersInjector.injectTunnelManager(instance, singletonCImpl.provideTunnelManagerProvider.get());
      MainActivity_MembersInjector.injectSessionRecorder(instance, singletonCImpl.provideSessionRecorderProvider.get());
      MainActivity_MembersInjector.injectNetworkMonitor(instance, singletonCImpl.provideNetworkMonitorProvider.get());
      MainActivity_MembersInjector.injectAgentLoop(instance, singletonCImpl.provideAgentLoopProvider.get());
      MainActivity_MembersInjector.injectGoalScheduler(instance, singletonCImpl.provideGoalSchedulerProvider.get());
      MainActivity_MembersInjector.injectWorkflowEngine(instance, singletonCImpl.provideWorkflowEngineProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends App_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public Map<Class<?>, Provider<ViewModel>> getHiltViewModelMap() {
      return Collections.<Class<?>, Provider<ViewModel>>emptyMap();
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }
  }

  private static final class ActivityRetainedCImpl extends App_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private dagger.internal.Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements dagger.internal.Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends App_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectMcpForegroundService(McpForegroundService arg0) {
      injectMcpForegroundService2(arg0);
    }

    @CanIgnoreReturnValue
    private McpForegroundService injectMcpForegroundService2(McpForegroundService instance) {
      McpForegroundService_MembersInjector.injectMcpServer(instance, singletonCImpl.provideMcpServerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends App_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private dagger.internal.Provider<WebViewManager> provideWebViewManagerProvider;

    private dagger.internal.Provider<TabManager> provideTabManagerProvider;

    private dagger.internal.Provider<DomController> provideDomControllerProvider;

    private dagger.internal.Provider<VayuDownloadManager> provideDownloadManagerProvider;

    private dagger.internal.Provider<WaitController> provideWaitControllerProvider;

    private dagger.internal.Provider<ProfileManager> provideProfileManagerProvider;

    private dagger.internal.Provider<BiometricAuth> provideBiometricAuthProvider;

    private dagger.internal.Provider<CredentialVault> provideCredentialVaultProvider;

    private dagger.internal.Provider<FormDetector> provideFormDetectorProvider;

    private dagger.internal.Provider<DialogController> provideDialogControllerProvider;

    private dagger.internal.Provider<SmsOtpReader> provideSmsOtpReaderProvider;

    private dagger.internal.Provider<PluginRegistry> providePluginRegistryProvider;

    private dagger.internal.Provider<TunnelManager> provideTunnelManagerProvider;

    private dagger.internal.Provider<SessionRecorder> provideSessionRecorderProvider;

    private dagger.internal.Provider<NetworkMonitor> provideNetworkMonitorProvider;

    private dagger.internal.Provider<McpServer> provideMcpServerProvider;

    private dagger.internal.Provider<BrainClient> provideBrainClientProvider;

    private dagger.internal.Provider<GoalScheduler> provideGoalSchedulerProvider;

    private dagger.internal.Provider<AgentLoop> provideAgentLoopProvider;

    private dagger.internal.Provider<WorkflowEngine> provideWorkflowEngineProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideWebViewManagerProvider = DoubleCheck.provider(new SwitchingProvider<WebViewManager>(singletonCImpl, 2));
      this.provideTabManagerProvider = DoubleCheck.provider(new SwitchingProvider<TabManager>(singletonCImpl, 3));
      this.provideDomControllerProvider = DoubleCheck.provider(new SwitchingProvider<DomController>(singletonCImpl, 1));
      this.provideDownloadManagerProvider = DoubleCheck.provider(new SwitchingProvider<VayuDownloadManager>(singletonCImpl, 4));
      this.provideWaitControllerProvider = DoubleCheck.provider(new SwitchingProvider<WaitController>(singletonCImpl, 5));
      this.provideProfileManagerProvider = DoubleCheck.provider(new SwitchingProvider<ProfileManager>(singletonCImpl, 7));
      this.provideBiometricAuthProvider = DoubleCheck.provider(new SwitchingProvider<BiometricAuth>(singletonCImpl, 8));
      this.provideCredentialVaultProvider = DoubleCheck.provider(new SwitchingProvider<CredentialVault>(singletonCImpl, 6));
      this.provideFormDetectorProvider = DoubleCheck.provider(new SwitchingProvider<FormDetector>(singletonCImpl, 9));
      this.provideDialogControllerProvider = DoubleCheck.provider(new SwitchingProvider<DialogController>(singletonCImpl, 10));
      this.provideSmsOtpReaderProvider = DoubleCheck.provider(new SwitchingProvider<SmsOtpReader>(singletonCImpl, 11));
      this.providePluginRegistryProvider = DoubleCheck.provider(new SwitchingProvider<PluginRegistry>(singletonCImpl, 12));
      this.provideTunnelManagerProvider = DoubleCheck.provider(new SwitchingProvider<TunnelManager>(singletonCImpl, 13));
      this.provideSessionRecorderProvider = DoubleCheck.provider(new SwitchingProvider<SessionRecorder>(singletonCImpl, 14));
      this.provideNetworkMonitorProvider = DoubleCheck.provider(new SwitchingProvider<NetworkMonitor>(singletonCImpl, 15));
      this.provideMcpServerProvider = DoubleCheck.provider(new SwitchingProvider<McpServer>(singletonCImpl, 0));
      this.provideBrainClientProvider = DoubleCheck.provider(new SwitchingProvider<BrainClient>(singletonCImpl, 17));
      this.provideGoalSchedulerProvider = DoubleCheck.provider(new SwitchingProvider<GoalScheduler>(singletonCImpl, 18));
      this.provideAgentLoopProvider = DoubleCheck.provider(new SwitchingProvider<AgentLoop>(singletonCImpl, 16));
      this.provideWorkflowEngineProvider = DoubleCheck.provider(new SwitchingProvider<WorkflowEngine>(singletonCImpl, 19));
    }

    @Override
    public void injectApp(App arg0) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements dagger.internal.Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.vayu.agenticbrowser.agent.McpServer 
          return (T) AppModule_ProvideMcpServerFactory.provideMcpServer(singletonCImpl.provideDomControllerProvider.get(), singletonCImpl.provideTabManagerProvider.get(), singletonCImpl.provideDownloadManagerProvider.get(), singletonCImpl.provideWaitControllerProvider.get(), singletonCImpl.provideCredentialVaultProvider.get(), singletonCImpl.provideProfileManagerProvider.get(), singletonCImpl.provideBiometricAuthProvider.get(), singletonCImpl.provideFormDetectorProvider.get(), singletonCImpl.provideDialogControllerProvider.get(), singletonCImpl.provideSmsOtpReaderProvider.get(), singletonCImpl.providePluginRegistryProvider.get(), singletonCImpl.provideTunnelManagerProvider.get(), singletonCImpl.provideSessionRecorderProvider.get(), singletonCImpl.provideNetworkMonitorProvider.get());

          case 1: // com.vayu.agenticbrowser.engine.DomController 
          return (T) AppModule_ProvideDomControllerFactory.provideDomController(singletonCImpl.provideWebViewManagerProvider.get(), singletonCImpl.provideTabManagerProvider.get());

          case 2: // com.vayu.agenticbrowser.engine.WebViewManager 
          return (T) AppModule_ProvideWebViewManagerFactory.provideWebViewManager();

          case 3: // com.vayu.agenticbrowser.tabs.TabManager 
          return (T) AppModule_ProvideTabManagerFactory.provideTabManager();

          case 4: // com.vayu.agenticbrowser.downloads.VayuDownloadManager 
          return (T) AppModule_ProvideDownloadManagerFactory.provideDownloadManager();

          case 5: // com.vayu.agenticbrowser.engine.WaitController 
          return (T) AppModule_ProvideWaitControllerFactory.provideWaitController(singletonCImpl.provideTabManagerProvider.get(), singletonCImpl.provideDownloadManagerProvider.get());

          case 6: // com.vayu.agenticbrowser.vault.CredentialVault 
          return (T) AppModule_ProvideCredentialVaultFactory.provideCredentialVault(singletonCImpl.provideProfileManagerProvider.get(), singletonCImpl.provideBiometricAuthProvider.get(), singletonCImpl.provideDomControllerProvider.get(), singletonCImpl.provideTabManagerProvider.get());

          case 7: // com.vayu.agenticbrowser.vault.ProfileManager 
          return (T) AppModule_ProvideProfileManagerFactory.provideProfileManager();

          case 8: // com.vayu.agenticbrowser.vault.BiometricAuth 
          return (T) AppModule_ProvideBiometricAuthFactory.provideBiometricAuth();

          case 9: // com.vayu.agenticbrowser.engine.FormDetector 
          return (T) AppModule_ProvideFormDetectorFactory.provideFormDetector(singletonCImpl.provideDomControllerProvider.get());

          case 10: // com.vayu.agenticbrowser.engine.DialogController 
          return (T) AppModule_ProvideDialogControllerFactory.provideDialogController();

          case 11: // com.vayu.agenticbrowser.vault.SmsOtpReader 
          return (T) AppModule_ProvideSmsOtpReaderFactory.provideSmsOtpReader();

          case 12: // com.vayu.agenticbrowser.plugins.PluginRegistry 
          return (T) AppModule_ProvidePluginRegistryFactory.providePluginRegistry();

          case 13: // com.vayu.agenticbrowser.tunnel.TunnelManager 
          return (T) AppModule_ProvideTunnelManagerFactory.provideTunnelManager();

          case 14: // com.vayu.agenticbrowser.agent.SessionRecorder 
          return (T) AppModule_ProvideSessionRecorderFactory.provideSessionRecorder();

          case 15: // com.vayu.agenticbrowser.common.NetworkMonitor 
          return (T) AppModule_ProvideNetworkMonitorFactory.provideNetworkMonitor();

          case 16: // com.vayu.agenticbrowser.brain.AgentLoop 
          return (T) AppModule_ProvideAgentLoopFactory.provideAgentLoop(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideBrainClientProvider.get(), singletonCImpl.providePluginRegistryProvider.get(), singletonCImpl.provideMcpServerProvider.get(), singletonCImpl.provideGoalSchedulerProvider.get());

          case 17: // com.vayu.agenticbrowser.brain.BrainClient 
          return (T) AppModule_ProvideBrainClientFactory.provideBrainClient();

          case 18: // com.vayu.agenticbrowser.brain.GoalScheduler 
          return (T) AppModule_ProvideGoalSchedulerFactory.provideGoalScheduler();

          case 19: // com.vayu.agenticbrowser.brain.WorkflowEngine 
          return (T) AppModule_ProvideWorkflowEngineFactory.provideWorkflowEngine(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideAgentLoopProvider.get(), singletonCImpl.provideMcpServerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
