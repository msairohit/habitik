package com.habitik;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast",
    "deprecation"
})
public final class HabitikApp_MembersInjector implements MembersInjector<HabitikApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public HabitikApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<HabitikApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new HabitikApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(HabitikApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.habitik.HabitikApp.workerFactory")
  public static void injectWorkerFactory(HabitikApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
