package com.warriormedia.library.app;

import com.pdw.gson.Gson;
import com.warriormedia.library.util.MessageException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * json 返回结果的帮助类
 *
 * @author Zou.sq
 * @version 1.0
 */
public class JsonResult {
    private static final String KEY_DATA = "data";
    private static final String KEY_STATUS = "status";
    private static final String KEY_CODE = "code";
    private static final String KEY_MSG = "msg";
    private static final String BLANK_STRING = "";
    private static final String BRACKET_STRING = "{}";
    private static final String STATUS_SUCCESS = "0";

    public String Data;
    public String State;
    public String Code;
    public String Msg;
    public String JsonString;
    private JSONObject mJsonObject;

    /**
     * 构造函数
     *
     * @param jsonString json格式的字符串
     * @throws JSONException json异常
     */
    public JsonResult(String jsonString) throws JSONException {
        // 解决字符串带BOM的问题
        if (jsonString != null && jsonString.startsWith("\ufeff")) {
            jsonString = jsonString.substring(1);
        }
        mJsonObject = new JSONObject(jsonString);
        JsonString = jsonString;
        if (mJsonObject.has(KEY_STATUS)) {
            JSONObject statusObject = new JSONObject(mJsonObject.getString(KEY_STATUS));
            if (statusObject.has(KEY_MSG)) {
                Msg = statusObject.getString(KEY_MSG);
            }
            if (statusObject.has(KEY_CODE)) {
                Code = statusObject.getString(KEY_CODE);
            }
        }
        if (mJsonObject.has(KEY_DATA)) {
            Data = mJsonObject.getString(KEY_DATA);
        }
    }

    /**
     * 返回结果是否正常
     *
     * @return 正常返回true，否则返回false
     */
    public Boolean isOK() {
        return Code.equals(STATUS_SUCCESS);
    }

    /**
     * 返回结果中是否存在 key
     *
     * @param key 指定的key
     * @return boolean
     */
    public boolean hasKey(String key) {
        return mJsonObject != null && mJsonObject.has(key);
    }

    /**
     * 返回data节点下是否存在 key
     *
     * @param key 指定的key
     * @return boolean
     */
    public boolean hasDataOfKey(String key) {
        try {
            JSONObject jsonObj = mJsonObject.getJSONObject(KEY_DATA);
            return jsonObj != null && jsonObj.has(key);
        } catch (JSONException e) {
            return false;
        }

    }

    /**
     * 根据key或者字符串的值
     *
     * @param key 关键字
     * @return 返回字符串的值
     * @throws JSONException    json异常
     * @throws MessageException 业务异常
     */
    public String getDataString(String key) throws JSONException, MessageException {
        if (!isOK()) {
            throw new MessageException(Data);
        }
        if (mJsonObject.has(KEY_DATA)) {
            JSONObject jsonObj = mJsonObject.getJSONObject(KEY_DATA);
            return jsonObj.optString(key);
        }
        return "";
    }

    /**
     * 根据key返回指定类型的实例
     *
     * @param <T>      指定类型的实例定义
     * @param key      关键字
     * @param classOfT 指定类型的定义
     * @return 返回指定类型的实例
     * @throws JSONException    json异常
     * @throws MessageException 业务异常
     */
    public <T> T getData(String key, Class<T> classOfT) throws JSONException, MessageException {
        if (!isOK()) {
            throw new MessageException(Data);
        }
        JSONObject jsonObject = mJsonObject.getJSONObject(KEY_DATA);
        String returnString = jsonObject.optString(key);
        if (returnString == null || returnString.trim().equals(BLANK_STRING)
                || returnString.trim().equals(BRACKET_STRING)) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(returnString, classOfT);
    }

    /**
     * 根据key返回指定类型的实例
     *
     * @param <T>  指定类型的实例定义
     * @param key  关键字
     * @param type 指定类型的定义
     * @return 返回指定类型的实例
     * @throws JSONException    json异常
     * @throws MessageException 业务异常
     */
    public <T> T getData(String key, Type type) throws JSONException, MessageException {
        if (!isOK()) {
            throw new MessageException(Data);
        }
        JSONObject jsonObj = mJsonObject.getJSONObject(KEY_DATA);
        String returnString = jsonObj.optString(key);
        if (returnString == null || returnString.trim().equals(BLANK_STRING)) {
            return null;
        }
        Gson gson = new Gson();
        return (T) gson.fromJson(returnString, type);
    }

    public <T> T getData(Type type) throws JSONException, MessageException {

        if (!isOK()) {
            throw new MessageException(Data);
        }
        if (!mJsonObject.has(KEY_DATA)) {
            return null;
        }
        String returnString = mJsonObject.getString(KEY_DATA);
        if (returnString == null || returnString.trim().equals(BLANK_STRING)) {
            return null;
        }
        Gson gson = new Gson();
        return (T) gson.fromJson(returnString, type);

    }

    /**
     * 根据key返回指定类型的实例
     *
     * @param <T>      指定类型的实例定义
     * @param classOfT 指定类型的定义
     * @return 返回指定类型的实例
     * @throws JSONException    json异常
     * @throws MessageException 业务异常
     */
    public <T> T getData(Class<T> classOfT) throws JSONException, MessageException {
        if (!isOK()) {
            throw new MessageException(Data);
        }
        Gson gson = new Gson();
        return gson.fromJson(Data, classOfT);
    }

    /**
     * 获取指定Key的JSONArray对象
     *
     * @param Key 关键字
     * @return JSONArray 对象
     * @throws JSONException    json异常
     * @throws MessageException 业务异常
     */
    public JSONArray getJSONArray(String Key) throws JSONException, MessageException {
        if (!isOK()) {
            throw new MessageException(Data);
        }
        if (!mJsonObject.has(KEY_DATA)) {
            return null;
        }
        try {
            JSONObject jsonObj = mJsonObject.getJSONObject(KEY_DATA);
            return jsonObj.getJSONArray(Key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
