package com.habitik.di;

import com.habitik.data.HabitikDatabase;
import com.habitik.data.dao.TaskLogDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideTaskLogDaoFactory implements Factory<TaskLogDao> {
  private final Provider<HabitikDatabase> databaseProvider;

  public DatabaseModule_ProvideTaskLogDaoFactory(Provider<HabitikDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TaskLogDao get() {
    return provideTaskLogDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideTaskLogDaoFactory create(
      Provider<HabitikDatabase> databaseProvider) {
    return new DatabaseModule_ProvideTaskLogDaoFactory(databaseProvider);
  }

  public static TaskLogDao provideTaskLogDao(HabitikDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTaskLogDao(database));
  }
}
