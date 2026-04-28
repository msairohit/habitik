package com.habitik.service;

import com.habitik.data.RoutineRepository;
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
public final class RoutineReceiver_MembersInjector implements MembersInjector<RoutineReceiver> {
  private final Provider<RoutineRepository> repositoryProvider;

  public RoutineReceiver_MembersInjector(Provider<RoutineRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public static MembersInjector<RoutineReceiver> create(
      Provider<RoutineRepository> repositoryProvider) {
    return new RoutineReceiver_MembersInjector(repositoryProvider);
  }

  @Override
  public void injectMembers(RoutineReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
  }

  @InjectedFieldSignature("com.habitik.service.RoutineReceiver.repository")
  public static void injectRepository(RoutineReceiver instance, RoutineRepository repository) {
    instance.repository = repository;
  }
}
