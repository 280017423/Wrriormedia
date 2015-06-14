package com.wrriormedia.library.http;

import com.wrriormedia.library.app.JsonResult;
import com.wrriormedia.library.util.MessageException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 网络访问辅助类
 *
 * @author wang.xy
 */

/**
 * @author alina
 * @version 2012-08-02，zou.sq，增加httpGet,getString,parseUrl,decodeUrl,encodeUrl等方法
 *          <br>
 *          2012-10-31，tan.xx，get， post新增带参数requestType处理等方法<br>
 */
public interface IPDWHttpClient {

    int REQUEST_GET = 1;
    int REQUEST_POST = 2;
    int REQUEST_PUT = 3;
    int REQUEST_DELETE = 4;

    /**
     * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url       url地址
     * @param getParams 附加在Url后面的参数
     * @return json数据
     * @throws NetworkException
     * @throws MessageException 异常信息
     */
    JsonResult get(String url, List<NameValuePair> getParams) throws com.wrriormedia.library.http.NetworkException, com.wrriormedia.library.util.MessageException;

    /**
     * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param getParams  附加在Url后面的参数
     * @return json数据
     * @throws MessageException
     * @throws NetworkException
     */
    JsonResult get(String url, HttpParams httpParams, List<NameValuePair> getParams) throws com.wrriormedia.library.http.NetworkException,
            com.wrriormedia.library.util.MessageException;

    /**
     * @param url        url地址
     * @param httpParams 参数
     * @param getParams  参数
     * @return http响应
     * @throws NetworkException
     */
    HttpResponse getResponse(String url, HttpParams httpParams, List<NameValuePair> getParams) throws com.wrriormedia.library.http.NetworkException;

    /**
     * @param response HttpResponse响应对象
     * @return response中的相应内容
     */
    String getResponseString(HttpResponse response);

    /**
     * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @return json数据
     * @throws MessageException 消息异常信息
     * @throws NetworkException 网络异常信息
     */
    JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams) throws com.wrriormedia.library.http.NetworkException,
            com.wrriormedia.library.util.MessageException;

    /**
     * 通过put方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @return json数据
     * @throws MessageException 消息异常信息
     * @throws NetworkException 网络异常信息
     */
    JsonResult put(String url, HttpParams httpParams, List<NameValuePair> postParams) throws com.wrriormedia.library.http.NetworkException,
            com.wrriormedia.library.util.MessageException;

    /**
     * 通过delete方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @return json数据
     * @throws MessageException 消息异常信息
     * @throws NetworkException 网络异常信息
     */
    JsonResult delete(String url, HttpParams httpParams, List<NameValuePair> postParams) throws com.wrriormedia.library.http.NetworkException,
            com.wrriormedia.library.util.MessageException;

    /**
     * 获取当前请求的Cookies
     *
     * @return 返回
     */
    String getCookies();

    /**
     * @param cookieStore 存储coockie
     */
    void setCookieStore(CookieStore cookieStore);

    /**
     * 设置CookieStore
     *
     * @param domain 域名
     * @param name   Cookie名称
     * @param values 值 参数
     */
    void setCookieStores(String domain, String name, String values);

    /**
     * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @param fileList   文件列表
     * @return json数据
     * @throws MessageException             消息异常信息
     * @throws NetworkException             网络异常信息
     * @throws UnsupportedEncodingException
     */
    JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams, List<File> fileList)
            throws com.wrriormedia.library.http.NetworkException, MessageException, UnsupportedEncodingException;

}
