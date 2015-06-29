package com.wrriormedia.library.orm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines DB schema definition statements from provided Java classes. <br/>
 * Use this class to specify structure of your DB. Call method addClass() for
 * each table and provide corresponding Java class. <br/>
 * Normally this class instantiated only once at the very beginning of the
 * application lifecycle. Once instantiated it is used by underlying
 * SQLDatabaseHelper and provides SQL statements for create or upgrade of DB
 * schema.
 *
 * @author JEREMYOT
 * @author Vladimir Kroz
 *         <p>
 *         This project based on and inspired by 'androidactiverecord' project
 *         written by JEREMYOT
 *         </p>
 */
public class DatabaseBuilder {

    @SuppressWarnings({"rawtypes"})
    Map<String, Class> mClasses = new HashMap<>();
    String mDBName;

    /**
     * Create a new DatabaseBuilder for a database.
     *
     * @param dbName 数据库名称
     */
    public DatabaseBuilder(String dbName) {
        this.mDBName = dbName;
    }

    /**
     * Add or update a table for an AREntity that is stored in the current
     * database.
     *
     * @param <T> Any ActiveRecordBase type.
     * @param c   The class to reference when updating or adding a table.
     */
    public <T extends BaseModel> void addClass(Class<T> c) {
        mClasses.put(c.getSimpleName(), c);
    }

    /**
     * Returns list of DB tables according to classes added to a schema map
     *
     * @return names in SQL notation
     */
    @SuppressWarnings("rawtypes")
    public String[] getTables() {
        String[] ret = new String[mClasses.size()];
        Class[] arr = new Class[mClasses.size()];
        arr = mClasses.values().toArray(arr);
        for (int i = 0; i < arr.length; i++) {
            Class c = arr[i];
            ret[i] = com.wrriormedia.library.orm.Utils.toSQLName(c.getSimpleName());
        }
        return ret;
    }

    /**
     * Returns SQL create statement for specified table
     *
     * @param table name in SQL notation
     * @param <T>
     * @return string
     * @throws DataAccessException
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseModel> String getSQLCreate(String table)
            throws DataAccessException {
        StringBuilder sb = null;
        Class<T> c = getClassBySqlName(table);
        T e = null;
        if (null != c) {
            try {
                e = c.newInstance();
            } catch (IllegalAccessException e1) {
                throw new DataAccessException(e1.getLocalizedMessage());
            } catch (InstantiationException e1) {
                throw new DataAccessException(e1.getLocalizedMessage());
            }
            sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table)
                    .append(" (_id integer primary key");
            for (Field column : e.getColumnFieldsWithoutID()) {
                column.setAccessible(true);
                String jname = column.getName();
                String qname = com.wrriormedia.library.orm.Utils.toSQLName(jname);
                Class<?> columntype = column.getType();
                String sqliteType = com.wrriormedia.library.orm.Utils.getSQLiteTypeString(columntype);
                sb.append(", ").append(qname).append(" ").append(sqliteType);
            }
            sb.append(")");

        }
        return sb.toString();
    }

    /**
     * Returns SQL drop table statement for specified table
     *
     * @param table name in SQL notation
     * @return 返回删除语句
     */
    public String getSQLDrop(String table) {
        return "DROP TABLE IF EXISTS " + table;
    }

    /**
     * 获取数据库名称
     *
     * @return 返回数据库名
     */
    public String getDatabaseName() {
        return mDBName;
    }

    @SuppressWarnings("rawtypes")
    private Class getClassBySqlName(String table) {
        String jName = com.wrriormedia.library.orm.Utils.toJavaClassName(table);
        return mClasses.get(jName);
    }
}
