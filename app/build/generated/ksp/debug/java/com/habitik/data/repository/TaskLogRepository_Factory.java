package com.habitik.data.repository;

import com.habitik.data.dao.TaskLogDao;
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
public final class TaskLogRepository_Factory implements Factory<TaskLogRepository> {
  private final Provider<TaskLogDao> taskLogDaoProvider;

  public TaskLogRepository_Factory(Provider<TaskLogDao> taskLogDaoProvider) {
    this.taskLogDaoProvider = taskLogDaoProvider;
  }

  @Override
  public TaskLogRepository get() {
    return newInstance(taskLogDaoProvider.get());
  }

  public static TaskLogRepository_Factory create(Provider<TaskLogDao> taskLogDaoProvider) {
    return new TaskLogRepository_Factory(taskLogDaoProvider);
  }

  public static TaskLogRepository newInstance(TaskLogDao taskLogDao) {
    return new TaskLogRepository(taskLogDao);
  }
}
