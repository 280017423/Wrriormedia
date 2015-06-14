/**
 *
 */
package com.wrriormedia.library.util;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zeng.ww
 * @version 1.1.1
 */
public class UrlParameters {
    private Bundle mParameters = new Bundle();
    private List<String> mKeys = new ArrayList<>();

    /**
     * 构造函数
     */
    public UrlParameters() {

    }

    /**
     * 移除指定位置的参数
     *
     * @param key   键
     * @param value 值
     */
    public void add(String key, String value) {
        if (this.mKeys.contains(key)) {
            this.mParameters.putString(key, value);
        } else {
            this.mKeys.add(key);
            this.mParameters.putString(key, value);
        }
    }

    /**
     * 移除指定位置的参数
     *
     * @param key 键值
     */
    public void remove(String key) {
        mKeys.remove(key);
        this.mParameters.remove(key);
    }

    /**
     * 移除指定位置的参数
     *
     * @param i 索引值
     * @throws
     */
    public void remove(int i) {
        String key = this.mKeys.get(i);
        this.mParameters.remove(key);
        mKeys.remove(key);
    }

    /**
     * 获取参数键值
     *
     * @param key 键值
     * @return int 返回类型
     * @throws
     */
    public int getLocation(String key) {
        if (this.mKeys.contains(key)) {
            return this.mKeys.indexOf(key);
        }
        return -1;
    }

    /**
     * 获取参数键值
     *
     * @param location 位置
     * @return String 返回类型
     * @throws
     */
    public String getKey(int location) {
        if (location >= 0 && location < this.mKeys.size()) {
            return this.mKeys.get(location);
        }
        return "";
    }

    /**
     * 获取参数值
     *
     * @param key 键值
     * @return String 返回类型
     * @throws
     */
    public String getValue(String key) {
        return mParameters.getString(key);
    }

    /**
     * 获取参数值
     *
     * @param location 位置
     * @return String 返回类型
     * @throws
     */
    public String getValue(int location) {
        String key = this.mKeys.get(location);
        return mParameters.getString(key);
    }

    /**
     * 参数个数
     *
     * @return int 返回类型
     */
    public int size() {
        return mKeys.size();
    }

    /**
     * 添加参数
     *
     * @param parameters 参数值
     */
    public void addAll(UrlParameters parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            this.add(parameters.getKey(i), parameters.getValue(i));
        }

    }

    /**
     * 清除参数
     */
    public void clear() {
        this.mKeys.clear();
        this.mParameters.clear();
    }

}
