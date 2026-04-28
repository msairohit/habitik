package com.habitik.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.habitik.data.dao.RoutineDao;
import com.habitik.data.dao.RoutineDao_Impl;
import com.habitik.data.dao.StreakDao;
import com.habitik.data.dao.StreakDao_Impl;
import com.habitik.data.dao.TaskDao;
import com.habitik.data.dao.TaskDao_Impl;
import com.habitik.data.dao.TaskLogDao;
import com.habitik.data.dao.TaskLogDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HabitikDatabase_Impl extends HabitikDatabase {
  private volatile TaskDao _taskDao;

  private volatile TaskLogDao _taskLogDao;

  private volatile StreakDao _streakDao;

  private volatile RoutineDao _routineDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `category` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `startTime` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `isFlexible` INTEGER NOT NULL, `flexWindowEnd` TEXT, `repeatDays` TEXT NOT NULL, `repeatCount` INTEGER NOT NULL, `isImportant` INTEGER NOT NULL, `reminderMin` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `task_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `logDate` TEXT NOT NULL, `status` TEXT NOT NULL, `doneAt` INTEGER, `occurrence` INTEGER NOT NULL, FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_logs_taskId` ON `task_logs` (`taskId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `streaks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `bestStreak` INTEGER NOT NULL, `lastDoneDate` TEXT, `graceUsed` INTEGER NOT NULL, FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_streaks_taskId` ON `streaks` (`taskId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `routines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `startTime` TEXT NOT NULL, `durationMinutes` INTEGER NOT NULL, `daysOfWeek` TEXT NOT NULL, `isCompletedToday` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '744d2e01c7bea5be634652d1aaeb7763')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `tasks`");
        db.execSQL("DROP TABLE IF EXISTS `task_logs`");
        db.execSQL("DROP TABLE IF EXISTS `streaks`");
        db.execSQL("DROP TABLE IF EXISTS `routines`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTasks = new HashMap<String, TableInfo.Column>(14);
        _columnsTasks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("colorHex", new TableInfo.Column("colorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("startTime", new TableInfo.Column("startTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("durationMin", new TableInfo.Column("durationMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("isFlexible", new TableInfo.Column("isFlexible", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("flexWindowEnd", new TableInfo.Column("flexWindowEnd", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("repeatDays", new TableInfo.Column("repeatDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("repeatCount", new TableInfo.Column("repeatCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("isImportant", new TableInfo.Column("isImportant", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("reminderMin", new TableInfo.Column("reminderMin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTasks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTasks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTasks = new TableInfo("tasks", _columnsTasks, _foreignKeysTasks, _indicesTasks);
        final TableInfo _existingTasks = TableInfo.read(db, "tasks");
        if (!_infoTasks.equals(_existingTasks)) {
          return new RoomOpenHelper.ValidationResult(false, "tasks(com.habitik.data.entity.TaskEntity).\n"
                  + " Expected:\n" + _infoTasks + "\n"
                  + " Found:\n" + _existingTasks);
        }
        final HashMap<String, TableInfo.Column> _columnsTaskLogs = new HashMap<String, TableInfo.Column>(6);
        _columnsTaskLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskLogs.put("taskId", new TableInfo.Column("taskId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskLogs.put("logDate", new TableInfo.Column("logDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskLogs.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskLogs.put("doneAt", new TableInfo.Column("doneAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskLogs.put("occurrence", new TableInfo.Column("occurrence", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTaskLogs = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTaskLogs.add(new TableInfo.ForeignKey("tasks", "CASCADE", "NO ACTION", Arrays.asList("taskId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTaskLogs = new HashSet<TableInfo.Index>(1);
        _indicesTaskLogs.add(new TableInfo.Index("index_task_logs_taskId", false, Arrays.asList("taskId"), Arrays.asList("ASC")));
        final TableInfo _infoTaskLogs = new TableInfo("task_logs", _columnsTaskLogs, _foreignKeysTaskLogs, _indicesTaskLogs);
        final TableInfo _existingTaskLogs = TableInfo.read(db, "task_logs");
        if (!_infoTaskLogs.equals(_existingTaskLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "task_logs(com.habitik.data.entity.TaskLogEntity).\n"
                  + " Expected:\n" + _infoTaskLogs + "\n"
                  + " Found:\n" + _existingTaskLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsStreaks = new HashMap<String, TableInfo.Column>(6);
        _columnsStreaks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreaks.put("taskId", new TableInfo.Column("taskId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreaks.put("currentStreak", new TableInfo.Column("currentStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreaks.put("bestStreak", new TableInfo.Column("bestStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreaks.put("lastDoneDate", new TableInfo.Column("lastDoneDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStreaks.put("graceUsed", new TableInfo.Column("graceUsed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStreaks = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysStreaks.add(new TableInfo.ForeignKey("tasks", "CASCADE", "NO ACTION", Arrays.asList("taskId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesStreaks = new HashSet<TableInfo.Index>(1);
        _indicesStreaks.add(new TableInfo.Index("index_streaks_taskId", false, Arrays.asList("taskId"), Arrays.asList("ASC")));
        final TableInfo _infoStreaks = new TableInfo("streaks", _columnsStreaks, _foreignKeysStreaks, _indicesStreaks);
        final TableInfo _existingStreaks = TableInfo.read(db, "streaks");
        if (!_infoStreaks.equals(_existingStreaks)) {
          return new RoomOpenHelper.ValidationResult(false, "streaks(com.habitik.data.entity.StreakEntity).\n"
                  + " Expected:\n" + _infoStreaks + "\n"
                  + " Found:\n" + _existingStreaks);
        }
        final HashMap<String, TableInfo.Column> _columnsRoutines = new HashMap<String, TableInfo.Column>(6);
        _columnsRoutines.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoutines.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoutines.put("startTime", new TableInfo.Column("startTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoutines.put("durationMinutes", new TableInfo.Column("durationMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoutines.put("daysOfWeek", new TableInfo.Column("daysOfWeek", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoutines.put("isCompletedToday", new TableInfo.Column("isCompletedToday", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRoutines = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRoutines = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRoutines = new TableInfo("routines", _columnsRoutines, _foreignKeysRoutines, _indicesRoutines);
        final TableInfo _existingRoutines = TableInfo.read(db, "routines");
        if (!_infoRoutines.equals(_existingRoutines)) {
          return new RoomOpenHelper.ValidationResult(false, "routines(com.habitik.data.Routine).\n"
                  + " Expected:\n" + _infoRoutines + "\n"
                  + " Found:\n" + _existingRoutines);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "744d2e01c7bea5be634652d1aaeb7763", "e3e9f572480cee1056dfea10704c322c");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "tasks","task_logs","streaks","routines");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `tasks`");
      _db.execSQL("DELETE FROM `task_logs`");
      _db.execSQL("DELETE FROM `streaks`");
      _db.execSQL("DELETE FROM `routines`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TaskDao.class, TaskDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TaskLogDao.class, TaskLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StreakDao.class, StreakDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RoutineDao.class, RoutineDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TaskDao taskDao() {
    if (_taskDao != null) {
      return _taskDao;
    } else {
      synchronized(this) {
        if(_taskDao == null) {
          _taskDao = new TaskDao_Impl(this);
        }
        return _taskDao;
      }
    }
  }

  @Override
  public TaskLogDao taskLogDao() {
    if (_taskLogDao != null) {
      return _taskLogDao;
    } else {
      synchronized(this) {
        if(_taskLogDao == null) {
          _taskLogDao = new TaskLogDao_Impl(this);
        }
        return _taskLogDao;
      }
    }
  }

  @Override
  public StreakDao streakDao() {
    if (_streakDao != null) {
      return _streakDao;
    } else {
      synchronized(this) {
        if(_streakDao == null) {
          _streakDao = new StreakDao_Impl(this);
        }
        return _streakDao;
      }
    }
  }

  @Override
  public RoutineDao routineDao() {
    if (_routineDao != null) {
      return _routineDao;
    } else {
      synchronized(this) {
        if(_routineDao == null) {
          _routineDao = new RoutineDao_Impl(this);
        }
        return _routineDao;
      }
    }
  }
}
