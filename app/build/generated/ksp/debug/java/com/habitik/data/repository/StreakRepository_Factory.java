package com.habitik.data.repository;

import com.habitik.data.dao.StreakDao;
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
public final class StreakRepository_Factory implements Factory<StreakRepository> {
  private final Provider<StreakDao> streakDaoProvider;

  public StreakRepository_Factory(Provider<StreakDao> streakDaoProvider) {
    this.streakDaoProvider = streakDaoProvider;
  }

  @Override
  public StreakRepository get() {
    return newInstance(streakDaoProvider.get());
  }

  public static StreakRepository_Factory create(Provider<StreakDao> streakDaoProvider) {
    return new StreakRepository_Factory(streakDaoProvider);
  }

  public static StreakRepository newInstance(StreakDao streakDao) {
    return new StreakRepository(streakDao);
  }
}
