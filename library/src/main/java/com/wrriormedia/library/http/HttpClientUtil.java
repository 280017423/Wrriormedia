package com.wrriormedia.library.http;

import android.os.Bundle;

import com.wrriormedia.library.app.JsonResult;
import com.wrriormedia.library.util.EvtLog;
import com.wrriormedia.library.util.MessageException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * 网络访问辅助类
 *
 * @author wang.xy
 */

/**
 * @author alina
 * @version 2012-08-02，zeng.ww，增加httpGet,getString,parseUrl,decodeUrl,encodeUrl等方法
 *          <br>
 *          2012-10-31，tan.xx，get， post新增带参数requestType处理等方法<br>
 */
public class HttpClientUtil {
    private static final String TAG = "HttpClientUtil";
    private static final String INTERROGATION = "?";
    /**
     * 这个变量需要重构<br>
     * 这个属性不能用于判断网络是否可用，判断网络是否可用请用 NetUtil.isNetworkAvailable() 方法；<br>
     * 这个方法仅用于用于判断返回的json是否有异常，如果有异常，表示有可能是使用了错误的网络，如CMCC等；
     */
    public static boolean LAST_REQUEST_IS_OK = true;
    private static IPDWHttpClient PDW_HTTP_CLIENT = new DefaultPDWHttpClient();

    /**
     * @param pdwHttpClient IPDWHttpClient对象
     */
    public static void setPDWHttpClient(IPDWHttpClient pdwHttpClient) {
        if (pdwHttpClient == null) {
            throw new NullPointerException("http client 不能为空");
        }

        PDW_HTTP_CLIENT = pdwHttpClient;
    }

    /**
     * @param cookieStore 存储coockie
     */
    public static void setCookieStore(CookieStore cookieStore) {
        PDW_HTTP_CLIENT.setCookieStore(cookieStore);
    }

    /**
     * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @return json数据 json数据异常
     * @throws NetworkException 网络异常
     * @throws MessageException 业务异常
     */
    public static JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams)
            throws NetworkException, MessageException {
        return PDW_HTTP_CLIENT.post(url, httpParams, postParams);
    }

    /**
     * 通过put方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @return json数据 json数据异常
     * @throws NetworkException 网络异常
     * @throws MessageException 业务异常
     */
    public static JsonResult put(String url, HttpParams httpParams, List<NameValuePair> postParams)
            throws NetworkException, MessageException {
        return PDW_HTTP_CLIENT.put(url, httpParams, postParams);
    }

    /**
     * 通过delete方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @return json数据 json数据异常
     * @throws NetworkException 网络异常
     * @throws MessageException 业务异常
     */
    public static JsonResult delete(String url, HttpParams httpParams, List<NameValuePair> postParams)
            throws NetworkException, MessageException {
        return PDW_HTTP_CLIENT.delete(url, httpParams, postParams);
    }

    /**
     * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @param fileList   文件集合
     * @return json数据 json数据异常
     * @throws NetworkException             网络异常
     * @throws MessageException             业务异常
     * @throws UnsupportedEncodingException
     */
    public static JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams, List<File> fileList)
            throws NetworkException, MessageException, UnsupportedEncodingException {
        return PDW_HTTP_CLIENT.post(url, httpParams, postParams, fileList);
    }

    /**
     * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url       url地址
     * @param getParams 附加在Url后面的参数
     * @return json数据
     * @throws MessageException
     * @throws NetworkException 异常信息
     */
    public static JsonResult get(String url, List<NameValuePair> getParams) throws NetworkException, MessageException {
        return PDW_HTTP_CLIENT.get(url, getParams);
    }

    /**
     * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param getParams  附加在Url后面的参数
     * @return json数据
     * @throws NetworkException 异常信息
     * @throws MessageException 消息异常
     */
    public static JsonResult get(String url, HttpParams httpParams, List<NameValuePair> getParams)
            throws NetworkException, MessageException {
        return PDW_HTTP_CLIENT.get(url, getParams);
    }

    /**
     * @param url       请求url
     * @param getParams 请求参数
     * @return string 构建后的url
     */
    public static String buildUrl(String url, List<NameValuePair> getParams) {
        if (getParams != null && getParams.size() > 0) {
            String returnUrl = url;
            if (!url.contains(INTERROGATION)) {
                returnUrl = url + INTERROGATION;
            }
            String tempParams = "";
            for (int i = 0; i < getParams.size(); i++) {
                NameValuePair nameValuePair = getParams.get(i);
                tempParams = tempParams + "&" + nameValuePair.getName() + "="
                        + URLEncoder.encode(nameValuePair.getValue());
            }
            returnUrl = returnUrl + tempParams.substring(1);
            EvtLog.d(TAG, returnUrl);

            return returnUrl;
        }

        return url;
    }

    /**
     * @param url
     * @param httpParams
     * @param getParams
     * @return http响应
     * @throws NetworkException 自定义网络异常
     * @throws Exception
     */
    public static HttpResponse getResponse(String url, HttpParams httpParams, List<NameValuePair> getParams)
            throws NetworkException {
        return PDW_HTTP_CLIENT.getResponse(url, httpParams, getParams);
    }

    /**
     * @param response
     * @return response中的相应内容
     * @throws IOException
     */
    public static String getResponseString(HttpResponse response) throws IOException {
        return PDW_HTTP_CLIENT.getResponseString(response);
    }

    /*********************** 微博使用相关方法 **************************/
    /**
     * 把请求地址中的参数装入到bundle对象中.
     *
     * @param url 请求地址
     * @return a dictionary bundle of keys and values
     */
    public static Bundle parseUrl(String url) {
        // hack to prevent MalformedURLException
        url = url.replace("weiboconnect", "http");
        try {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    /**
     * 对Url进行解码
     *
     * @param s 请求地址
     * @return 解码后的值
     */
    public static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String[] array = s.split("&");
            for (String parameter : array) {
                String[] v = parameter.split("=");
                if (v.length == 2) {
                    params.putString(URLDecoder.decode(v[0]), URLDecoder.decode(v[1]));
                } else if (v.length == 1) {
                    params.putString(URLDecoder.decode(v[0]), "");
                }
            }
        }
        return params;
    }

    /**
     * 对Url进行编码
     *
     * @param parameters 参数
     * @return 编码后的地址
     */
    public static String encodeUrl(com.wrriormedia.library.util.UrlParameters parameters) {
        if (parameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int loc = 0; loc < parameters.size(); loc++) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            String value = (parameters.getValue(loc) == null || "".equals(parameters.getValue(loc))) ? "" : URLEncoder
                    .encode(parameters.getValue(loc));
            sb.append(URLEncoder.encode(parameters.getKey(loc)) + "=" + value);
        }
        return sb.toString();
    }

    /**
     * 设置CookieStore
     *
     * @param domain 域名
     * @param name   Cookie名称
     * @param values 值参数
     */
    public static void setCookieStores(String domain, String name, String values) {
        PDW_HTTP_CLIENT.setCookieStores(domain, name, values);
    }

    /**
     * 获取当前请求的Cookies
     *
     * @return 返回cookie信息
     */
    public static String getCookies() {
        return PDW_HTTP_CLIENT.getCookies();
    }
}
