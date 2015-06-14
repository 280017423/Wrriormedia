/**
 * 网络辅助类
 */
package com.wrriormedia.library.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author wang.xy
 */
public class NetUtil {

    private static final String TAG = "NetUtil";
    /**
     * 客户端IP地址，当网络切换时，需要重新调用;如果获取不到时为空字符串
     */
    public static String HOST_IP = "";
    private static INetworkState NETWORK_STATE = new com.wrriormedia.library.util.NetworkState();

    static {
        refreshHostIP();
    }

    /**
     * 设置网络状态的方法
     *
     * @param networkState 网络状态
     */
    public static void setNetworkState(INetworkState networkState) {
        if (networkState == null) {
            throw new NullPointerException("传入的网络状态不能为空");
        }

        NETWORK_STATE = networkState;
    }

    /**
     * 检查网络是否可用
     *
     * @return 如果可用，返回true，不可用则返回false
     */
    public static boolean isNetworkAvailable() {
        return NETWORK_STATE.isNetworkAvailable();
    }

    /**
     * 检查网络是否可用
     *
     * @return 如果可用，返回true，不可用则返回false
     */
    public static boolean isGPSAvailable() {
        return NETWORK_STATE.isGPSAvailable();
    }

    /**
     * @return 检查wifi网络是否可用
     */
    public static boolean isWifiAvailable() {
        return NETWORK_STATE.isWifiAvailable();
    }

    /**
     * 判断当前 网络是否是wifi
     *
     * @param context 上下文对象
     * @return 判断当前网络是否是wifi
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 重新获取客户端IP地址，当网络切换时，需要调用该方法
     */
    public static void refreshHostIP() {
        HOST_IP = getHostIP();
    }

    /**
     * 获取当前连接wifi的SSID
     *
     * @return 当前连接wifi的SSID
     */
    public static String getConnectionWifiSSID() {
        return NETWORK_STATE.getConnectionWifiSSID();
    }

    /**
     * 获取IP地址
     *
     * @return 返回客户端IP地址，获取地址不成功时，返回空字符串
     */
    private static String getHostIP() {
        String hostIP = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en != null) {
                while (en.hasMoreElements()) {
                    NetworkInterface intf = en.nextElement();
                    if (intf != null) {
                        Enumeration<InetAddress> ipAddr = intf.getInetAddresses();
                        while (ipAddr.hasMoreElements()) {
                            InetAddress inetAddress = ipAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()) {
                                hostIP = inetAddress.getHostAddress();
                                break;
                            }
                        }
                    }
                    if (hostIP != null && !"".equals(hostIP)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            EvtLog.w(TAG, "未获取到访问的IP地址");
        }
        return hostIP;
    }

    /**
     * 获取设备IP地址（获取不到IP时使用的这个）
     *
     * @return String 设备IP地址
     */
    public static String getIP() {
        String ipAddress;
        StringBuilder ipSb = new StringBuilder();
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                while (inetAddressEnumeration.hasMoreElements()) {
                    InetAddress inetAddress = inetAddressEnumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                            && inetAddress.isSiteLocalAddress()) {
                        ipSb.append(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException ex) {
            EvtLog.w(TAG, "未获取到访问的IP地址");
        }
        ipAddress = ipSb.toString();
        return ipAddress;
    }
}
