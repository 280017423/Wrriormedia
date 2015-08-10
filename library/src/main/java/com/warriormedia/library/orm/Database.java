package com.warriormedia.library.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.warriormedia.library.util.EvtLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a database to be used by Android Active Record entities.
 *
 * @author JEREMYOT
 * @author Vladimir Kroz
 *         <p/>
 *         <p>
 *         This project based on and inspired by 'androidactiverecord' project
 *         written by JEREMYOT
 *         </p>
 */
public class Database {
    static final String TAG = "Database";
    static final String CNAME = Database.class.getSimpleName();
    private static final int SLEEP_TIME_MS_FOR_WAIT_DB_LOCK = 20;
    private static final String DB_IS_LOCKED_BY_OTHER_THREAD = "数据库已经被其他线程锁住，休眠20ms";

    private static Map<String, com.warriormedia.library.orm.DatabaseBuilder> BUILDERS = new HashMap<>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private SQLiteDatabase mSQLiteDatabase;
    private DatabaseOpenHelper mDatabaseOpenHelper;

    /**
     * Creates a new DatabaseWrapper object
     *
     * @param dbName    The file name to use for the SQLite database.
     * @param dbVersion Database version
     * @param context   The context used for database creation, its package name will
     *                  be used to place the database on external storage if any is
     *                  present, otherwise the context's application data directory.
     */
    Database(Context context, String dbName, int dbVersion, com.warriormedia.library.orm.DatabaseBuilder builder) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context, dbName, dbVersion, builder);
    }

    /**
     * Returns DatabaseBuilder object assosicted with Database
     *
     * @param dbName database name
     * @return DatabaseBuilder object assosicted with Database
     */
    public static com.warriormedia.library.orm.DatabaseBuilder getBuilder(String dbName) {
        return BUILDERS.get(dbName);
    }

    /**
     * Initializes Database framework. This method must be called for each used
     * database only once before using database. This is required for proper
     * setup static attributes of the Database
     *
     * @param builder DatabaseBuilder
     * @return
     */
    public static void setBuilder(com.warriormedia.library.orm.DatabaseBuilder builder) {
        BUILDERS.put(builder.getDatabaseName(), builder);
    }

    /**
     * @param ctx       上下文环境
     * @param dbName    数据库名
     * @param dbVersion 数据库版本
     * @param builder   DatabaseBuilder
     * @return 创建好的数据库对象
     */
    public static Database createInstance(Context ctx, String dbName, int dbVersion, com.warriormedia.library.orm.DatabaseBuilder builder) {
        return new Database(ctx, dbName, dbVersion, builder);
    }

    /**
     * Opens or creates the database file. Uses external storage if present,
     * otherwise uses local storage.
     */
    public void open() {
        if (mSQLiteDatabase != null && mSQLiteDatabase.isOpen()) {
            delayForAWhile();

            mSQLiteDatabase.close();
            mSQLiteDatabase = null;
        }

        r.lock();
        try {
            mSQLiteDatabase = mDatabaseOpenHelper.getReadableDatabase();
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        } finally {
            r.unlock();
        }
        EvtLog.d(TAG, CNAME + ".open(): new db obj " + mSQLiteDatabase.toString());
    }

    /**
     * 关闭数据库
     */
    public void close() {
        if (mSQLiteDatabase != null && mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase.close();
        }
        mSQLiteDatabase = null;
    }

    /**
     * 查询数据库是否已经打开
     *
     * @return 已经打开返回true
     */
    public boolean isOpen() {
        return mSQLiteDatabase != null && mSQLiteDatabase.isOpen();
    }

    /**
     * Insert into a table in the database.
     *
     * @param table      The table to insert into.
     * @param parameters The data.
     * @return the row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insert(String table, ContentValues parameters) {
        delayForAWhile();

        return mSQLiteDatabase.insert(table, null, parameters);
    }

    /**
     * Update a table in the database.
     *
     * @param table       The table to update.
     * @param values      The new values.
     * @param whereClause The condition to match (Don't include "where").
     * @param whereArgs   The arguments to replace "?" with.
     * @return The number of rows affected.
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        delayForAWhile();

        return mSQLiteDatabase.update(table, values, whereClause, whereArgs);
    }

    /**
     * Delete from a table in the database
     *
     * @param table       The table to delete from.
     * @param whereClause The condition to match (Don't include WHERE).
     * @param whereArgs   The arguments to replace "?" with.
     * @return The number of rows affected.
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        delayForAWhile();

        return mSQLiteDatabase.delete(table, whereClause, whereArgs);
    }

    /**
     * Execute a raw SQL query.
     *
     * @param sql Standard SQLite compatible SQL.
     * @return A cursor over the data returned.
     */
    public Cursor rawQuery(String sql) {
        return rawQuery(sql, null);
    }

    /**
     * Execute a single SQL statement that is not a query. For example, CREATE
     * TABLE, DELETE, INSERT, etc. Multiple statements separated by ;s are not
     * supported. it takes a write lock
     *
     * @param sql 要执行的语句
     */
    public void execSQL(String sql) {
        delayForAWhile();
        mSQLiteDatabase.execSQL(sql);
    }

    /**
     * Execute a single SQL statement that is not a query. For example, CREATE
     * TABLE, DELETE, INSERT, etc. Multiple statements separated by ;s are not
     * supported. it takes a write lock
     *
     * @param sql 要执行的语句
     */
    public void execSQL(String sql, Object[] bindArgs) {
        delayForAWhile();
        mSQLiteDatabase.execSQL(sql, bindArgs);
    }

    /**
     * Execute a raw SQL query.
     *
     * @param sql    Standard SQLite compatible SQL.
     * @param params The values to replace "?" with.
     * @return A cursor over the data returned.
     */
    public Cursor rawQuery(String sql, String[] params) {
        return mSQLiteDatabase.rawQuery(sql, params);
    }

    /**
     * Execute a query.
     *
     * @param table         The table to query.
     * @param selectColumns The columns to select.
     * @param where         The condition to match (Don't include "where").
     * @param whereArgs     The arguments to replace "?" with.
     * @return A cursor over the data returned.
     * @throws DataAccessException is database is null or closed
     */
    public Cursor query(String table, String[] selectColumns, String where, String[] whereArgs)
            throws DataAccessException {
        return query(false, table, selectColumns, where, whereArgs, null, null, null, null);
    }

    /**
     * Execute a query.
     *
     * @param distinct      是否包含重复结果
     * @param table         The table to query.
     * @param selectColumns The columns to select.
     * @param where         The condition to match (Don't include "where").
     * @param whereArgs     The arguments to replace "?" with.
     * @param groupBy       分组条件
     * @param having        分组过滤条件
     * @param orderBy       排序条件
     * @param limit         只取前几条，为null则取全部
     * @return A cursor over the data returned.
     * @throws DataAccessException is database is null or closed
     */
    public Cursor query(boolean distinct, String table, String[] selectColumns, String where, String[] whereArgs,
                        String groupBy, String having, String orderBy, String limit) throws DataAccessException {
        if (null == mSQLiteDatabase || !mSQLiteDatabase.isOpen()) {
            EvtLog.w(TAG, String.format("%s.query(): ERROR - db object is null or closed", CNAME));
            throw new DataAccessException(com.warriormedia.library.orm.Utils.ERR_DB_IS_NOT_OPEN);
        }

        return mSQLiteDatabase.query(distinct, table, selectColumns, where, whereArgs, groupBy, having, orderBy, limit);
    }

    /**
     * @return Returns array of database tables names
     * @throws DataAccessException 可能抛出的异常
     */
    public String[] getTables() throws DataAccessException {
        if (null == mSQLiteDatabase || !mSQLiteDatabase.isOpen()) {
            EvtLog.w(TAG, String.format("%s.getTables(): ERROR - db object is null or closed", CNAME));
            throw new DataAccessException(com.warriormedia.library.orm.Utils.ERR_DB_IS_NOT_OPEN);
        }

        Cursor c = query("sqlite_master", new String[]{"name"}, "type = ?", new String[]{"table"});
        List<String> tables = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                tables.add(c.getString(0));
            }
        } finally {
            c.close();
        }
        return tables.toArray(new String[0]);
    }

    /**
     * 查询表中所有的列
     *
     * @param table 要查的表
     * @return 列集合
     */
    public String[] getColumnsForTable(String table) {
        Cursor c = rawQuery(String.format("PRAGMA table_info(%s)", table));
        List<String> columns = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                columns.add(c.getString(c.getColumnIndex("name")));
            }
        } finally {
            c.close();
        }
        return columns.toArray(new String[0]);
    }

    /**
     * 查询数据库版本
     *
     * @return 版本号
     * @throws DataAccessException 可能抛出的异常
     */
    public int getVersion() throws DataAccessException {
        if (null == mSQLiteDatabase || !mSQLiteDatabase.isOpen()) {
            EvtLog.w(TAG, String.format("%s.getVersion(): ERROR - db object is null or closed", CNAME));
            throw new DataAccessException(com.warriormedia.library.orm.Utils.ERR_DB_IS_NOT_OPEN);
        }

        return mSQLiteDatabase.getVersion();
    }

    /**
     * 设置数据库版本
     *
     * @param version 要设置的版本
     * @throws DataAccessException 可能抛出的异常
     */
    public void setVersion(int version) throws DataAccessException {
        if (null == mSQLiteDatabase || !mSQLiteDatabase.isOpen()) {
            EvtLog.w(TAG, String.format("%s.setVersion(): ERROR - db object is null or closed", CNAME));
            throw new DataAccessException(com.warriormedia.library.orm.Utils.ERR_DB_IS_NOT_OPEN);
        }

        mSQLiteDatabase.setVersion(version);
    }

    /**
     * 开启一个事务
     */
    public void beginTransaction() {
        delayForAWhile();

        mSQLiteDatabase.beginTransaction();
    }

    /**
     * 结束并提交事务
     */
    public void endTransaction() {
        mSQLiteDatabase.setTransactionSuccessful();
        mSQLiteDatabase.endTransaction();
    }

    /**
     * 事务回滚
     */
    public void rollTransaction() {
        mSQLiteDatabase.endTransaction();
    }

    /**
     * 当其他线程锁住sqlite数据库时，当前线程延时20ms，轮询知道锁被释放
     */
    private void delayForAWhile() {
        while (mSQLiteDatabase.isDbLockedByOtherThreads()) {
            try {
                Thread.sleep(SLEEP_TIME_MS_FOR_WAIT_DB_LOCK);
                EvtLog.d(TAG, DB_IS_LOCKED_BY_OTHER_THREAD);
            } catch (InterruptedException e) {
                EvtLog.w(TAG, e);
            }
        }
    }
}
