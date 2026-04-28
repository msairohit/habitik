package com.habitik.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.habitik.data.entity.TaskEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TaskDao_Impl implements TaskDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TaskEntity> __insertionAdapterOfTaskEntity;

  private final EntityDeletionOrUpdateAdapter<TaskEntity> __updateAdapterOfTaskEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteTask;

  public TaskDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTaskEntity = new EntityInsertionAdapter<TaskEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `tasks` (`id`,`name`,`category`,`colorHex`,`startTime`,`durationMin`,`isFlexible`,`flexWindowEnd`,`repeatDays`,`repeatCount`,`isImportant`,`reminderMin`,`isActive`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCategory());
        statement.bindString(4, entity.getColorHex());
        statement.bindString(5, entity.getStartTime());
        statement.bindLong(6, entity.getDurationMin());
        final int _tmp = entity.isFlexible() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getFlexWindowEnd() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getFlexWindowEnd());
        }
        statement.bindString(9, entity.getRepeatDays());
        statement.bindLong(10, entity.getRepeatCount());
        final int _tmp_1 = entity.isImportant() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getReminderMin());
        final int _tmp_2 = entity.isActive() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        statement.bindLong(14, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfTaskEntity = new EntityDeletionOrUpdateAdapter<TaskEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `tasks` SET `id` = ?,`name` = ?,`category` = ?,`colorHex` = ?,`startTime` = ?,`durationMin` = ?,`isFlexible` = ?,`flexWindowEnd` = ?,`repeatDays` = ?,`repeatCount` = ?,`isImportant` = ?,`reminderMin` = ?,`isActive` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCategory());
        statement.bindString(4, entity.getColorHex());
        statement.bindString(5, entity.getStartTime());
        statement.bindLong(6, entity.getDurationMin());
        final int _tmp = entity.isFlexible() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getFlexWindowEnd() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getFlexWindowEnd());
        }
        statement.bindString(9, entity.getRepeatDays());
        statement.bindLong(10, entity.getRepeatCount());
        final int _tmp_1 = entity.isImportant() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getReminderMin());
        final int _tmp_2 = entity.isActive() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        statement.bindLong(14, entity.getCreatedAt());
        statement.bindLong(15, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDeleteTask = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tasks SET isActive = 0 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTask(final TaskEntity task, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTaskEntity.insertAndReturnId(task);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTask(final TaskEntity task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTaskEntity.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeleteTask(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeleteTask.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSoftDeleteTask.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TaskEntity>> getAllActiveTasks() {
    final String _sql = "SELECT * FROM tasks WHERE isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMin");
          final int _cursorIndexOfIsFlexible = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlexible");
          final int _cursorIndexOfFlexWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "flexWindowEnd");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfRepeatCount = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatCount");
          final int _cursorIndexOfIsImportant = CursorUtil.getColumnIndexOrThrow(_cursor, "isImportant");
          final int _cursorIndexOfReminderMin = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMin");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMin;
            _tmpDurationMin = _cursor.getInt(_cursorIndexOfDurationMin);
            final boolean _tmpIsFlexible;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFlexible);
            _tmpIsFlexible = _tmp != 0;
            final String _tmpFlexWindowEnd;
            if (_cursor.isNull(_cursorIndexOfFlexWindowEnd)) {
              _tmpFlexWindowEnd = null;
            } else {
              _tmpFlexWindowEnd = _cursor.getString(_cursorIndexOfFlexWindowEnd);
            }
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final int _tmpRepeatCount;
            _tmpRepeatCount = _cursor.getInt(_cursorIndexOfRepeatCount);
            final boolean _tmpIsImportant;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsImportant);
            _tmpIsImportant = _tmp_1 != 0;
            final int _tmpReminderMin;
            _tmpReminderMin = _cursor.getInt(_cursorIndexOfReminderMin);
            final boolean _tmpIsActive;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new TaskEntity(_tmpId,_tmpName,_tmpCategory,_tmpColorHex,_tmpStartTime,_tmpDurationMin,_tmpIsFlexible,_tmpFlexWindowEnd,_tmpRepeatDays,_tmpRepeatCount,_tmpIsImportant,_tmpReminderMin,_tmpIsActive,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTaskById(final int id, final Continuation<? super TaskEntity> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TaskEntity>() {
      @Override
      @Nullable
      public TaskEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMin");
          final int _cursorIndexOfIsFlexible = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlexible");
          final int _cursorIndexOfFlexWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "flexWindowEnd");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfRepeatCount = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatCount");
          final int _cursorIndexOfIsImportant = CursorUtil.getColumnIndexOrThrow(_cursor, "isImportant");
          final int _cursorIndexOfReminderMin = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMin");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final TaskEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMin;
            _tmpDurationMin = _cursor.getInt(_cursorIndexOfDurationMin);
            final boolean _tmpIsFlexible;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFlexible);
            _tmpIsFlexible = _tmp != 0;
            final String _tmpFlexWindowEnd;
            if (_cursor.isNull(_cursorIndexOfFlexWindowEnd)) {
              _tmpFlexWindowEnd = null;
            } else {
              _tmpFlexWindowEnd = _cursor.getString(_cursorIndexOfFlexWindowEnd);
            }
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final int _tmpRepeatCount;
            _tmpRepeatCount = _cursor.getInt(_cursorIndexOfRepeatCount);
            final boolean _tmpIsImportant;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsImportant);
            _tmpIsImportant = _tmp_1 != 0;
            final int _tmpReminderMin;
            _tmpReminderMin = _cursor.getInt(_cursorIndexOfReminderMin);
            final boolean _tmpIsActive;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new TaskEntity(_tmpId,_tmpName,_tmpCategory,_tmpColorHex,_tmpStartTime,_tmpDurationMin,_tmpIsFlexible,_tmpFlexWindowEnd,_tmpRepeatDays,_tmpRepeatCount,_tmpIsImportant,_tmpReminderMin,_tmpIsActive,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TaskEntity>> getTasksByDay(final String dayOfWeek) {
    final String _sql = "SELECT * FROM tasks WHERE isActive = 1 AND repeatDays LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, dayOfWeek);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<TaskEntity>>() {
      @Override
      @NonNull
      public List<TaskEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMin = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMin");
          final int _cursorIndexOfIsFlexible = CursorUtil.getColumnIndexOrThrow(_cursor, "isFlexible");
          final int _cursorIndexOfFlexWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "flexWindowEnd");
          final int _cursorIndexOfRepeatDays = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatDays");
          final int _cursorIndexOfRepeatCount = CursorUtil.getColumnIndexOrThrow(_cursor, "repeatCount");
          final int _cursorIndexOfIsImportant = CursorUtil.getColumnIndexOrThrow(_cursor, "isImportant");
          final int _cursorIndexOfReminderMin = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMin");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<TaskEntity> _result = new ArrayList<TaskEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMin;
            _tmpDurationMin = _cursor.getInt(_cursorIndexOfDurationMin);
            final boolean _tmpIsFlexible;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFlexible);
            _tmpIsFlexible = _tmp != 0;
            final String _tmpFlexWindowEnd;
            if (_cursor.isNull(_cursorIndexOfFlexWindowEnd)) {
              _tmpFlexWindowEnd = null;
            } else {
              _tmpFlexWindowEnd = _cursor.getString(_cursorIndexOfFlexWindowEnd);
            }
            final String _tmpRepeatDays;
            _tmpRepeatDays = _cursor.getString(_cursorIndexOfRepeatDays);
            final int _tmpRepeatCount;
            _tmpRepeatCount = _cursor.getInt(_cursorIndexOfRepeatCount);
            final boolean _tmpIsImportant;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsImportant);
            _tmpIsImportant = _tmp_1 != 0;
            final int _tmpReminderMin;
            _tmpReminderMin = _cursor.getInt(_cursorIndexOfReminderMin);
            final boolean _tmpIsActive;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new TaskEntity(_tmpId,_tmpName,_tmpCategory,_tmpColorHex,_tmpStartTime,_tmpDurationMin,_tmpIsFlexible,_tmpFlexWindowEnd,_tmpRepeatDays,_tmpRepeatCount,_tmpIsImportant,_tmpReminderMin,_tmpIsActive,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
