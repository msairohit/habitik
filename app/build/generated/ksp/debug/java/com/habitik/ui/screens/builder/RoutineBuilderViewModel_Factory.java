package com.habitik.ui.screens.builder;

import com.habitik.data.repository.TaskRepository;
import com.habitik.service.AlarmScheduler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class RoutineBuilderViewModel_Factory implements Factory<RoutineBuilderViewModel> {
  private final Provider<TaskRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  public RoutineBuilderViewModel_Factory(Provider<TaskRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public RoutineBuilderViewModel get() {
    return newInstance(repositoryProvider.get(), alarmSchedulerProvider.get());
  }

  public static RoutineBuilderViewModel_Factory create(Provider<TaskRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new RoutineBuilderViewModel_Factory(repositoryProvider, alarmSchedulerProvider);
  }

  public static RoutineBuilderViewModel newInstance(TaskRepository repository,
      AlarmScheduler alarmScheduler) {
    return new RoutineBuilderViewModel(repository, alarmScheduler);
  }
}
