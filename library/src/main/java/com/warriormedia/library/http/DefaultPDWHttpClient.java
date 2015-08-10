package com.warriormedia.library.http;

import com.warriormedia.library.R;
import com.warriormedia.library.app.HtcApplicationBase;
import com.warriormedia.library.app.JsonResult;
import com.warriormedia.library.util.EvtLog;
import com.warriormedia.library.util.MessageException;
import com.warriormedia.library.util.NetUtil;
import com.warriormedia.library.util.PackageUtil;
import com.warriormedia.library.util.StringUtil;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author zou.sq
 * @version 2012-08-02，zou.sq
 */
public class DefaultPDWHttpClient implements IPDWHttpClient {

    private static final int PRINT_MAX_LENGTH = 1024;
    private static final String TAG = "DefaultPDWHttpClient";
    private static final int TIMEOUT_SHORT_IN_MS = 5000;
    private static final int CONNECT_TIMEOUT_MIDDLE_IN_MS = 10000;
    private static final int READ_TIMEOUT_MIDDLE_IN_MS = 20000;
    private static final int UPLOAD_FILE_TIMEOUT_MIDDLE_IN_MS = 30000;
    private static final String CHARSET = "UTF-8";
    private static final String INTERROGATION = "?";
    private static final String AJAX_APPEND_HEADER = "ajax";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private static final String SPLIT_FLAG_AND = "&";
    private static final String SPLIT_FLAG_EQUAL = "=";
    /**
     * 登录成功后的Cookie信息
     */
    private static CookieStore COOKIE_STORE;

    /**
     * buildUrl 拼接get地址 参数
     *
     * @param url       地址
     * @param getParams 参数
     * @return String 拼接后的参数
     */
    public static String buildUrl(String url, List<NameValuePair> getParams) {
        if (!StringUtil.isNullOrEmpty(url) && null != getParams && !getParams.isEmpty()) {
            String returnUrl = url;
            if (!url.contains(INTERROGATION)) {
                returnUrl = url + INTERROGATION;
            }
            returnUrl = returnUrl + buildContent(getParams);
            return returnUrl;
        }
        return url;
    }

    /**
     * buildContent 拼接post 参数内容
     *
     * @param getParams 参数
     * @return String 拼接后的参数
     */
    public static String buildContent(List<NameValuePair> getParams) {
        if (null == getParams) {
            return "";
        }
        String content;
        String tempParams = "";
        for (int i = 0; i < getParams.size(); i++) {
            NameValuePair nameValuePair = getParams.get(i);
            if (nameValuePair != null) {
                String key = StringUtil.isNullOrEmpty(nameValuePair.getName()) ? "" : nameValuePair.getName();
                String value = StringUtil.isNullOrEmpty(nameValuePair.getValue()) ? "" : nameValuePair.getValue();
                try {
                    tempParams = tempParams + SPLIT_FLAG_AND + key + SPLIT_FLAG_EQUAL
                            + URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        if (tempParams.length() > 1) {
            content = tempParams.substring(1);
        } else {
            content = tempParams;
        }
        return content;
    }

    /**
     * 打印服务器返回的信息，异步执行优化网络相应速度2013-01-28
     *
     * @param s 服务器返回的信息
     */
    private static void printResponse(final String s) {
        if (s == null || s.length() == 0 && !EvtLog.IS_DEBUG_LOGGABLE) {
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                EvtLog.d(TAG, "server response:\n");
                int idxBegin = 0;
                int idxEnd = 0;
                int iStep = PRINT_MAX_LENGTH;
                int length = s.length();
                while (idxBegin < length) {
                    if (idxEnd + iStep > length) {
                        idxEnd = length;
                    } else {
                        idxEnd += iStep;
                    }
                    EvtLog.d(TAG, ">>" + s.substring(idxBegin, idxEnd));

                    idxBegin = idxEnd;
                }
            }
        }).start();
    }

    /**
     * 输出当前Cookie
     *
     * @param cookieStore 参数
     */
    private static void printCookies(CookieStore cookieStore) {
        if (EvtLog.IS_DEBUG_LOGGABLE && cookieStore != null) {
            for (Cookie cookie : cookieStore.getCookies()) {
                EvtLog.d(TAG,
                        "上传的cookie信息：domain: " + cookie.getDomain() + "\r\n name: " + cookie.getName() + "\r\n value: " + cookie.getValue());
            }
        }
    }

    /**
     * @param cookieStore 存储coockie
     */
    @Override
    public void setCookieStore(CookieStore cookieStore) {
        COOKIE_STORE = cookieStore;
    }

    /**
     * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @return json数据
     * @throws NetworkException 网络异常
     * @throws MessageException 业务异常
     */
    @Override
    public JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams) throws NetworkException,
            MessageException {
        return getJsonResultConnect(url, postParams, REQUEST_POST);
    }

    @Override
    public JsonResult put(String url, HttpParams httpParams, List<NameValuePair> postParams) throws NetworkException,
            com.warriormedia.library.util.MessageException {
        return getJsonResultConnect(url, postParams, REQUEST_PUT);
    }

    @Override
    public JsonResult delete(String url, HttpParams httpParams, List<NameValuePair> postParams) throws NetworkException,
            com.warriormedia.library.util.MessageException {
        return getJsonResultConnect(url, postParams, REQUEST_DELETE);
    }

    /**
     * 通过post方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param postParams 参数
     * @param fileList   文件列表
     * @return json数据
     * @throws NetworkException             网络异常
     * @throws MessageException             业务异常
     * @throws UnsupportedEncodingException 编码不支持异常
     */
    @Override
    public JsonResult post(String url, HttpParams httpParams, List<NameValuePair> postParams, List<File> fileList)
            throws NetworkException, MessageException, UnsupportedEncodingException {
        EvtLog.d(TAG, "post begin, " + url);
        if (!com.warriormedia.library.util.NetUtil.isNetworkAvailable()) {
            HttpClientUtil.LAST_REQUEST_IS_OK = false;
            throw new NetworkException(com.warriormedia.library.util.PackageUtil.getString(R.string.network_is_not_available));
        }
        JsonResult jsonResult = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(AJAX_APPEND_HEADER, "true");
        httpPost.setHeader("Accept-Encoding", "gzip");
        if (httpParams != null) {
            httpPost.setParams(httpParams);
        }

        MultipartEntity entity = new MultipartEntity();
        if (postParams != null && !postParams.isEmpty()) {
            if (EvtLog.IS_DEBUG_LOGGABLE) {
                printPostData(postParams);
            }
            for (NameValuePair entry : postParams) {
                // 修改编码为utf-8编码，解决服务器乱码的问题
                entity.addPart(entry.getName(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
                EvtLog.d(TAG, "Name:" + entry.getName() + " Value:" + entry.getValue());
            }
        }
        for (int i = 0; i < fileList.size(); i++) {
            File tempFile = fileList.get(i);
            entity.addPart("file", new FileBody(tempFile));
        }
        httpPost.setEntity(entity);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_SHORT_IN_MS);
        HttpConnectionParams.setSoTimeout(httpParameters, UPLOAD_FILE_TIMEOUT_MIDDLE_IN_MS);
        DefaultHttpClient client = new DefaultHttpClient(httpParameters);
        setHttpClientInterceptor(client);
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        if (COOKIE_STORE != null) {
            client.setCookieStore(COOKIE_STORE);
            printCookies(COOKIE_STORE);
        }
        EvtLog.d(TAG, "client.execute begin ");
        try {
            HttpResponse httpResponse = client.execute(httpPost);

            int status = httpResponse.getStatusLine().getStatusCode();
            EvtLog.d(TAG, "client.execute end, status: " + status);
            String returnString = EntityUtils.toString(httpResponse.getEntity());
            if (status == HttpStatus.SC_OK) {
                try {
                    jsonResult = new JsonResult(returnString);
                    if (EvtLog.IS_DEBUG_LOGGABLE) {
                        printResponse(returnString);
                    }
                } catch (JSONException e) {
                    EvtLog.w(TAG, e);
                }
                if (client.getCookieStore().getCookies() != null && client.getCookieStore().getCookies().size() > 0) {
                    COOKIE_STORE = client.getCookieStore();
                }
            } else {
                HtcApplicationBase.getInstance().savaLog(url, postParams, returnString);
                EvtLog.e(TAG, "server response: " + returnString + ";  status:" + status);
            }
        } catch (Exception e) {
            HtcApplicationBase.getInstance().savaLog(url, postParams, e.toString());
            if (e instanceof IOException) {
                EvtLog.e(TAG, "NetworkException");
                HttpClientUtil.LAST_REQUEST_IS_OK = false;
                throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
            } else {
                EvtLog.w(TAG, e);
            }
        } finally {
            EvtLog.d(TAG, "post end, " + url);
        }
        if (jsonResult != null) {
            HttpClientUtil.LAST_REQUEST_IS_OK = true;
        }
        return jsonResult;
    }

    /**
     * 设置请求和响应的intercepter以通知服务器
     *
     * @param client 客户端请求的DefaultHttpClient
     */
    private void setHttpClientInterceptor(DefaultHttpClient client) {
        client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) {
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
            }
        });
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext context) {
                final HttpEntity entity = response.getEntity();
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url       url地址
     * @param getParams 附加在Url后面的参数
     * @return json数据
     * @throws MessageException 异常信息
     * @throws NetworkException 异常信息
     */
    @Override
    public JsonResult get(String url, List<NameValuePair> getParams) throws NetworkException, MessageException {
        return get(url, null, getParams);
    }

    /**
     * 通过get方式，跟服务器进行数据交互。该方法已经进行了网络检查
     *
     * @param url        url地址
     * @param httpParams http参数
     * @param getParams  附加在Url后面的参数
     * @return json数据
     * @throws NetworkException 异常信息
     * @throws MessageException 异常信息
     */
    @Override
    public JsonResult get(String url, HttpParams httpParams, List<NameValuePair> getParams) throws NetworkException,
            MessageException {
        return getJsonResultConnect(url, getParams, REQUEST_GET);
    }

    /**
     * getJosnResultConnect 获取连接
     *
     * @param url         地址
     * @param params      参数
     * @param requestType 请求方式
     * @return JsonResult json数据
     * @throws NetworkException 网络异常
     */
    private JsonResult getJsonResultConnect(final String url, final List<NameValuePair> params,
                                            final int requestType) throws NetworkException {
        if (!NetUtil.isNetworkAvailable()) {
            HttpClientUtil.LAST_REQUEST_IS_OK = false;
            throw new NetworkException(PackageUtil.getString(R.string.network_is_not_available));
        }
        return getJsonResultByNetwork(url, params, requestType);
    }

    /**
     * 从网络获取数据
     *
     * @param url         请求地址
     * @param params      参数
     * @param requestType 是不是get请求
     * @return JsonResult 返回的json对象
     * @throws NetworkException 网咯异常
     */
    public JsonResult getJsonResultByNetwork(String url, List<NameValuePair> params, int requestType) throws NetworkException {
        String buildUrl = url;
        JsonResult jsonResult = null;
        if (REQUEST_GET == requestType || REQUEST_DELETE == requestType) {
            buildUrl = buildUrl(url, params);
        }
        InputStream is = null;
        HttpURLConnection httpURLConnection = null;
        String cookieVal;
        try {
            URL connectUrl = new URL(buildUrl);
            httpURLConnection = (HttpURLConnection) connectUrl.openConnection();
            httpURLConnection.setReadTimeout(READ_TIMEOUT_MIDDLE_IN_MS);
            httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT_MIDDLE_IN_MS);
            if (REQUEST_GET == requestType) {
                httpURLConnection.setRequestMethod("GET");
            } else if (REQUEST_POST == requestType) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
            } else if (REQUEST_PUT == requestType) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("PUT");
            } else if (REQUEST_DELETE == requestType) {
                httpURLConnection.setRequestMethod("DELETE");
            }
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty("Charset", CHARSET); // 设置编码
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            addHeader(httpURLConnection, params); //添加header信息
            if (COOKIE_STORE != null) { //添加Cookie信息
                httpURLConnection.addRequestProperty("Cookie", cookiesToString(COOKIE_STORE));
                printCookies(COOKIE_STORE);
            }
            EvtLog.d(TAG, "开始请求url:" + buildUrl);
            if (EvtLog.IS_DEBUG_LOGGABLE && (REQUEST_PUT == requestType || REQUEST_POST == requestType)) {
                printPostData(params);
            }
            httpURLConnection.connect();
            if (REQUEST_PUT == requestType || REQUEST_POST == requestType) {
                DataOutputStream os = new DataOutputStream(httpURLConnection.getOutputStream());
                String content = buildContent(params);
                EvtLog.d(TAG, "参数:" + content);
                byte[] sendData = content.getBytes();
                if (sendData.length > 0) {
                    int len = PRINT_MAX_LENGTH;
                    int offset = 0;
                    while (sendData.length - offset > len) {
                        os.write(sendData, offset, len);
                        offset += len;
                    }
                    os.write(sendData, offset, sendData.length - offset);
                    os.flush();
                    os.close();
                }
            }
            int respCode = httpURLConnection.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK) {
                is = httpURLConnection.getInputStream();
                if (null != is) {
                    StringBuilder result = new StringBuilder(1024);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    if (EvtLog.IS_DEBUG_LOGGABLE) {
                        printResponse(result.toString());
                    }
                    cookieVal = httpURLConnection.getHeaderField("set-cookie");
                    EvtLog.d(TAG, "cookieVal:" + cookieVal);
                    saveCookie(url, cookieVal);
                    String returnStr = result.toString();
                    jsonResult = new JsonResult(returnStr);
                } else {
                    EvtLog.w(TAG, "返回为null");
                }
            } else {
                HtcApplicationBase.getInstance().savaLog(url, params, httpURLConnection.getResponseMessage());
                EvtLog.d(TAG, "server response code  " + respCode + "; msg :" + httpURLConnection.getResponseMessage());
            }
        } catch (IOException ioe) {
            HtcApplicationBase.getInstance().savaLog(url, params, ioe.toString());
            EvtLog.w(TAG, ioe);
            HttpClientUtil.LAST_REQUEST_IS_OK = false;
            throw new NetworkException(
                    PackageUtil.getString(R.string.network_is_not_available));
        } catch (JSONException e) {
            HtcApplicationBase.getInstance().savaLog(url, params, e.toString());
            EvtLog.w(TAG, e);
            HttpClientUtil.LAST_REQUEST_IS_OK = false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (IOException e) {
                EvtLog.w(TAG, e);
            }
        }
        if (jsonResult != null) {
            HttpClientUtil.LAST_REQUEST_IS_OK = true;
        }
        return jsonResult;
    }

    /**
     * 添加header 设备标识
     *
     * @param hrc HttpURLConnection对象
     */
    private void addHeader(HttpURLConnection hrc, List<NameValuePair> nameValuePairs) {
        if (hrc != null) {
            hrc.addRequestProperty("referer", "http://warriormedia.cn/");
            hrc.addRequestProperty("version", PackageUtil.getVersionName());
        }
    }

    /**
     * saveCookie 保存Cookie
     *
     * @param url       请求的url地址
     * @param cookieVal cookie字符串
     */
    private void saveCookie(String url, String cookieVal) {
        if (!StringUtil.isNullOrEmpty(cookieVal)) {
            String name = "";
            String domain = "";
            String values = "";

            String[] cookieArr = cookieVal.split(";");
            if (cookieArr.length > 1) {
                String[] cookieNameValue = cookieArr[0].split(SPLIT_FLAG_EQUAL);
                if (cookieNameValue.length > 1) {
                    name = cookieNameValue[0];
                    values = cookieNameValue[1];
                    if (StringUtil.isNullOrEmpty(values)) {
                        return;
                    }
                }
                String[] domainArr = cookieArr[1].split(SPLIT_FLAG_EQUAL);
                if (domainArr.length > 1) {
                    domain = domainArr[1];
                    try {
                        domain = new URL(url).getHost();
                    } catch (MalformedURLException e) {
                        EvtLog.e(TAG, e);
                    }
                }
            }
            setCookieStores(domain, name, values);
        }
    }

    private void printPostData(List<NameValuePair> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        EvtLog.d(TAG, "请求参数信息: " + params.size());
        for (int i = 0; i < params.size(); ++i) {
            EvtLog.d(TAG, params.get(i).getName() + ":" + params.get(i).getValue());
        }
    }

    /**
     * @param url        请求url
     * @param httpParams http参数
     * @param getParams  请求参数
     * @return http响应 相应数据
     * @throws NetworkException 网络异常
     */
    @Override
    public HttpResponse getResponse(String url, HttpParams httpParams, List<NameValuePair> getParams)
            throws NetworkException {
        if (!com.warriormedia.library.util.NetUtil.isNetworkAvailable()) {
            HttpClientUtil.LAST_REQUEST_IS_OK = false;
            throw new NetworkException(com.warriormedia.library.util.PackageUtil.getString(R.string.network_is_not_available));
        }
        String buildUrl = buildUrl(url, getParams);
        HttpGet httpGet = new HttpGet(buildUrl);
        if (httpParams != null) {
            httpGet.setParams(httpParams);
        }
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_SHORT_IN_MS);
        HttpConnectionParams.setSoTimeout(httpParameters, CONNECT_TIMEOUT_MIDDLE_IN_MS);

        DefaultHttpClient client = new DefaultHttpClient(httpParameters);
        setHttpClientInterceptor(client);

        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        if (COOKIE_STORE != null) {
            client.setCookieStore(COOKIE_STORE);
        }
        EvtLog.d(TAG, "client.execute begin, url: " + buildUrl);
        HttpResponse httpResponse = null;
        try {
            httpResponse = client.execute(httpGet);
        } catch (ClientProtocolException e) {
            EvtLog.e(TAG, e);
        } catch (IOException e) {
            EvtLog.e(TAG, e);
        }
        return httpResponse;
    }

    /**
     * @param response HttpResponse
     * @return response中的相应内容
     */
    @Override
    public String getResponseString(HttpResponse response) {
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try {
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            String nl = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + nl);
            }
            in.close();
        } catch (Exception e) {
            EvtLog.e(TAG, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    EvtLog.w(TAG, e);
                }
            }
        }
        return sb.toString();
    }

    /**
     * cookiesToString
     *
     * @param cookieStore cookie信息
     * @return String 例如ASP.NET_SessionId=5cu1fqdvvifq1tr3qk005zdy;
     * domain=api.paidui.cn;
     */
    private String cookiesToString(CookieStore cookieStore) {
        String cookieInfo = "";
        for (Cookie cookie : cookieStore.getCookies()) {
            EvtLog.d(TAG, " cookiesToString  :" + "domain: " + cookie.getDomain() + " name: " + cookie.getName()
                    + " value: " + cookie.getValue());
            cookieInfo = cookie.getName() + SPLIT_FLAG_EQUAL + cookie.getValue() + ";" + "doman=" + cookie.getDomain();
        }
        return cookieInfo;
    }

    /**
     * 设置CookieStore
     *
     * @param domain 域名
     * @param name   Cookie名称
     * @param values 值 参数
     */
    @Override
    public void setCookieStores(String domain, String name, String values) {
        BasicClientCookie2 cookie = new BasicClientCookie2(name, values);
        cookie.setDomain(domain);
        cookie.setValue(values);
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(cookie);
        setCookieStore(cookieStore);
    }

    /**
     * 获取当前请求的Cookies
     *
     * @return 返回Cookies
     */
    @Override
    public String getCookies() {
        StringBuilder cookies = new StringBuilder();
        if (COOKIE_STORE != null) {
            for (Cookie cookie : COOKIE_STORE.getCookies()) {
                String cookieString = cookie.getName() + SPLIT_FLAG_EQUAL + cookie.getValue() + "; domain="
                        + cookie.getDomain() + ";";
                cookies.append(cookieString);
            }
        }
        return cookies.toString();
    }

    /**
     * A private class which handles the content inflation.
     */
    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            EvtLog.d(TAG, "Use gzip stream to unzip the content");
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

}
