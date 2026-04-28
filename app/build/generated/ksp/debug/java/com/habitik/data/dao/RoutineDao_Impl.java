package com.habitik.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.habitik.data.Routine;
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
public final class RoutineDao_Impl implements RoutineDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Routine> __insertionAdapterOfRoutine;

  private final EntityDeletionOrUpdateAdapter<Routine> __deletionAdapterOfRoutine;

  private final EntityDeletionOrUpdateAdapter<Routine> __updateAdapterOfRoutine;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsDone;

  public RoutineDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRoutine = new EntityInsertionAdapter<Routine>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `routines` (`id`,`title`,`startTime`,`durationMinutes`,`daysOfWeek`,`isCompletedToday`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Routine entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getStartTime());
        statement.bindLong(4, entity.getDurationMinutes());
        statement.bindString(5, entity.getDaysOfWeek());
        final int _tmp = entity.isCompletedToday() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfRoutine = new EntityDeletionOrUpdateAdapter<Routine>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `routines` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Routine entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfRoutine = new EntityDeletionOrUpdateAdapter<Routine>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `routines` SET `id` = ?,`title` = ?,`startTime` = ?,`durationMinutes` = ?,`daysOfWeek` = ?,`isCompletedToday` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Routine entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getStartTime());
        statement.bindLong(4, entity.getDurationMinutes());
        statement.bindString(5, entity.getDaysOfWeek());
        final int _tmp = entity.isCompletedToday() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfMarkAsDone = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE routines SET isCompletedToday = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Routine routine, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRoutine.insertAndReturnId(routine);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Routine routine, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRoutine.handle(routine);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Routine routine, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRoutine.handle(routine);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsDone(final int id, final boolean completed,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsDone.acquire();
        int _argIndex = 1;
        final int _tmp = completed ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
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
          __preparedStmtOfMarkAsDone.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Routine>> getAllRoutines() {
    final String _sql = "SELECT * FROM routines";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"routines"}, new Callable<List<Routine>>() {
      @Override
      @NonNull
      public List<Routine> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfDaysOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeek");
          final int _cursorIndexOfIsCompletedToday = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompletedToday");
          final List<Routine> _result = new ArrayList<Routine>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Routine _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpStartTime;
            _tmpStartTime = _cursor.getString(_cursorIndexOfStartTime);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final String _tmpDaysOfWeek;
            _tmpDaysOfWeek = _cursor.getString(_cursorIndexOfDaysOfWeek);
            final boolean _tmpIsCompletedToday;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompletedToday);
            _tmpIsCompletedToday = _tmp != 0;
            _item = new Routine(_tmpId,_tmpTitle,_tmpStartTime,_tmpDurationMinutes,_tmpDaysOfWeek,_tmpIsCompletedToday);
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
