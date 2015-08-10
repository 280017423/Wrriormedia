/**
 * 网络辅助类
 */
package com.warriormedia.library.util;

/**
 * @author wang.xy
 */
public interface INetworkState {
    /**
     * 检查网络是否可用
     *
     * @return 如果可用，返回true，不可用则返回false
     */
    boolean isNetworkAvailable();

    /**
     * 检查网络是否可用
     *
     * @return 如果可用，返回true，不可用则返回false
     */
    boolean isGPSAvailable();

    /**
     * @return 检查wifi网络是否可用
     */
    boolean isWifiAvailable();

    /**
     * @return 当前连接wifi的SSID
     */
    String getConnectionWifiSSID();

}
