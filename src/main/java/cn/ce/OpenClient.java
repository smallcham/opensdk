package cn.ce;

import cn.ce.opensdk.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OpenClient
{
    private final String appKey;
    private final String appSec;
    private String accessToken;

    public OpenClient(String appKey, String appSec, String accessToken) {
        this.appKey = appKey;
        this.appSec = appSec;
        this.accessToken = accessToken;
    }

    public HttpResult post(String url) throws Exception {
        return post(url, null);
    }

    public HttpResult post(String url, Map<String, Object> param) throws Exception {
        return post(url, param, false);
    }

    public HttpResult post(String url, Map<String, Object> param, boolean isJson) throws Exception {
        return post(url, param, null, isJson);
    }

    public HttpResult post(String url, Map<String, Object> param, Map<String, Object> head, boolean isJson) throws Exception {
        return request(url, param, head, "POST", isJson);
    }

    public HttpResult get(String url) throws Exception {
        return get(url, null);
    }

    public HttpResult get(String url, Map<String, Object> param) throws Exception {
        return get(url, param, false);
    }

    public HttpResult get(String url, Map<String, Object> param, boolean isJson) throws Exception {
        return get(url, param, null, isJson);
    }

    public HttpResult get(String url, Map<String, Object> param, Map<String, Object> head, boolean isJson) throws Exception {
        return request(url, param, head, "GET", isJson);
    }

    private HttpResult request(String url, Map<String, Object> param, Map<String, Object> head, String method, boolean isJson) throws Exception {
        method = method.toUpperCase();
        if (null == param) {
            param = new HashMap<>();
        }
        if (null == head) {
            head = new HashMap<>();
        }
        Map<String, Object> mergeParam = new HashMap<>();
        long time = new Date().getTime();
        mergeParam.put("timestamp", time);
        mergeParam.put("appKey", appKey);
        mergeParam.put("accessToken", accessToken);

        String uri;
        if (isJson) {
            String bodySign = MD5Utils.encode(JsonUtil.Single.toJson(param));
            mergeParam.put("bodySign", bodySign);
            head.put("Content-Type", "application/json");
        } else {
            mergeParam.putAll(param);
        }
        uri = URIEncode.kvEncode(mergeParam);
        head.put("sign", HMAC.sha256(appSec, uri));
        HttpResult result;
        if (method.equals("POST")) {
            result = HttpClient.post(url + "?" + uri, isJson ? param : null, head);
        } else if (method.equals("GET")) {
            result = HttpClient.get(url + "?" + uri, isJson ? param : null, head);
        } else {
            throw new Exception("not support type: " + method);
        }
        return result;
    }

    public static void main( String[] args ) throws Exception {
        //公钥用于发送给服务端识别调用者身份
        String key = "ef0465ac-7ae8-4e64-90b2-817382d55f7b";
        //私钥用于加密，不作为参数传递
        String security = "8b01d0aa-d469-40ce-ac54-825d27eb6df9";
        //accessToken，确认调用权限
        String accessToken = "cddd0c00-a56f-11eb-ab99-eb72f8ad3a11";

        OpenClient client = new OpenClient(key, security, accessToken);
        Map<String, Object> param = new HashMap<>();
        param.put("code", "zcapaoiwadoapajdwaa");
        param.put("code1", 12312311);
        param.put("code2", "92391alwjl");
        HttpResult res = client.post("http://localhost:8080/test/local.0.0.0", param);
        System.out.println(res.asString());
    }
}
