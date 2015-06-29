package com.wrriormedia.library.orm;

import com.wrriormedia.library.orm.annotation.Transient;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 实体类基类，需要保存数据到sqlite的实体，都需要从该类继承
 * <p>
 * 目前支持下面的几种数据类型：int, java.lang.Integer, long, java.lang.Long, java.lang.String<br>
 * </p>
 * 注意：1、各个字段的类型最好使用可空类型，如 java.lang.Integer 等；<br>
 * 2、不建议使用Date等类型，使用java.lang.Long替换；<br>
 * 3、
 *
 * @author 王先佑
 * @since 2013-09-24 huang.b 3.3.1编码 添加对Model的私有属性支持，添加Transient注解<br>
 */
public class BaseModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1907916979605330513L;
    public long _id;

    /**
     * This entities row id.
     *
     * @return The SQLite row id.
     */
    public long getID() {
        return _id;
    }

    /**
     * This entities row id.
     *
     * @param id the specified entities row id
     */
    public void setID(long id) {
        _id = id;
    }

    String getTableName() {
        return com.wrriormedia.library.orm.Utils.toSQLName(getClass().getSimpleName());
    }

    /**
     * 获取类除id以外的公有字段，不包含以 m_ 或 s_ 开头的字段
     *
     * @return 类的字段数组列表.
     */
    String[] getColumnsWithoutID() {
        List<String> columns = new ArrayList<>();
        for (Field field : getColumnFieldsWithoutID()) {
            field.setAccessible(true);
            columns.add(field.getName());
        }

        return columns.toArray(new String[0]);
    }

    /**
     * 获取类公有的字段，不包含以 m_ 或 s_ 开头的字段，如果子类和父类有相同的属性名，不管类型是否一样，只取一个，是哪一个就不确定了
     *
     * @return An array of fields for this class.
     */
    List<Field> getColumnFields() {
        Field[] fields = getDeclaredFields(getClass());
        List<Field> columns = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            Transient mTransient = field.getAnnotation(Transient.class);
            if (mTransient != null) {
                continue;
            }
            if (!Modifier.isStatic(field.getModifiers()) && columnNames.indexOf(field.getName()) == -1) {
                columns.add(field);
                columnNames.add(field.getName());
            }
        }

        return columns;
    }

    /**
     * 获取类除id以外的字段，不包含以 m_ 或 s_ 开头的字段，如果子类和父类有相同的属性名，不管类型是否一样，只取一个，是哪一个就不确定了
     *
     * @return 类的字段列表.
     */
    List<Field> getColumnFieldsWithoutID() {
        Field[] fields = getDeclaredFields(getClass());
        List<Field> columns = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            Transient mTransient = field.getAnnotation(Transient.class);
            if (mTransient != null) {
                continue;
            }
            if (!field.getName().startsWith("_id") && !Modifier.isStatic(field.getModifiers())
                    && columnNames.indexOf(field.getName()) == -1) {
                columns.add(field);
                columnNames.add(field.getName());
            }
        }
        return columns;
    }

    public Field[] getDeclaredFields(Class<?> clazz) {
        List<Field> listFields = new ArrayList<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field[] field = clazz.getDeclaredFields();
                listFields.addAll(Arrays.asList(field));
            } catch (Exception e) {
                // 这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                // 如果这里的异常打印或者往外抛，则就不会执行clazz =
                // clazz.getSuperclass(),最后就不会进入到父类中了

            }
        }
        return listFields.toArray(new Field[]{});
    }
}
