package com.habitik.di;

import com.habitik.data.HabitikDatabase;
import com.habitik.data.dao.TaskDao;
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
public final class DatabaseModule_ProvideTaskDaoFactory implements Factory<TaskDao> {
  private final Provider<HabitikDatabase> databaseProvider;

  public DatabaseModule_ProvideTaskDaoFactory(Provider<HabitikDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TaskDao get() {
    return provideTaskDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideTaskDaoFactory create(
      Provider<HabitikDatabase> databaseProvider) {
    return new DatabaseModule_ProvideTaskDaoFactory(databaseProvider);
  }

  public static TaskDao provideTaskDao(HabitikDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTaskDao(database));
  }
}
