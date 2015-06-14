package com.wrriormedia.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.pdw.gson.Gson;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.StringUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * SharedPreference操作类； 在操作实体类的时候做为SharePreference的实体Model，必须带无参构造方法(默认即可)；<br>
 * Model注意地方：<br>
 * 1、数据类型只能为String,Integer等基本对象类型，不能为int,boolean基本类型；<br>
 * 2、数据如果为final,private修饰是不会保存到SharePreference；<br>
 * 3、尽量对应的Model文件名保存为:对应Model中final变量SHAREPREFERENCES_NAME中；
 * 使用SharePreference即可以整个Model操作，也可以根据单个key操作；
 *
 * @author huang.b
 * @version 2013-7-9 下午3:28:55
 */
public class SharedPreferenceUtil {

    // 无效标记
    public static final int INVALID_CODE = -1;
    private static final String TAG = "SharedPreferenceUtil";
    private static String separator = "_";

    /**
     * 根据KEY 查询字符串值
     *
     * @param mContext 上下文对象
     * @param key      键名字
     * @return String 返回的字符串值
     */
    public static String getStringValueByKey(Context mContext, String fileName, String key) {
        String value = "";
        if (!StringUtil.isNullOrEmpty(key)) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            value = sharedPreferences.getString(key, "");
        }
        return value;
    }

    /**
     * 根据KEY 查询Boolean值
     *
     * @param mContext 上下文对象
     * @param key      键名
     * @return boolean 返回的boolean值
     */
    public static boolean getBooleanValueByKey(Context mContext, String fileName, String key) {
        boolean value = false;
        if (!StringUtil.isNullOrEmpty(key)) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            value = sharedPreferences.getBoolean(key, false);
        }
        return value;
    }

    /**
     * 根据KEY 查询Integer值
     *
     * @param mContext 上下文对象
     * @param key      键名
     * @return int 查询Integer值
     */
    public static int getIntegerValueByKey(Context mContext, String fileName, String key) {
        int value = INVALID_CODE;
        if (!StringUtil.isNullOrEmpty(key)) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            value = sharedPreferences.getInt(key, INVALID_CODE);
        }
        return value;
    }

    /**
     * 根据KEY 查询float值
     *
     * @param mContext 上下文对象
     * @param key      键名
     * @return float 查询float值
     */
    public static float getFloatValueByKey(Context mContext, String fileName, String key) {
        float value = INVALID_CODE;
        if (!StringUtil.isNullOrEmpty(key)) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            value = sharedPreferences.getFloat(key, INVALID_CODE);
        }
        return value;
    }

    /**
     * 避免多个地方同时修改所以加synchronized; 保存本地数据，在读取的时候就不需要加了；
     *
     * @param mContext 上下文对象
     * @param key      键名
     * @param value    需要存储的值
     */
    public static synchronized void saveValue(Context mContext, String fileName, String key, Object value) {
        if (!StringUtil.isNullOrEmpty(key)) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            Editor editor = sharedPreferences.edit();
            if (value != null) {
                if (value instanceof String) {
                    editor.putString(key, String.valueOf(value));
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Float) {
                    editor.putFloat(key, (Float) value);
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                }
                editor.apply();
            }
        }
    }

    /**
     * 避免多个地方同时修改所以加synchronized;
     *
     * @param mContext 上下文对象
     * @param fileName 存储的文件名
     * @param key      待移除数据的key
     */
    public static synchronized void removeValue(Context mContext, String fileName, String key) {
        if (!StringUtil.isNullOrEmpty(key)) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }
    }

    /**
     * 如果同一个对象可以在SharePreference中保存多个实例，如WeiboInfoModel; 可以有Sina，QQ，RenRen
     * 直接将对象保存，字段类型只能为String,Integer,Long,Boolean,Float,Double类型，如果有其它类型，
     * 那么需要另做对象保存；
     *
     * @param mContext 上下文对象
     * @param fileName 文件名
     * @param obj      保存的对象
     */
    public static synchronized void saveObject(Context mContext, String fileName, Object obj) {
        Class<?> clazz = obj.getClass();
        EvtLog.d(TAG, "saveObject:" + clazz.getName() + "----" + obj.toString());
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        // 获取实体类的所有属性，返回Field数组
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.getName() == null) {
                    continue;
                }
                if (field.get(obj) == null) {
                    editor.remove(field.getName());
                    continue;
                }
                // 如果类型是String// 如果类型是Integer// 如果类型是Boolean 是封装类
                if (field.getType().toString().equals("class java.lang.String")) {
                    editor.putString(field.getName(), field.get(obj).toString());
                } else if (field.getType().toString().equals("class java.lang.Integer")
                        || field.getType().toString().equals("int")) {
                    editor.putInt(field.getName(), Integer.parseInt(field.get(obj).toString()));
                } else if (field.getType().toString().equals("class java.lang.Boolean")
                        || field.getType().toString().equals("boolean")) {
                    editor.putBoolean(field.getName(), Boolean.parseBoolean(field.get(obj).toString()));
                } else if (field.getType().toString().equals("class java.lang.Long")
                        || field.getType().toString().equals("long")) {
                    editor.putLong(field.getName(), Long.parseLong(field.get(obj).toString()));
                } else if (field.getType().toString().equals("class java.lang.Double")
                        || field.getType().toString().equals("double")
                        || field.getType().toString().equals("class java.lang.Float")
                        || field.getType().toString().equals("float")) {
                    editor.putFloat(field.getName(), Float.parseFloat(field.get(obj).toString()));
                } else if (field.getType().toString().equals("java.util.ArrayList")
                        || field.getType().toString().equals("java.util.List")) {
                    editor.putString(field.getName(), new Gson().toJson(field.get(obj).toString()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        editor.apply();
    }

    /**
     * @param mContext 上下文对象
     * @param fileName 文件名
     * @param clazz    类型
     * @return Object 获取到的对象
     */
    public static Object getObject(Context mContext, String fileName, Class<?> clazz) {
        Object object = null;
        try {
            object = clazz.newInstance();
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            // 获取实体类的所有属性，返回Field数组
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> typeClass = field.getType();
                Constructor<?> con;
                Object valueObj = null;
                // 如果类型是String// 如果类型是Integer// 如果类型是Boolean 是封装类
                if (typeClass.toString().equals("class java.lang.String")) {
                    con = typeClass.getConstructor(String.class);
                    String value = sharedPreferences.getString(field.getName(), "");
                    valueObj = con.newInstance(value);
                } else if (typeClass.toString().equals("class java.lang.Integer")) {
                    con = typeClass.getConstructor(String.class);
                    Integer value = sharedPreferences.getInt(field.getName(), -1);
                    valueObj = con.newInstance(value + "");
                } else if (typeClass.toString().equals("int")) {
                    valueObj = sharedPreferences.getInt(field.getName(), -1);
                } else if (typeClass.toString().equals("class java.lang.Boolean")) {
                    con = typeClass.getConstructor(String.class);
                    Boolean value = sharedPreferences.getBoolean(field.getName(), false);
                    valueObj = con.newInstance(value + "");
                } else if (typeClass.toString().equals("boolean")) {
                    valueObj = sharedPreferences.getBoolean(field.getName(), false);
                } else if (typeClass.toString().equals("class java.lang.Long")) {
                    con = typeClass.getConstructor(String.class);
                    Long value = sharedPreferences.getLong(field.getName(), -1);
                    valueObj = con.newInstance(value + "");
                } else if (typeClass.toString().equals("long")) {
                    valueObj = sharedPreferences.getLong(field.getName(), -1);
                } else if (typeClass.toString().equals("class java.lang.Double")
                        || typeClass.toString().equals("class java.lang.Float")) {
                    con = typeClass.getConstructor(String.class);
                    Float value = sharedPreferences.getFloat(field.getName(), -1f);
                    valueObj = con.newInstance(value + "");
                } else if (typeClass.toString().equals("double") || typeClass.toString().equals("float")) {
                    valueObj = sharedPreferences.getFloat(field.getName(), -1f);
                } else if (typeClass.toString().equals("java.util.ArrayList")
                        || typeClass.toString().equals("java.util.List")) {
                    String value = sharedPreferences.getString(field.getName(), "");
                    valueObj = new Gson().fromJson(value, field.getGenericType());
                }
                if (valueObj != null) {
                    if (!Modifier.toString(field.getModifiers()).endsWith("final")) {
                        field.set(object, valueObj);
                    }
                }
            }
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 清除当前Model的本地存储；
     *
     * @param mContext 上下文对象
     * @param fileName 文件名字
     */
    public static void clearObject(Context mContext, String fileName) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
