package com.wrriormedia.library.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.pdw.gson.Gson;
import com.wrriormedia.library.util.EvtLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * sqlite 数据管理器
 *
 * @author colin.wang
 * @since 2013-03-29 zeng.ww 修改firstopen()方法，增加同步操作
 */
public abstract class DataManager {

    private static final String POINT = ".";

    private static final String DOT = ", ";

    private static final String SPACE = " ";

    private static final String TAG = "DataManager";

    private static final String OBJECT_CAN_NOT_NULL = "插入的对象不能为空。";
    private static final String ID_CAN_NOT_NULL = "使用Update方法时，_id必须有值";
    private static final String WHERE_CLAUSE_CAN_NOT_NULL = "更新条件不能为空，否则会更新整个数据库。";
    private static final String ID_WHERE_CLAUSE = "_id = ?";
    private static final String COLUMN_NOT_EXISITS = "指定的列不存在";
    private static final String SET_DATABASE_FIRST = "Set database first";
    private final Object mLock = new Object();
    private Database mDatabase;
    private Context mContext;
    private String mDBName;
    private int mDBVersion;
    private DatabaseBuilder mDatabaseBuilder;

    /**
     * 初始化数据库
     *
     * @param context         当前上下文
     * @param dbName          数据库名称
     * @param dbVersion       数据库版本
     * @param databaseBuilder 数据库表的描述类
     */
    protected DataManager(Context context, String dbName, int dbVersion, DatabaseBuilder databaseBuilder) {
        this.mContext = context;
        this.mDBName = dbName;
        this.mDBVersion = dbVersion;
        this.mDatabaseBuilder = databaseBuilder;

        setDatabase();
    }

    protected void setDatabase() {
        if (mDatabase == null) {
            mDatabase = new Database(mContext, mDBName, mDBVersion, mDatabaseBuilder);
        }
    }

    /**
     * 开启事务。该方法会调用open方法，自动打开数据库连接
     */
    public void beginTransaction() {
        if (mDatabase != null) {
            // open();
            mDatabase.beginTransaction();
        }
    }

    /**
     * 结束事务。该方法会调用close方法，自动关闭数据库连接
     */
    public void endTransaction() {
        if (mDatabase != null) {
            mDatabase.endTransaction();
            // close();
        }
    }

    /**
     * 回滚事务
     */
    public void rollBack() {
        if (mDatabase != null) {
            mDatabase.rollTransaction();
            // close();
        }
    }

    /**
     *
     */
    public void open() {
        // if(!mDatabase.isOpen()){
        // mDatabase.open();
        // }
    }

    /**
     *
     */
    public void close() {
        // mDatabase.close();
    }

    /**
     *
     */
    public void firstOpen() {
        synchronized (mLock) {
            if (mDatabase != null && !mDatabase.isOpen()) {
                mDatabase.open();
            }
        }

    }

    /**
     *
     */
    public void lastClose() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    /**
     * 根据指定的条件，获取单个实体。如果存在多条符合条件的记录，则只返回第一条记录
     *
     * @param <T>         实体类型
     * @param type        指定的实体类型
     * @param distinct
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @return 返回单个实体。如果存在多条符合条件的记录，则只返回第一条记录
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> T get(Class<T> type, boolean distinct, String[] selectColumns, String whereClause,
                                       String[] whereArgs, String groupBy, String having, String orderBy, String limit) throws DataAccessException {
        if (mDatabase == null) {
            throw new DataAccessException(SET_DATABASE_FIRST);
        }
        // 关闭时重新打开
        firstOpen();
        T entity = null;

        Cursor c = null;
        try {
            c = mDatabase.query(false, com.wrriormedia.library.orm.Utils.getTableName(type), selectColumns, whereClause, whereArgs, groupBy,
                    having, orderBy, limit);
            if (c.moveToNext()) {
                entity = type.newInstance();
                entity = com.wrriormedia.library.orm.Utils.inflate(c, entity);
            }
        } catch (IllegalAccessException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        } catch (InstantiationException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return entity;
    }

    public <T extends BaseModel> T get(Class<T> type, String whereClause, String[] whereArgs)
            throws DataAccessException {
        return get(type, false, null, whereClause, whereArgs, null, null, null, "1");
    }

    /**
     * 根据指定的条件，获取实体的列表
     *
     * @param <T>         泛型实体类，必须是BaseModel的子类
     * @param type        指定的实体类型
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @return 返回实体列表
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> List<T> getList(Class<T> type, String whereClause, String[] whereArgs)
            throws DataAccessException {
        return getList(type, false, whereClause, whereArgs, null, null, null, null);
    }

    /**
     * 根据指定的条件，获取实体的列表
     *
     * @param <T>         泛型实体类，必须是BaseModel的子类
     * @param type        指定的实体类型
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @param pageSize    每页记录条数，取正值
     * @param pageIndex   取第几页的记录，取正值，从1开始
     * @return 返回实体列表
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> List<T> getList(Class<T> type, String whereClause, String[] whereArgs, int pageSize,
                                                 int pageIndex) throws DataAccessException {
        return getList(type, false, whereClause, whereArgs, null, null, null, pageSize, pageIndex);
    }

    /**
     * 根据指定的条件，获取实体的列表
     *
     * @param <T>         泛型实体类，必须是BaseModel的子类
     * @param type        指定的实体类型
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @param orderBy     排序字段
     * @param limit       取数据的条数
     * @return 返回实体列表
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> List<T> getList(Class<T> type, String whereClause, String[] whereArgs, String orderBy,
                                                 String limit) throws DataAccessException {
        return getList(type, false, whereClause, whereArgs, null, null, orderBy, null);
    }

    /**
     * 根据指定的条件，获取实体的列表
     *
     * @param <T>         泛型实体类，必须是BaseModel的子类
     * @param type        指定的实体类型
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @param orderBy     排序字段
     * @param pageSize    每页记录条数，取正值
     * @param pageIndex   取第几页的记录，取正值，从1开始
     * @return 返回实体列表
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> List<T> getList(Class<T> type, String whereClause, String[] whereArgs, String orderBy,
                                                 int pageSize, int pageIndex) throws DataAccessException {
        return getList(type, false, whereClause, whereArgs, null, null, orderBy, pageSize, pageIndex);
    }

    /**
     * 根据指定的条件，获取实体的列表
     *
     * @param <T>         泛型实体类，必须是BaseModel的子类
     * @param type        指定的实体类型
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @param groupBy     分组条件
     * @param having      分组条件
     * @param orderBy     排序字段
     * @param limit       取数据的条数
     * @return 返回实体列表
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> List<T> getList(Class<T> type, boolean distinct, String whereClause,
                                                 String[] whereArgs, String groupBy, String having, String orderBy, String limit) throws DataAccessException {
        if (mDatabase == null) {
            throw new DataAccessException(SET_DATABASE_FIRST);
        }
        // 关闭时重新打开
        firstOpen();
        List<T> resultList = new ArrayList<>();

        Cursor c = mDatabase.query(distinct, com.wrriormedia.library.orm.Utils.getTableName(type), null, whereClause, whereArgs, groupBy, having,
                orderBy, limit);

        try {
            while (c.moveToNext()) {
                // T entity = EntitiesMap.instance().get(type,
                // c.getLong(c.getColumnIndex("_id")));
                // if (entity == null) {
                T entity = type.newInstance();
                entity = com.wrriormedia.library.orm.Utils.inflate(c, entity);
                // }
                resultList.add(entity);
            }
        } catch (IllegalAccessException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        } catch (InstantiationException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        } finally {
            c.close();

        }

        return resultList;
    }

    /**
     * 根据指定的条件，获取实体的列表
     *
     * @param <T>         泛型实体类，必须是BaseModel的子类
     * @param type        指定的实体类型
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @param groupBy     分组条件
     * @param having      分组条件
     * @param orderBy     排序字段
     * @param pageSize    每页记录条数，取正值
     * @param pageIndex   取第几页的记录，取正值，从1开始
     * @return 返回实体列表
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> List<T> getList(Class<T> type, boolean distinct, String whereClause,
                                                 String[] whereArgs, String groupBy, String having, String orderBy, int pageSize, int pageIndex)
            throws DataAccessException {
        if (mDatabase == null) {
            throw new DataAccessException(SET_DATABASE_FIRST);
        }
        // 关闭时重新打开
        firstOpen();
        List<T> resultList = new ArrayList<>();

        if (pageSize <= 0 || pageIndex <= 0) {
            return resultList;
        }
        int startIndex = pageSize * (pageIndex - 1);
        String limit = startIndex + ", " + pageSize;

        Cursor c = mDatabase.query(distinct, com.wrriormedia.library.orm.Utils.getTableName(type), null, whereClause, whereArgs, groupBy, having,
                orderBy, limit);
        try {

            // skip pages before pageIdex
            c.moveToPosition(pageSize * (pageIndex - 1) - 1);

            // begin to get data
            while (c.moveToNext()) {
                // T entity = EntitiesMap.instance().get(type,
                // c.getLong(c.getColumnIndex("_id")));
                // if (entity == null) {
                T entity = type.newInstance();
                entity = com.wrriormedia.library.orm.Utils.inflate(c, entity);
                // }
                resultList.add(entity);
            }
        } catch (IllegalAccessException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        } catch (InstantiationException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        } finally {
            c.close();

        }

        return resultList;
    }

    /**
     * @param <T>
     * @param <T2>
     * @param distinct    是否去掉结果中的重复项
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @param groupBy     分组字段
     * @param having      分组过滤条件
     * @param orderBy     排序字段
     * @param returnCount 取数据的条数
     * @param type1       要关联的第一个表
     * @param type2       要关联的第二个表
     * @return T类型的列表
     * @throws DataAccessException
     */
    public <T extends BaseModel, T2 extends BaseModel> List<T> getList(boolean distinct, String whereClause,
                                                                       String[] whereArgs, String groupBy, String having, String orderBy, String returnCount, Class<T> type1,
                                                                       Class<T2> type2) throws DataAccessException {
        if (mDatabase == null) {
            throw new DataAccessException(SET_DATABASE_FIRST);
        }
        // 关闭时重新打开
        firstOpen();
        List<T> resultList = new ArrayList<>();
        BaseModel returnModel;
        try {
            returnModel = type1.newInstance();

            // for column_name
            List<Field> columns = returnModel.getColumnFields();
            StringBuilder returnCols = new StringBuilder();
            String returnTableName = com.wrriormedia.library.orm.Utils.getTableName(type1);
            for (int i = 0; i < columns.size(); ++i) {
                Field column = columns.get(i);
                column.setAccessible(true);
                String colName = com.wrriormedia.library.orm.Utils.toSQLName(column.getName());
                if (i != columns.size() - 1) {
                    returnCols.append(returnTableName).append(POINT).append(colName + DOT);
                } else {
                    returnCols.append(returnTableName).append(POINT).append(colName);
                }
            }
            StringBuilder sql = new StringBuilder("select");
            if (distinct) {
                sql.append(" distinct");
            }
            sql.append(SPACE).append(returnCols);
            sql.append(" from ");

            // for table_name
            sql.append(com.wrriormedia.library.orm.Utils.getTableName(type1)).append(DOT);
            sql.append(com.wrriormedia.library.orm.Utils.getTableName(type2));

            // for where clause
            if (whereClause != null) {
                StringBuilder whereStr = new StringBuilder(whereClause);
                if (whereArgs != null) {
                    for (int i = 0; i < whereArgs.length; ++i) {
                        whereStr.replace(0, whereStr.length(), whereArgs[i]);
                    }
                }
                sql.append(" where ").append(whereStr);
            }

            if (groupBy != null) {
                sql.append(" group by ").append(groupBy);
            }
            if (having != null) {
                sql.append(" having ").append(having);
            }
            if (orderBy != null) {
                sql.append(" order by ").append(orderBy);
            }

            if (returnCount != null) {
                sql.append(" limit ").append(returnCount);
            }
            // EvtLog.d("debug", "getList, " + sql.toString());

            // execute
            Cursor c = mDatabase.rawQuery(sql.toString());
            // Cursor c = mDatabase.query(distinct,
            // Utils.getTableName(returnModel.getClass()), null, whereClause,
            // whereArgs, groupBy, having,
            // orderBy, limit);
            try {
                while (c.moveToNext()) {
                    // T entity = EntitiesMap.instance().get(type,
                    // c.getLong(c.getColumnIndex("_id")));
                    // if (entity == null) {
                    T entity = type1.newInstance();
                    entity = com.wrriormedia.library.orm.Utils.inflate(c, entity);
                    // }
                    resultList.add(entity);
                }
            } catch (IllegalAccessException e) {
                throw new DataAccessException(e.getLocalizedMessage());
            } catch (InstantiationException e) {
                throw new DataAccessException(e.getLocalizedMessage());
            } finally {
                c.close();

            }

        } catch (IllegalAccessException e1) {
            throw new DataAccessException(e1.getLocalizedMessage());
        } catch (InstantiationException e1) {
            throw new DataAccessException(e1.getLocalizedMessage());
        }

        return resultList;
    }

    /**
     * @param <T>
     * @param <T2>
     * @param distinct    是否去掉结果中的重复项
     * @param whereClause 查询条件
     * @param whereArgs   查询参数，他会按照顺序替换 whereClause 中间的？号
     * @param groupBy     分组字段
     * @param having      分组过滤条件
     * @param orderBy     排序字段
     * @param pageSize    每页记录条数，取正值
     * @param pageIndex   取第几页的记录，取正值，从1开始
     * @param type1       要关联的第一个表
     * @param type2       要关联的第二个表
     * @return T类型的列表
     * @throws DataAccessException
     */
    public <T extends BaseModel, T2 extends BaseModel> List<T> getList(boolean distinct, String whereClause,
                                                                       String[] whereArgs, String groupBy, String having, String orderBy, int pageSize, int pageIndex,
                                                                       Class<T> type1, Class<T2> type2) throws DataAccessException {
        if (mDatabase == null) {
            throw new DataAccessException(SET_DATABASE_FIRST);
        }
        firstOpen();// 关闭时重新打开
        List<T> resultList = new ArrayList<>();
        BaseModel returnModel;
        try {
            returnModel = type1.newInstance();
            // for column_name
            List<Field> columns = returnModel.getColumnFields();
            StringBuilder returnCols = new StringBuilder();
            String returnTableName = com.wrriormedia.library.orm.Utils.getTableName(type1);
            for (int i = 0; i < columns.size(); ++i) {
                Field column = columns.get(i);
                column.setAccessible(true);
                String colName = com.wrriormedia.library.orm.Utils.toSQLName(column.getName());
                if (i != columns.size() - 1) {
                    returnCols.append(returnTableName).append(POINT).append(colName + DOT);
                } else {
                    returnCols.append(returnTableName).append(POINT).append(colName);
                }
            }
            StringBuilder sql = new StringBuilder("select");
            if (distinct) {
                sql.append(" distinct");
            }
            sql.append(SPACE).append(returnCols);
            sql.append(" from ");

            // for table_name
            sql.append(com.wrriormedia.library.orm.Utils.getTableName(type1)).append(DOT);
            sql.append(com.wrriormedia.library.orm.Utils.getTableName(type2));

            // for where clause
            if (whereClause != null) {
                StringBuilder whereStr = new StringBuilder(whereClause);
                if (whereArgs != null) {
                    for (int i = 0; i < whereArgs.length; ++i) {
                        whereStr.replace(0, whereStr.length(), whereArgs[i]);
                    }
                }
                sql.append(" where ").append(whereStr);
            }

            if (groupBy != null) {
                sql.append(" group by ").append(groupBy);
            }
            if (having != null) {
                sql.append(" having ").append(having);
            }
            if (orderBy != null) {
                sql.append(" order by ").append(orderBy);
            }

            int startIndex = pageSize * (pageIndex - 1);
            String limit = startIndex + ", " + pageSize;
            if (pageSize >= 0) {
                sql.append(" limit ").append(limit);
            }

            EvtLog.d("debug", "getList, " + sql.toString());

            // execute
            Cursor c = mDatabase.rawQuery(sql.toString());
            try {
                while (c.moveToNext()) {
                    T entity = type1.newInstance();
                    entity = com.wrriormedia.library.orm.Utils.inflate(c, entity);
                    resultList.add(entity);
                }
            } catch (IllegalAccessException e) {
                throw new DataAccessException(e.getLocalizedMessage());
            } catch (InstantiationException e) {
                throw new DataAccessException(e.getLocalizedMessage());
            } finally {
                c.close();

            }
        } catch (IllegalAccessException e1) {
            throw new DataAccessException(e1.getLocalizedMessage());
        } catch (InstantiationException e1) {
            throw new DataAccessException(e1.getLocalizedMessage());
        }
        return resultList;
    }

    public <T extends BaseModel> int countColumn(Class<T> type, boolean distinct, String[] column) {
        try {
            Cursor cursor = mDatabase.query(distinct, com.wrriormedia.library.orm.Utils.getTableName(type), column, null, null, null, null, null,
                    null);
            return cursor.getCount();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 插入记录
     *
     * @param model 实体类的实例
     * @return 返回插入记录的id
     * @throws DataAccessException 数据库访问异常
     */
    public long insert(BaseModel model) throws DataAccessException {
        if (model == null) {
            throw new DataAccessException(OBJECT_CAN_NOT_NULL);
        }
        // 关闭时重新打开
        firstOpen();
        List<Field> columns = model.getID() > 0 ? model.getColumnFields() : model.getColumnFieldsWithoutID();
        ContentValues values = new ContentValues();
        String typeString;
        for (Field column : columns) {
            try {
                typeString = column.getType().getName();
                column.setAccessible(true);
                // 字段不为空时才插入值
                Object fieldValue = column.get(model);
                if (fieldValue != null
                        && (!(typeString.equals("java.util.ArrayList") || typeString.equals("java.util.List")) && column
                        .getType().getSuperclass() != BaseModel.class)) {
                    values.put(com.wrriormedia.library.orm.Utils.toSQLName(column.getName()), String.valueOf(fieldValue));
                } else if (fieldValue != null
                        && (com.wrriormedia.library.orm.Utils.isInstanceofBaseModel(column.getType()) || typeString.equals("java.util.ArrayList") || typeString
                        .equals("java.util.List"))) {
                    // 如果该字段继承自BaseModel；也可以保存到数据库中，转化为Json字符串保存
                    values.put(com.wrriormedia.library.orm.Utils.toSQLName(column.getName()), new Gson().toJson(fieldValue));
                }
            } catch (IllegalAccessException e) {
                throw new DataAccessException(e.getLocalizedMessage());
            }
        }

        long id;
        id = mDatabase.insert(model.getTableName(), values);
        return id;
    }

    /**
     * @param <T>   泛型实体类，必须是BaseModel的子类。 实例model的_id必须赋值，否则不能更新
     * @param model 实体的实例。 实例model的_id必须赋值，否则不能更新
     * @return 受影响的记录数
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> int update(BaseModel model) throws DataAccessException {
        if (model == null) {
            throw new DataAccessException(OBJECT_CAN_NOT_NULL);
        }

        if (model.getID() <= 0) {
            throw new DataAccessException(ID_CAN_NOT_NULL);
        }
        // 关闭时重新打开
        firstOpen();
        List<Field> columns = model.getColumnFieldsWithoutID();
        ContentValues values = new ContentValues(columns.size());
        String typeString;
        for (Field column : columns) {
            try {
                typeString = column.getType().getName();
                column.setAccessible(true);
                // 字段不为空时才插入值
                Object fieldValue = column.get(model);
                if (fieldValue != null
                        && (!(typeString.equals("java.util.ArrayList") || typeString.equals("java.util.List")) && column
                        .getType().getSuperclass() != BaseModel.class)) {
                    values.put(com.wrriormedia.library.orm.Utils.toSQLName(column.getName()), String.valueOf(fieldValue));
                } else if (fieldValue != null
                        && (com.wrriormedia.library.orm.Utils.isInstanceofBaseModel(column.getType()) || typeString.equals("java.util.ArrayList") || typeString
                        .equals("java.util.List"))) {
                    // 如果该字段继承自BaseModel；也可以保存到数据库中，转化为Json字符串保存
                    values.put(com.wrriormedia.library.orm.Utils.toSQLName(column.getName()), new Gson().toJson(fieldValue));
                } else {
                    // 如果没有只，用空覆盖
                    values.put(com.wrriormedia.library.orm.Utils.toSQLName(column.getName()), "");
                }
            } catch (IllegalArgumentException e) {
                throw new DataAccessException(COLUMN_NOT_EXISITS + column.getName());
            } catch (IllegalAccessException e) {
                throw new DataAccessException(COLUMN_NOT_EXISITS + column.getName());
            }
        }
        return updateByClause(model.getClass(), values, ID_WHERE_CLAUSE,
                new String[]{String.valueOf(model.getID())});
    }

    /**
     * @param <T>         泛型实体类，必须是BaseModel的子类。 实例model的_id可以没有赋值
     * @param model       实体的实例
     * @param whereClause
     * @param whereArgs
     * @param type
     * @return 受影响的记录数
     * @throws DataAccessException 数据库访问异常
     */
    public <T extends BaseModel> int updateByClause(Class<T> type, BaseModel model, String whereClause,
                                                    String[] whereArgs) throws DataAccessException {
        if (model == null) {
            throw new DataAccessException(OBJECT_CAN_NOT_NULL);
        }

        if (whereClause == null) {
            throw new DataAccessException(WHERE_CLAUSE_CAN_NOT_NULL);
        }
        // 关闭时重新打开
        firstOpen();
        List<Field> columns = model.getColumnFieldsWithoutID();
        ContentValues values = new ContentValues(columns.size());
        for (Field column : columns) {
            try {
                column.setAccessible(true);
                Object fieldValue = column.get(model);
                if (null != fieldValue) {
                    values.put(com.wrriormedia.library.orm.Utils.toSQLName(column.getName()), String.valueOf(fieldValue));
                } else {
                    values.put(com.wrriormedia.library.orm.Utils.toSQLName(column.getName()), "");
                }
            } catch (IllegalArgumentException e) {
                throw new DataAccessException(COLUMN_NOT_EXISITS + column.getName());
            } catch (IllegalAccessException e) {
                throw new DataAccessException(COLUMN_NOT_EXISITS + column.getName());
            }
        }
        return updateByClause(model.getClass(), values, whereClause, whereArgs);
    }

    /**
     * 根据条件修改多条数据
     *
     * @param <T>         泛型类
     * @param type        需要修改的表的实体类
     * @param values      需要修改的内容
     * @param whereClause 查询条件
     * @param whereArgs   查询条件对应的值
     * @return 返回受影响的行数
     */
    public <T extends BaseModel> int updateByClause(Class<T> type, ContentValues values, String whereClause,
                                                    String[] whereArgs) {
        // 关闭时重新打开
        firstOpen();
        int rowAffect = 0;
        try {
            String table = com.wrriormedia.library.orm.Utils.getTableName(type);
            rowAffect = mDatabase.update(table, values, whereClause, whereArgs);
        } catch (Exception e) {
            EvtLog.w(TAG, e);
        }
        return rowAffect;
    }

    /**
     * 实体保存方法，如果已经存在则更新，否则插入新纪录
     *
     * @param model 保存的实例
     * @return 保存实例的主键值
     * @throws DataAccessException 数据库异常
     */
    public long save(BaseModel model) throws DataAccessException {
        return save(model, ID_WHERE_CLAUSE, model.getID() + "");
    }

    public long save(BaseModel model, String primaryKey, String primaryValue) throws DataAccessException {
        if (model == null) {
            throw new DataAccessException(OBJECT_CAN_NOT_NULL);
        }
        // 关闭时重新打开
        firstOpen();
        long id = 0;
        BaseModel existModel = get(model.getClass(), primaryKey, new String[]{primaryValue});
        if (existModel != null) {
            // 根据存在的_ID,update
            model.setID(existModel.getID());
            update(model);
        } else {
            id = insert(model);
        }
        return id;
    }

    /**
     * 删除指定主键的实例
     *
     * @param <T>  泛型类
     * @param type 要删除的记录类型
     * @param id   要删除记录的主键
     * @return 删除成功后，返回true；否则返回false
     */
    public <T extends BaseModel> boolean delete(Class<T> type, long id) {
        // 关闭时重新打开
        firstOpen();
        return delete(type, ID_WHERE_CLAUSE, new String[]{String.valueOf(id)});
    }

    /**
     * 删除指定主键的实例
     *
     * @param <T>         泛型类
     * @param type        要删除的记录类型
     * @param whereClause where条件
     * @param whereArgs   where参数
     * @return 删除成功后，返回true；否则返回false
     */
    public <T extends BaseModel> boolean delete(Class<T> type, String whereClause, String[] whereArgs) {
        return delete(com.wrriormedia.library.orm.Utils.getTableName(type), whereClause, whereArgs);
    }

    public <T extends BaseModel> boolean delete(String tableName, String whereClause, String[] whereArgs) {
        boolean result = true;
        // 关闭时重新打开
        firstOpen();
        try {
            mDatabase.delete(tableName, whereClause, whereArgs);
        } catch (Exception e) {
            result = false;
            EvtLog.w(TAG, e);
        }
        return result;
    }

    /**
     * select 查询方法，可以进行跨表查询
     *
     * @param sql 查询的语句
     * @return 符合条件的Cursor
     */
    public Cursor rawQuery(String sql) {
        // 关闭时重新打开
        firstOpen();
        return rawQuery(sql, null);
    }

    /**
     * select 查询方法，可以进行跨表查询
     *
     * @param sql           查询的语句
     * @param selectionArgs sql查询语句中参数的值
     * @return 符合条件的Cursor
     */
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        // 关闭时重新打开
        firstOpen();
        return mDatabase.rawQuery(sql, selectionArgs);
    }

    /**
     * Execute a single SQL statement that is not a query. For example, CREATE
     * TABLE, DELETE, INSERT, etc. Multiple statements separated by ;s are not
     * supported. it takes a write lock
     *
     * @param sql 执行的语句
     */
    public void execSQL(String sql) {
        // 关闭时重新打开
        firstOpen();
        mDatabase.execSQL(sql);
    }

    public void execSQL(String sql, Object[] bindArgs) {
        // 关闭时重新打开
        firstOpen();
        mDatabase.execSQL(sql, bindArgs);
    }

    /**
     * @return 返回数据库是否已经打开
     */
    public boolean isDatabaseOpen() {
        return mDatabase != null && mDatabase.isOpen();
    }
}
