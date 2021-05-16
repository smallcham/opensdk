package cn.ce.opensdk;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by medusa on 2016/5/30.
 * explain: HttpClient 工具类<br/>
 * <b>为了不影响当前正在使用[xinnet-utils第一版]的工程，基于[xinnet-utils 1.3] 重构 为 [xinnet-utils2] </b><br/>
 * <hr/>
 * <b>该版本主要做以下更改：</b>
 * <li>修改json格式传参拼装数据方法为 {@link com.xinnet.core.utils.json.JsonUtil.Single#toJson(Object)}</li>
 * <li>更丰富日志打印</li>
 */
public class HttpClient {

    private final static Logger log = LoggerFactory.getLogger(HttpClient.class);
    /**
     * 默认参数编码
     */
    private final static String DEFAULT_CHARSET = "UTF-8";

    private static CloseableHttpClient client;

    /**
     * 超时时间 3 分钟（毫秒）
     */
    private final static int SOCKET_TIMEOUT = 30000;

    /**
     * 最大连接数
     */
    private final static int MAX_CONN_TOTAL = 200;

    /**
     * 每个路由可承受的并发请求数量
     */
    private final static int DEFAULT_MAX_PER_ROUTE = 50;

    static {
        try {
            initPool();
        }
        catch (Exception e){
            e.printStackTrace();
            log.error("构建HttpClient对象失败!",e);
        }
    }

    /******** GET *********/

    /**
     * explain: 获取请求response
     * author: 湛智
     * @param url 请求地址
     * @param param 参数
     * @param charset 参数编码
     * @param head 请求头
     * date 2016/5/30 - 14:22
     **/
    private static CloseableHttpResponse getResponse(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        return client.execute(newGet(url, param, charset, head));
    }

    /**
     * explain: GET 请求
     * author: 湛智
     * @param url 请求地址
     * @param head 请求头
     * @param param 请求参数
     * @param charset 参数集
     * date 2016/5/30 - 14:23
     **/
    public static HttpResult get(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        CloseableHttpResponse response = getResponse(url, param, charset, head);
        HttpEntity entity = response.getEntity();
        int code = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(entity, charset);
        EntityUtils.consume(entity);
        close(response);
        log.info(String.format("Request: [ %s ] \nParams: %s \nHeaders: [ %s ] \nResult: [ %s ] Method: [ GET ] StatusCode: [ %s ]", url, null == param ? "" : param.toString(), null == head ? "" : head.toString(), result, code));
        return new HttpResult(code, result);
    }

    /**
     * explain: GET 请求（不需要请求头）
     * author: 湛智
     * @param url 请求地址
     * @param param 请求参数
     * @param charset 参数集
     * date 2016/5/30 - 14:23
     **/
    public static HttpResult get(String url, Map<String, Object> param, String charset) throws IOException {
        return get(url, param, charset, null);
    }

    /**
     * explain: GET 请求 (默认UTF-8)
     * author: 湛智
     * @param url 请求地址
     * @param head 请求头
     * @param param 请求参数
     * date 2016/5/30 - 14:23
     **/
    public static HttpResult get(String url, Map<String, Object> param, Map<String, Object> head) throws IOException {
        return get(url, param, DEFAULT_CHARSET, head);
    }

    /**
     * explain: GET 请求（默认UTF-8， 不需要请求头）
     * author: 湛智
     * @param url 请求地址
     * @param param 请求参数
     * date 2016/5/30 - 14:23
     **/
    public static HttpResult get(String url, Map<String, Object> param) throws IOException {
        return get(url, param, DEFAULT_CHARSET, null);
    }

    /**
     * explain: GET 请求
     * author: 湛智
     * @param url 请求地址
     * date 2016/5/30 - 14:23
     **/
    public static HttpResult get(String url) throws IOException {
        return get(url, null, DEFAULT_CHARSET, null);
    }

    /**
     * explain: 创建一个HttpGet
     * author: 湛智
     * @param url 请求URL
     * @param charset 编码
     * @param head 请求头
     * @throws IllegalArgumentException 当 url 参数为空时抛出
     * date 2016/5/30 - 14:26
     **/
    private static HttpGet newGet(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        if (StringUtils.isEmpty(url)) throw new IllegalArgumentException("请求地址不能为空");
        UrlEncodedFormEntity encodedFormEntity = paramFormat(param, charset);
        if (encodedFormEntity != null) url += "?" + EntityUtils.toString(encodedFormEntity);
        HttpGet httpGet = new HttpGet(url);
        addHead(httpGet, head);
        return httpGet;
    }

    /******** POST *********/
    private static CloseableHttpResponse postResponse(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        return client.execute(newPost(url, param, charset, head));
    }

    public static HttpResult post(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        CloseableHttpResponse response = postResponse(url, param, charset, head);
        HttpEntity entity = response.getEntity();
        int code = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(entity, charset);
        EntityUtils.consume(entity);
        close(response);
        log.info(String.format("Request: [ %s ] \nParams: %s \nHeaders: [ %s ] \nResult: [ %s ] Method: [ POST ] StatusCode: [ %s ]", url, null == param ? "" : param.toString(), null == head ? "" : head.toString(), result, code));
        return new HttpResult(code, result);
    }

    public static HttpResult post(String url, Map<String, Object> param, String charset) throws IOException {
        return post(url, param, charset, null);
    }

    public static HttpResult post(String url, Map<String, Object> param, Map<String, Object> head) throws IOException {
        return post(url, param, DEFAULT_CHARSET, head);
    }

    public static HttpResult post(String url, Map<String, Object> param) throws IOException {
        return post(url, param, DEFAULT_CHARSET, null);
    }

    public static HttpResult post(String url) throws IOException {
        return post(url, null, DEFAULT_CHARSET, null);
    }

    private static HttpPost newPost(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        if (StringUtils.isEmpty(url)) throw new IllegalArgumentException("请求地址不能为空");
        HttpPost httpPost = new HttpPost(url);
        if (isJson(head)) httpPost.setEntity(jsonParamFormat(param, charset));
        else httpPost.setEntity(paramFormat(param, charset));
        addHead(httpPost, head);
        return httpPost;
    }

    /******** PUT *********/
    private static CloseableHttpResponse putResponse(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        return client.execute(newPut(url, param, charset, head));
    }

    public static HttpResult put(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        CloseableHttpResponse response = putResponse(url, param, charset, head);
        HttpEntity entity = response.getEntity();
        int code = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(entity, charset);
        EntityUtils.consume(entity);
        close(response);
        log.info(String.format("Request: [ %s ] \nParams: %s \nHeaders: [ %s ] \nResult: [ %s ] Method: [ PUT ] StatusCode: [ %s ]", url, null == param ? "" : param.toString(), null == head ? "" : head.toString(), result, code));
        return new HttpResult(code, result);
    }

    public static HttpResult put(String url, Map<String, Object> param, String charset) throws IOException {
        return put(url, param, charset, null);
    }

    public static HttpResult put(String url, Map<String, Object> param, Map<String, Object> head) throws IOException {
        return put(url, param, DEFAULT_CHARSET, head);
    }

    public static HttpResult put(String url, Map<String, Object> param) throws IOException {
        return put(url, param, DEFAULT_CHARSET, null);
    }

    public static HttpResult put(String url) throws IOException {
        return put(url, null, DEFAULT_CHARSET, null);
    }

    private static HttpPut newPut(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        if (StringUtils.isEmpty(url)) throw new IllegalArgumentException("请求地址不能为空");
        HttpPut httpPut = new HttpPut(url);
        if (isJson(head)) httpPut.setEntity(jsonParamFormat(param, charset));
        else httpPut.setEntity(paramFormat(param, charset));
        addHead(httpPut, head);
        return httpPut;
    }

    /******** DELETE *********/
    private static CloseableHttpResponse deleteResponse(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        return client.execute(newDelete(url, param, charset, head));
    }

    public static HttpResult delete(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        CloseableHttpResponse response = deleteResponse(url, param, charset, head);
        HttpEntity entity = response.getEntity();
        int code = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(entity, charset);
        EntityUtils.consume(entity);
        close(response);
        log.info(String.format("Request: [ %s ] \nParams: %s \nHeaders: [ %s ] \nResult: [ %s ] Method: [ DELETE ] StatusCode: [ %s ]", url, null == param ? "" : param.toString(), null == head ? "" : head.toString(), result, code));
        return new HttpResult(code, result);
    }

    public static HttpResult delete(String url, Map<String, Object> param, String charset) throws IOException {
        return delete(url, param, charset, null);
    }

    public static HttpResult delete(String url, Map<String, Object> param, Map<String, Object> head) throws IOException {
        return delete(url, param, DEFAULT_CHARSET, head);
    }

    public static HttpResult delete(String url, Map<String, Object> param) throws IOException {
        return delete(url, param, DEFAULT_CHARSET, null);
    }

    public static HttpResult delete(String url) throws IOException {
        return delete(url, null, DEFAULT_CHARSET, null);
    }

    private static HttpDelete newDelete(String url, Map<String, Object> param, String charset, Map<String, Object> head) throws IOException {
        if (StringUtils.isEmpty(url)) throw new IllegalArgumentException("请求地址不能为空");
        url += "?" + EntityUtils.toString(paramFormat(param, charset));
        HttpDelete httpDelete = new HttpDelete(url);
        addHead(httpDelete, head);
        return httpDelete;
    }

    /**
     * explain: 添加请求头
     * author: 湛智
     * @param requestBase HttpGet、HttpPost、HttpPut、HttpDelete 父类
     * @param head 请求头Map
     * date 2016/5/30 - 14:27
     **/
    private static void addHead(HttpRequestBase requestBase, Map<String, Object> head) {
        if (null == head) return;
        Set<String> keySet = head.keySet();
        for (String key : keySet) {
            requestBase.setHeader(key, String.valueOf(head.get(key)));
        }
    }

    /**
     * explain: 关闭response流
     * author: 湛智
     * @param response response
     * date 2016/5/30 - 14:28
     **/
    private static void close(CloseableHttpResponse response) {
        try {
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * explain: 格式化参数
     * author: 湛智
     * @param param 请求参数
     * @param charset 参数编码
     * date 2016/5/30 - 14:29
     **/
    private static UrlEncodedFormEntity paramFormat(Map<String, Object> param, String charset) throws IOException {
        if (null == param) return null;
        List<BasicNameValuePair> paramList = new ArrayList<>();
        Set<String> keySet = param.keySet();
        Object value;
        for (String key : keySet) {
            value = param.get(key);
            if (null == key || null == value) {
                continue;
            }
            paramList.add(new BasicNameValuePair(key, value.toString()));
        }
        return new UrlEncodedFormEntity(paramList, charset);
    }

    /**
     * explain: 格式化json参数
     * author: 湛智
     * @param param 请求参数
     * date 2016/10/18 - 10:35
     **/
    private static StringEntity jsonParamFormat(Map<String, Object> param, String charset) throws IOException {
        if (null == param) {
            StringEntity entity = new StringEntity("{}", charset);
            entity.setContentType("application/json");
            return entity;
        }
        StringEntity entity = new StringEntity(JsonUtil.Single.toJson(param), charset);
        entity.setContentType("application/json");
        return entity;
    }

    private static boolean isJson(Map<String, Object> head) {
        if (null == head) return false;
        String type = (String) head.get("Content-Type");
        return "application/json".equals(type);
    }

    /**
     * explain: 创建连接池
     * author: 湛智
     * date 2016/5/30 - 14:48
     **/
    private static void initPool() throws Exception{
        //支持http
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);

        //支持https
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        TrustStrategy anyTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        };
        SSLContext sslContext = SSLContexts.custom().useProtocol("TLS").loadTrustMaterial(trustStore, anyTrustStrategy).build();
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        registryBuilder.register("https", sslSF);

        Registry<ConnectionSocketFactory> registry = registryBuilder.build();

        // 创建连接配置
        ConnectionConfig connConfig = ConnectionConfig.custom().setCharset(Consts.UTF_8).build();

        // 创建Socket配置对象
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT).build();

        // 创建客户端连接管理器
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
        connManager.setMaxTotal(MAX_CONN_TOTAL);
        connManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);

        // 设置默认配置对象
        connManager.setDefaultConnectionConfig(connConfig);
        connManager.setDefaultSocketConfig(socketConfig);

        // 初始化请求客户端对象
        client = HttpClientBuilder.create().setConnectionManager(connManager).build();
    }

    public static void main(String[] args) throws Exception {
//        String accessKeySecret = "vjjhmlpjxrhzvprevktkvdop";
//        Map<String, Object> head = new HashMap<>();
//        Map<String, Object> param = new HashMap<>();
//        head.put("Auth-Token","e62036f6786943e38296de9f87160eb3bf7b8f54517b4bbd85be3c2a35288676");
//        head.put("Content-Type", "application/json");
//        param.put("accessKey","hrtvajwufrgwynkaxuhivkte");
//        param.put("timestamp", new Date().getTime());
//
//        param.put("commonName","test.xinnet.com");
//        param.put("organization","新网数码");
//        param.put("organizationUnit","云平台");
//        param.put("locality","110000");
//        param.put("state","110000");
//        param.put("country","CN");
//        param.put("keypairAlgorithm","RSA");
//        param.put("keypairParameter","2048");
//
//        String uri = URIEncode.kvEncode(param);
//        String sign = HMAC.sha256(accessKeySecret, "POST" + "&" + "/openApi/v1/genCsr" + "&" + uri);
//        param.put("sign", sign);
//        System.out.println(HttpClient.post("https://mpki-test.xinnet.com/openApi/v1/genCsr", param, head));
    }
}