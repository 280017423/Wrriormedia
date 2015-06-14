package com.wrriormedia.library.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * List处理工具
 *
 * @author tan.xx
 */
public class ListUtil {

    private static final String TAG = "ListUtil";

    /**
     * list深度拷贝
     *
     * @param resourceList 数据源
     * @return List
     */
    @SuppressWarnings({"rawtypes"})
    public static List deepCopy(List resourceList) {
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        ByteArrayInputStream byteIn = null;
        ObjectInputStream in = null;
        try {
            out = new ObjectOutputStream(byteout);
            out.writeObject(resourceList);
            byteIn = new ByteArrayInputStream(byteout.toByteArray());
            in = new ObjectInputStream(byteIn);
            return (List) in.readObject();
        } catch (IOException e) {
            EvtLog.w(TAG, "deepCopy error:" + e);
        } catch (ClassNotFoundException e) {
            EvtLog.w(TAG, "deepCopy error:" + e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (byteIn != null) {
                    byteIn.close();
                    byteIn = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                EvtLog.d(TAG, e.toString());
            }
        }
        return null;

    }
}
