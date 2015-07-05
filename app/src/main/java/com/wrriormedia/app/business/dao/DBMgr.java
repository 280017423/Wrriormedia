package com.wrriormedia.app.business.dao;

import com.wrriormedia.app.util.DBUtil;
import com.wrriormedia.library.orm.BaseModel;
import com.wrriormedia.library.orm.DataAccessException;
import com.wrriormedia.library.orm.DataManager;
import com.wrriormedia.library.util.StringUtil;

import java.util.List;

/**
 * 数据库管理类
 *
 * @author zou.sq
 */
public class DBMgr {
    private DBMgr() {
    }

    /**
     * 保存model
     *
     * @param model 需保存的对象
     * @param <T>   泛型
     */
    public static <T extends BaseModel> void saveModel(final T model) {
        if (null == model) {
            return;
        }
        DataManager dataManager = DBUtil.getDataManager();
        try {
            dataManager.save(model);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

    }

    public static <T extends BaseModel> void saveModel(final T model, String primaryKey, String primaryValue) {
        if (null == model) {
            return;
        }
        DataManager dataManager = DBUtil.getDataManager();
        try {
            dataManager.save(model, primaryKey, primaryValue);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存models
     *
     * @param models 需保存的对象集合
     * @param <T>    泛型
     */
    public static <T extends BaseModel> void saveModels(final List<T> models) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (null == models || models.isEmpty()) {
                    return;
                }
                DataManager dataManager = DBUtil.getDataManager();
                try {
                    for (int i = 0; i < models.size(); i++) {
                        dataManager.save(models.get(i));
                    }
                } catch (DataAccessException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 获取BaseModel本地记录，升序
     *
     * @param type model类型
     * @param <T>  BaseModel类型子类
     * @return List<T> BaseModel类型集合
     */
    public static <T extends BaseModel> T getBaseModel(Class<T> type, String whereClause) {
        T results = null;
        DataManager dataManager = DBUtil.getDataManager();
        dataManager.open();
        try {
            results = dataManager.get(type, whereClause, null);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        dataManager.close();
        return results;
    }

    /**
     * 获取BaseModel本地记录，升序
     *
     * @param type model类型
     * @param <T>  BaseModel类型子类
     * @return List<T> BaseModel类型集合
     */
    public static <T extends BaseModel> List<T> getBaseModels(Class<T> type) {
        List<T> results = null;
        DataManager dataManager = DBUtil.getDataManager();
        dataManager.open();
        try {
            results = dataManager.getList(type, null, null, "_id asc", null);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        dataManager.close();
        return results;
    }

    public static <T extends BaseModel> List<T> getBaseModels(Class<T> type, String where) {
        return getBaseModels(type, where, "");
    }

    public static <T extends BaseModel> List<T> getBaseModels(Class<T> type, String where, String limit) {
        List<T> results = null;
        DataManager dataManager = DBUtil.getDataManager();
        dataManager.open();
        try {
            results = dataManager.getList(type, where, null, "_id asc", limit);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        dataManager.close();
        return results;
    }

    /**
     * 删除表
     *
     * @param type 指定的model
     * @param <T>  BaseModel的子类
     */
    public static <T extends BaseModel> void deleteTableFromDb(final Class<T> type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataManager dataManager = DBUtil.getDataManager();
                dataManager.open();
                dataManager.delete(type, null, null);
                dataManager.close();
            }
        }).start();
    }

    public static <T extends BaseModel> void delete(final Class<T> type, final String where) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataManager dataManager = DBUtil.getDataManager();
                dataManager.open();
                dataManager.delete(type, where, null);
                dataManager.close();
            }
        }).start();
    }

    public static <T extends BaseModel> void delete(final Class<T> type, final long id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataManager dataManager = DBUtil.getDataManager();
                dataManager.open();
                dataManager.delete(type, id);
                dataManager.close();
            }
        }).start();
    }

    /**
     * 删除数据库的表
     *
     * @param tableName 表名字
     * @param <T>       BaseModel的子类
     * @return boolean 是否删除成功
     */
    public static <T extends BaseModel> boolean deleteTableFromDb(String tableName) {
        boolean isSuccess;
        if (StringUtil.isNullOrEmpty(tableName)) {
            return false;
        }
        DataManager dataManager = DBUtil.getDataManager();
        dataManager.open();
        isSuccess = dataManager.delete(tableName, null, null);
        dataManager.close();
        return isSuccess;
    }

}
