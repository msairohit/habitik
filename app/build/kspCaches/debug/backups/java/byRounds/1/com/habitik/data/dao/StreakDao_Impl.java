package com.habitik.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.habitik.data.entity.StreakEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class StreakDao_Impl implements StreakDao {
  private final RoomDatabase __db;

  private final EntityUpsertionAdapter<StreakEntity> __upsertionAdapterOfStreakEntity;

  public StreakDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertionAdapterOfStreakEntity = new EntityUpsertionAdapter<StreakEntity>(new EntityInsertionAdapter<StreakEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `streaks` (`id`,`taskId`,`currentStreak`,`bestStreak`,`lastDoneDate`,`graceUsed`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StreakEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindLong(3, entity.getCurrentStreak());
        statement.bindLong(4, entity.getBestStreak());
        if (entity.getLastDoneDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLastDoneDate());
        }
        statement.bindLong(6, entity.getGraceUsed());
      }
    }, new EntityDeletionOrUpdateAdapter<StreakEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `streaks` SET `id` = ?,`taskId` = ?,`currentStreak` = ?,`bestStreak` = ?,`lastDoneDate` = ?,`graceUsed` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StreakEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindLong(3, entity.getCurrentStreak());
        statement.bindLong(4, entity.getBestStreak());
        if (entity.getLastDoneDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLastDoneDate());
        }
        statement.bindLong(6, entity.getGraceUsed());
        statement.bindLong(7, entity.getId());
      }
    });
  }

  @Override
  public Object insertOrUpdateStreak(final StreakEntity streak,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfStreakEntity.upsert(streak);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getStreakForTask(final int taskId,
      final Continuation<? super StreakEntity> $completion) {
    final String _sql = "SELECT * FROM streaks WHERE taskId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<StreakEntity>() {
      @Override
      @Nullable
      public StreakEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfCurrentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStreak");
          final int _cursorIndexOfBestStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "bestStreak");
          final int _cursorIndexOfLastDoneDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastDoneDate");
          final int _cursorIndexOfGraceUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "graceUsed");
          final StreakEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTaskId;
            _tmpTaskId = _cursor.getInt(_cursorIndexOfTaskId);
            final int _tmpCurrentStreak;
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak);
            final int _tmpBestStreak;
            _tmpBestStreak = _cursor.getInt(_cursorIndexOfBestStreak);
            final String _tmpLastDoneDate;
            if (_cursor.isNull(_cursorIndexOfLastDoneDate)) {
              _tmpLastDoneDate = null;
            } else {
              _tmpLastDoneDate = _cursor.getString(_cursorIndexOfLastDoneDate);
            }
            final int _tmpGraceUsed;
            _tmpGraceUsed = _cursor.getInt(_cursorIndexOfGraceUsed);
            _result = new StreakEntity(_tmpId,_tmpTaskId,_tmpCurrentStreak,_tmpBestStreak,_tmpLastDoneDate,_tmpGraceUsed);
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
  public Flow<List<StreakEntity>> getAllStreaks() {
    final String _sql = "SELECT * FROM streaks";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"streaks"}, new Callable<List<StreakEntity>>() {
      @Override
      @NonNull
      public List<StreakEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfCurrentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStreak");
          final int _cursorIndexOfBestStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "bestStreak");
          final int _cursorIndexOfLastDoneDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastDoneDate");
          final int _cursorIndexOfGraceUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "graceUsed");
          final List<StreakEntity> _result = new ArrayList<StreakEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StreakEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpTaskId;
            _tmpTaskId = _cursor.getInt(_cursorIndexOfTaskId);
            final int _tmpCurrentStreak;
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak);
            final int _tmpBestStreak;
            _tmpBestStreak = _cursor.getInt(_cursorIndexOfBestStreak);
            final String _tmpLastDoneDate;
            if (_cursor.isNull(_cursorIndexOfLastDoneDate)) {
              _tmpLastDoneDate = null;
            } else {
              _tmpLastDoneDate = _cursor.getString(_cursorIndexOfLastDoneDate);
            }
            final int _tmpGraceUsed;
            _tmpGraceUsed = _cursor.getInt(_cursorIndexOfGraceUsed);
            _item = new StreakEntity(_tmpId,_tmpTaskId,_tmpCurrentStreak,_tmpBestStreak,_tmpLastDoneDate,_tmpGraceUsed);
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
