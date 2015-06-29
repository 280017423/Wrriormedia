package com.wrriormedia.library.orm;

import android.database.Cursor;

import com.pdw.gson.Gson;
import com.wrriormedia.library.util.EvtLog;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ORM的辅助方法类
 *
 * @author 王先佑
 * @since 2013-08-22 17:47 huang.b 添加数据库对于List数组存储支持；
 */
public class Utils {
    public static final String TAG = "Utils";

    public static final String ERR_DB_IS_NOT_OPEN = "数据库处于关闭状态，请确认是否已打开数据库";
    public static final String ERR_INSERT_FAILED = "插入操作失败. Internal error.";
    public static final String ID = "_id";

    /**
     * 将java属性名称转化为sql列的命名，如：entityIsAName 为 entity_IS_A_NAME。
     * <p>
     * 如果是小写，则转化为大写；若是大写，则在字母前加下划线“_”，如：<br>
     * "AbCd"->"AB_CD" <br>
     * "ABCd"->"AB_CD"<br>
     * "AbCD"->"AB_CD"<br>
     * "ShowplaceDetailsVO"->"SHOWPLACE_DETAILS_VO"<br>
     * </p>
     *
     * @param javaNotation java名称
     * @return 转化后的Sql名称
     */
    public static String toSQLName(String javaNotation) {
        if (ID.equalsIgnoreCase(javaNotation)) {
            return ID;
        }

        StringBuilder sb = new StringBuilder();
        char[] buf = javaNotation.toCharArray();

        for (int i = 0; i < buf.length; i++) {
            char prevChar = (i > 0) ? buf[i - 1] : 0;
            char c = buf[i];
            char nextChar = (i < buf.length - 1) ? buf[i + 1] : 0;
            boolean isFirstChar = i == 0;

            if (isFirstChar || Character.isLowerCase(c)) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c) || Character.isDigit(c)) { // 增加字段带数字的处理
                if (Character.isLetterOrDigit(prevChar)) {
                    if (Character.isLowerCase(prevChar)) {
                        sb.append('_').append(Character.toUpperCase(c));
                    } else if (nextChar > 0 && Character.isLowerCase(nextChar)) {
                        sb.append('_').append(Character.toUpperCase(c));
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * 将sql列名称转化为java的属性或者方法命名，如：entity_IS_A_NAME 为 entityIsAName。
     * <p>
     * 如果字母有下划线“_”则转为大写；否则转化为小写
     * </p>
     *
     * @param sqlNotation Sql名称
     * @return 转化后的java名称
     */
    public static String toJavaMethodName(String sqlNotation) {
        StringBuilder dest = new StringBuilder();
        char[] src = sqlNotation.toCharArray();

        for (int i = 0; i < src.length; i++) {
            char c = src[i];
            boolean isFirstChar = i == 0;

            if (isFirstChar || c != '_') {
                dest.append(Character.toLowerCase(c));
            } else {
                i++;
                if (i < src.length) {
                    dest.append(src[i]);
                }
            }
        }

        return dest.toString();
    }

    /**
     * 将Sql的表名转化为类名，如 entity_IS_A_NAME to entityIsAName;
     * <p>
     * 如果没有下划线，则转化为小写；如果有下划线，则忽略下划线，并且将下一个字母转化为大写
     * </p>
     *
     * @param sqlNotation Sql表名
     * @return 转为后的类名称
     */
    public static String toJavaClassName(String sqlNotation) {
        StringBuilder sb = new StringBuilder();
        char[] buf = sqlNotation.toCharArray();
        for (int i = 0; i < buf.length; i++) {
            char c = buf[i];
            if (i == 0) {
                sb.append(buf[i]);
            } else if (c != '_') {
                sb.append(Character.toLowerCase(c));
            } else {
                i++;
                if (i < buf.length) {
                    sb.append(buf[i]);
                }
            }
        }
        return sb.toString();
    }

    static <T extends BaseModel> String getTableName(Class<T> clazz) {
        return toSQLName(clazz.getSimpleName());
    }

    /**
     * Inflate entity entity using the current row from the given cursor.
     *
     * @param cursor The cursor to get object data from.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    static <T extends BaseModel> T inflate(Cursor cursor, BaseModel entity) throws DataAccessException {
        try {
            entity = entity.getClass().newInstance();
        } catch (IllegalAccessException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        } catch (InstantiationException e) {
            throw new DataAccessException(e.getLocalizedMessage());
        }

        for (Field field : entity.getColumnFields()) {
            String typeString = null;
            String colName = null;
            field.setAccessible(true);
            try {
                typeString = field.getType().getName();
                colName = toSQLName(field.getName());
                if (cursor.isNull(cursor.getColumnIndex(colName))) {
                    continue;
                }
                if (typeString.equals("long") || typeString.equals("java.lang.Long")) {
                    field.set(entity, cursor.getLong(cursor.getColumnIndex(colName)));
                } else if (typeString.equals("java.lang.String")) {
                    field.set(entity, cursor.getString(cursor.getColumnIndex(colName)));
                } else if (typeString.equals("double") || typeString.equals("java.lang.Double")) {
                    field.set(entity, cursor.getDouble(cursor.getColumnIndex(colName)));
                } else if (typeString.equals("java.lang.Boolean") || typeString.equals("boolean")) {
                    String fieldValue = cursor.getString(cursor.getColumnIndex(colName));
                    field.set(entity, fieldValue != null && fieldValue.equals("true"));
                } else if (typeString.equals("[B")) {
                    field.set(entity, cursor.getBlob(cursor.getColumnIndex(colName)));
                } else if (typeString.equals("int") || typeString.equals("java.lang.Integer")) {
                    field.set(entity, cursor.getInt(cursor.getColumnIndex(colName)));
                } else if (typeString.equals("float") || typeString.equals("java.lang.Float")) {
                    field.set(entity, cursor.getFloat(cursor.getColumnIndex(colName)));
                } else if (typeString.equals("short") || typeString.equals("java.lang.Short")) {
                    field.set(entity, cursor.getShort(cursor.getColumnIndex(colName)));
                } else if (typeString.equals("java.sql.timestamp")) {
                    long l = cursor.getLong(cursor.getColumnIndex(colName));
                    field.set(entity, new Timestamp(l));
                } else if (typeString.equals("java.util.Date")) {
                    long l = cursor.getLong(cursor.getColumnIndex(colName));
                    field.set(entity, new Date(l));
                } else if (isInstanceofBaseModel(field.getType())
                        || (typeString.equals("java.util.ArrayList") || typeString.equals("java.util.List"))) {
                    // 如果字段继承自BaseModel，，需要进行Json字符串解析；
                    String fieldValue = cursor.getString(cursor.getColumnIndex(colName));
                    EvtLog.d(TAG, "inflate:" + field.getClass());
                    field.set(entity, new Gson().fromJson(fieldValue, field.getGenericType()));
                } else {
                    throw new DataAccessException("Class cannot be read from Sqlite3 database.");
                }
            } catch (IllegalArgumentException e) {
                throw new DataAccessException(e.getLocalizedMessage());
            } catch (IllegalAccessException e) {
                throw new DataAccessException(e.getLocalizedMessage());
            } catch (Exception e) {
                EvtLog.w(TAG, e);
            }
        }

        // EntitiesMap.instance().set(entity);

        return (T) entity;
    }

    /**
     * 根据java字段类型，转化为sqlite的列的类型
     *
     * @param c 字段类型定义
     * @return sqlite列的类型
     */
    static String getSQLiteTypeString(Class<?> c) {
        String result = "text";
        String name = c.getName();

        // 如果继承自BaseModel，说明可以保存到数据库中，不过转化为Json字符串再保存
        if ("java.lang.String".equals(name) || "string".equals(name) || "java.util.ArrayList".equals(name)
                || "java.util.List".equals(name) || isInstanceofBaseModel(c)) {
            result = "text";
        } else if ("short".equals(name) || "int".equals(name) || "java.lang.Integer".equals(name)
                || "long".equals(name) || "java.lang.Long".equals(name) || "java.sql.Timestamp".equals(name)
                || "java.util.Date".equals(name)) {
            result = "int";
        } else if ("double".equals(name) || "java.lang.Double".equals(name) || "float".equals(name)
                || "java.lang.Float".equals(name)) {
            result = "real";
        } else if ("[B".equals(name)) {
            result = "blob";
        } else if ("java.lang.Boolean".equals(name) || "boolean".equals(name)) {
            result = "bool";
        } else {
            throw new IllegalArgumentException("类不能保存到Sqlite数据库，请查看是否存在不支持的类型！" + name);
        }

        return result;
    }

    /**
     * 判断字节码是否继承至BaseModel
     *
     * @param c Class
     * @return 是否继承至BaseModel
     */
    static boolean isInstanceofBaseModel(Class<?> c) {
        return c.getSuperclass() != null && c.getSuperclass() != Object.class && c.getSuperclass() == BaseModel.class;
    }
}
