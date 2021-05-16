package cn.ce.opensdk;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by wzz on 2016/5/30.
 *
 */
public class HttpResult {

    private int code;
    private String result;

    public HttpResult() {}

    public HttpResult(String result) {
        this.result = result;
    }

    public HttpResult(int code, String result) {
        this.code = code;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public String asString() {
        return result;
    }

    public int asInt() {
        return Integer.parseInt(result);
    }

    public boolean asBoolean() {
        return Boolean.parseBoolean(result);
    }

    public <T> T asBean(Class<T> clazz) {
        return JsonUtil.Single.toBean(result, clazz);
    }

    public JsonNode asNode() {
        return JsonUtil.Single.toNode(result);
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "code=" + code +
                ", result='" + result + '\'' +
                '}';
    }
}
