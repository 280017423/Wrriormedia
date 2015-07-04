package com.wrriormedia.app.util;

import android.content.Context;

import com.wrriormedia.app.business.dao.DBMgr;
import com.wrriormedia.app.model.DownloadModel;
import com.wrriormedia.app.model.DownloadTextModel;
import com.wrriormedia.library.app.HtcApplicationBase;
import com.wrriormedia.library.orm.DataManager;
import com.wrriormedia.library.orm.DatabaseBuilder;
import com.wrriormedia.library.util.PackageUtil;

/**
 * 数据库初始化类
 *
 * @author zou.sq
 * @since 2013-03-28 zou.sq 添加SpecialDishModel表初始化操作
 */
public class DBUtil {
    private static DatabaseBuilder DATABASE_BUILDER;
    private static PMRDataManager INSTANCE;

    // 获取数据库实例
    static {
        if (DATABASE_BUILDER == null) {
            DATABASE_BUILDER = new DatabaseBuilder(PackageUtil.getConfigString("db_name"));
            DATABASE_BUILDER.addClass(DownloadModel.class);
            DATABASE_BUILDER.addClass(DownloadTextModel.class);
        }
    }

    /**
     * 清除所有的数据表
     */
    public static void clearAllTables() {
        if (null != DATABASE_BUILDER) {
            String[] tables = DATABASE_BUILDER.getTables();
            for (String tableName : tables) {
                DBMgr.deleteTableFromDb(tableName);
            }
        }
    }

    public static DataManager getDataManager() {
        if (INSTANCE == null) {
            INSTANCE = new PMRDataManager(HtcApplicationBase.getInstance().getBaseContext(), DATABASE_BUILDER);
        }
        return INSTANCE;
    }

    static class PMRDataManager extends DataManager {
        protected PMRDataManager(Context context, DatabaseBuilder databaseBuilder) {
            super(context, PackageUtil.getConfigString("db_name"), PackageUtil.getConfigInt("db_version"), databaseBuilder);
        }
    }
}
