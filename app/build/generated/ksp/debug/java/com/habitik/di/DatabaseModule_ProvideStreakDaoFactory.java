package com.habitik.di;

import com.habitik.data.HabitikDatabase;
import com.habitik.data.dao.StreakDao;
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
public final class DatabaseModule_ProvideStreakDaoFactory implements Factory<StreakDao> {
  private final Provider<HabitikDatabase> databaseProvider;

  public DatabaseModule_ProvideStreakDaoFactory(Provider<HabitikDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public StreakDao get() {
    return provideStreakDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideStreakDaoFactory create(
      Provider<HabitikDatabase> databaseProvider) {
    return new DatabaseModule_ProvideStreakDaoFactory(databaseProvider);
  }

  public static StreakDao provideStreakDao(HabitikDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStreakDao(database));
  }
}
