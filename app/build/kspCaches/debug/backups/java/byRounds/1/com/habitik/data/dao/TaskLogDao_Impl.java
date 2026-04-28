package com.habitik.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.habitik.data.entity.TaskLogEntity;
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
public final class TaskLogDao_Impl implements TaskLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TaskLogEntity> __insertionAdapterOfTaskLogEntity;

  private final EntityDeletionOrUpdateAdapter<TaskLogEntity> __updateAdapterOfTaskLogEntity;

  public TaskLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTaskLogEntity = new EntityInsertionAdapter<TaskLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `task_logs` (`id`,`taskId`,`logDate`,`status`,`doneAt`,`occurrence`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskLogEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindString(3, entity.getLogDate());
        statement.bindString(4, entity.getStatus());
        if (entity.getDoneAt() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getDoneAt());
        }
        statement.bindLong(6, entity.getOccurrence());
      }
    };
    this.__updateAdapterOfTaskLogEntity = new EntityDeletionOrUpdateAdapter<TaskLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `task_logs` SET `id` = ?,`taskId` = ?,`logDate` = ?,`status` = ?,`doneAt` = ?,`occurrence` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskLogEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindString(3, entity.getLogDate());
        statement.bindString(4, entity.getStatus());
        if (entity.getDoneAt() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getDoneAt());
        }
        statement.bindLong(6, entity.getOccurrence());
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public Object insertLog(final TaskLogEntity log, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTaskLogEntity.insertAndReturnId(log);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLog(final TaskLogEntity log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTaskLogEntity.handle(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TaskLogEntity>> getLogsForDate(final String date) {
    final String _sql = "SELECT * FROM task_logs WHERE logDate = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"task_logs"}, new Callable<List<TaskLogEntity>>() {
      @Override
      @NonNull
      public List<TaskLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDoneAt = CursorUtil.getColumnIndexOrThrow(_cursor, "doneAt");
          final int _cursorIndexOfOccurrence = CursorUtil.getColumnIndexOrThrow(_cursor, "occurrence");
          final List<TaskLogEntity> _result = new ArrayList<TaskLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskLogEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTaskId;
            _tmpTaskId = _cursor.getInt(_cursorIndexOfTaskId);
            final String _tmpLogDate;
            _tmpLogDate = _cursor.getString(_cursorIndexOfLogDate);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Long _tmpDoneAt;
            if (_cursor.isNull(_cursorIndexOfDoneAt)) {
              _tmpDoneAt = null;
            } else {
              _tmpDoneAt = _cursor.getLong(_cursorIndexOfDoneAt);
            }
            final int _tmpOccurrence;
            _tmpOccurrence = _cursor.getInt(_cursorIndexOfOccurrence);
            _item = new TaskLogEntity(_tmpId,_tmpTaskId,_tmpLogDate,_tmpStatus,_tmpDoneAt,_tmpOccurrence);
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
  public Flow<List<TaskLogEntity>> getLogsForTask(final int taskId) {
    final String _sql = "SELECT * FROM task_logs WHERE taskId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"task_logs"}, new Callable<List<TaskLogEntity>>() {
      @Override
      @NonNull
      public List<TaskLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDoneAt = CursorUtil.getColumnIndexOrThrow(_cursor, "doneAt");
          final int _cursorIndexOfOccurrence = CursorUtil.getColumnIndexOrThrow(_cursor, "occurrence");
          final List<TaskLogEntity> _result = new ArrayList<TaskLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskLogEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTaskId;
            _tmpTaskId = _cursor.getInt(_cursorIndexOfTaskId);
            final String _tmpLogDate;
            _tmpLogDate = _cursor.getString(_cursorIndexOfLogDate);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Long _tmpDoneAt;
            if (_cursor.isNull(_cursorIndexOfDoneAt)) {
              _tmpDoneAt = null;
            } else {
              _tmpDoneAt = _cursor.getLong(_cursorIndexOfDoneAt);
            }
            final int _tmpOccurrence;
            _tmpOccurrence = _cursor.getInt(_cursorIndexOfOccurrence);
            _item = new TaskLogEntity(_tmpId,_tmpTaskId,_tmpLogDate,_tmpStatus,_tmpDoneAt,_tmpOccurrence);
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
  public Flow<List<TaskLogEntity>> getLogsInRange(final String startDate, final String endDate) {
    final String _sql = "SELECT * FROM task_logs WHERE logDate BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindString(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"task_logs"}, new Callable<List<TaskLogEntity>>() {
      @Override
      @NonNull
      public List<TaskLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDoneAt = CursorUtil.getColumnIndexOrThrow(_cursor, "doneAt");
          final int _cursorIndexOfOccurrence = CursorUtil.getColumnIndexOrThrow(_cursor, "occurrence");
          final List<TaskLogEntity> _result = new ArrayList<TaskLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskLogEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTaskId;
            _tmpTaskId = _cursor.getInt(_cursorIndexOfTaskId);
            final String _tmpLogDate;
            _tmpLogDate = _cursor.getString(_cursorIndexOfLogDate);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Long _tmpDoneAt;
            if (_cursor.isNull(_cursorIndexOfDoneAt)) {
              _tmpDoneAt = null;
            } else {
              _tmpDoneAt = _cursor.getLong(_cursorIndexOfDoneAt);
            }
            final int _tmpOccurrence;
            _tmpOccurrence = _cursor.getInt(_cursorIndexOfOccurrence);
            _item = new TaskLogEntity(_tmpId,_tmpTaskId,_tmpLogDate,_tmpStatus,_tmpDoneAt,_tmpOccurrence);
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
