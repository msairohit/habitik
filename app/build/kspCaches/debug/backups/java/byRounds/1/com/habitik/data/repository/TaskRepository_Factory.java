package com.habitik.data.repository;

import com.habitik.data.dao.TaskDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
    "cast",
    "deprecation"
})
public final class TaskRepository_Factory implements Factory<TaskRepository> {
  private final Provider<TaskDao> taskDaoProvider;

  public TaskRepository_Factory(Provider<TaskDao> taskDaoProvider) {
    this.taskDaoProvider = taskDaoProvider;
  }

  @Override
  public TaskRepository get() {
    return newInstance(taskDaoProvider.get());
  }

  public static TaskRepository_Factory create(Provider<TaskDao> taskDaoProvider) {
    return new TaskRepository_Factory(taskDaoProvider);
  }

  public static TaskRepository newInstance(TaskDao taskDao) {
    return new TaskRepository(taskDao);
  }
}
