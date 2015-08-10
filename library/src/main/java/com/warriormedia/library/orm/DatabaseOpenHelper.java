package com.warriormedia.library.orm;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Sqlite 辅助类，用于创建sqlite数据库
 *
 * @author 王先佑
 * @since 2012/08/17 zeng.ww 添加ormDropTable,initTableByClass方法
 */
class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseOpenHelper";

    private static final String orm_create_prefix = "@orm.create";
    private static final String orm_drop_prefix = "@orm.droptable";
    DatabaseBuilder mBuilder;
    int mVersion;
    Context mContext;

    /**
     * 构造函数
     *
     * @param context
     * @param dbPath
     * @param dbVersion
     * @param builder
     */
    public DatabaseOpenHelper(Context context, String dbPath, int dbVersion,
                              DatabaseBuilder builder) {
        super(context, dbPath, null, dbVersion);
        mBuilder = builder;
        mVersion = dbVersion;
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String dataName = mBuilder.getDatabaseName();
        EvtLog.d(TAG, "DataBase onCreate " + dataName + " Starting");
        for (String table : mBuilder.getTables()) {
            String sqlStr = null;
            try {
                sqlStr = mBuilder.getSQLCreate(table);
            } catch (DataAccessException e) {
                EvtLog.e(this.getClass().getName(), e);
            }
            if (sqlStr != null) {
                db.execSQL(sqlStr);
            }
        }
        db.setVersion(mVersion);
        EvtLog.d(TAG, "DataBase onCreate " + dataName + " End");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dataName = mBuilder.getDatabaseName();
        EvtLog.d(TAG, "DataBase onUpgrade " + dataName + " Starting");
        try {
            for (int currentVersion = oldVersion; currentVersion < newVersion; currentVersion++) {
                // 差异更新
                ArrayList<String> scripts = getUpgradeScript(currentVersion,
                        currentVersion + 1);
                EvtLog.d(TAG, "update database version from :" + currentVersion
                        + " to: " + (currentVersion + 1));
                // 无升级脚本，则默认先删除表，再创建表
                if (scripts == null || scripts.size() == 0) {
                    initTableByClass(db);
                }
                // 有升级脚本，则执行升级脚本
                else {
                    for (int i = 0; i < scripts.size(); ++i) {
                        String sql = scripts.get(i);
                        if (!("".equals(sql.trim()) || sql.trim().startsWith(
                                "--"))) {
                            if (sql.trim().startsWith(orm_create_prefix)) {
                                // 创建表
                                ormCreate(db, sql);
                            } else if (sql.trim().startsWith(orm_drop_prefix)) {
                                // 删除表
                                ormDropTable(db, sql);
                            } else {
                                db.execSQL(scripts.get(i));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            EvtLog.w(TAG, e);
            EvtLog.d(TAG, "update database error...");
            String sql = "@orm.droptable();";
            ormDropTable(db, sql);
            initTableByClass(db);
        }
        EvtLog.d(TAG, "DataBase onUpgrade " + dataName + " End");

    }

    /**
     * 通过Model创建数据表
     *
     * @param db 参数
     * @throws
     */
    private void initTableByClass(SQLiteDatabase db) {
        try {
            for (String table : mBuilder.getTables()) {
                String sqlStr = mBuilder.getSQLDrop(table);
                db.execSQL(sqlStr);
            }
            onCreate(db);
            EvtLog.d(TAG, "init Table By Class Success !");
        } catch (Exception e) {
            EvtLog.w(TAG, "init Table By Class Error !");
        }
    }

    /**
     * 创建表
     *
     * @param db              数据源
     * @param ormCreateString 创建表语句
     */
    private void ormCreate(SQLiteDatabase db, String ormCreateString) {
        if (ormCreateString == null) {
            return;
        }
        try {
            String table = ormCreateString.substring(
                    ormCreateString.indexOf("(") + 1,
                    ormCreateString.indexOf(")"));
            String sqlStr = mBuilder.getSQLCreate(com.warriormedia.library.orm.Utils.toSQLName(table));
            if (sqlStr != null) {
                db.execSQL(sqlStr);
            }
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
    }

    /**
     * 删除表
     *
     * @param ormDropString 删除表语句
     * @return void 返回类型
     * @throws
     */
    private void ormDropTable(SQLiteDatabase db, String ormDropString) {
        String sqlStr = "";
        Cursor cursor = null;
        String sysTablePrefix = "android";
        try {
            if (ormDropString == null) {
                return;
            }
            EvtLog.d(TAG, "Drop Table Start ...");
            String table = ormDropString.substring(
                    ormDropString.indexOf("(") + 1, ormDropString.indexOf(")"));
            if (!StringUtil.isNullOrEmpty(table)) {
                sqlStr = mBuilder.getSQLDrop(com.warriormedia.library.orm.Utils.toSQLName(table));
                if (sqlStr != null) {
                    db.execSQL(sqlStr);
                }
            } else {
                cursor = db.query(true, "sqlite_master",
                        new String[]{"name"}, "type=\"table\"", null, null,
                        null, null, null);
                // 循环删除所有表
                while (cursor.moveToNext()) {
                    table = cursor.getString(0);
                    if ("".equals(table.trim())
                            || table.trim().startsWith(sysTablePrefix)) {
                        continue;
                    }
                    sqlStr = mBuilder.getSQLDrop(table);
                    if (sqlStr != null) {
                        db.execSQL(sqlStr);
                    }
                }
            }
            EvtLog.d(TAG, "Drop Table End ...");
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
    }

    private ArrayList<String> getUpgradeScript(int oldVersion, int newVersion) {
        AssetManager assetMgr = mContext.getAssets();
        ArrayList<String> sqls = new ArrayList<>();
        try {
            EvtLog.d(TAG, "update database :" + mBuilder.getDatabaseName());
            InputStream inputStream = assetMgr.open(mBuilder.getDatabaseName()
                    + "_" + oldVersion + "_" + newVersion);
            DataInputStream dataInput = new DataInputStream(inputStream);
            StringBuilder sql = new StringBuilder();
            String clause;
            while ((clause = dataInput.readLine()) != null) {
                clause = clause.trim();
                // 过滤空行和注释
                if ("".equals(clause) || clause.startsWith("--")) {
                    continue;
                }
                // 查找语句结束的分号
                sql.append(" ").append(clause);
                if (clause.endsWith(";")) {
                    sqls.add(sql.toString());
                    sql.delete(0, sql.length());
                }
            }
        } catch (IOException e) {
            EvtLog.w(TAG, e);
        }
        return sqls;
    }

}
