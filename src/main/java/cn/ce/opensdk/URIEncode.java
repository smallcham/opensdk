package cn.ce.opensdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by medusa on 2016/5/31.
 *
 */
public class URIEncode {

    private final static String DEFAULT_CHARSET = "UTF-8";

    /**
     * explain: RFC 3986规定，"URI非保留字符"包括以下字符：字母（A-Z，a-z）、数字（0-9）、连字号（-）、点号（.）、下划线（_)、波浪线（~），算法实现如下：
     1. 将字符串转换成UTF-8编码的字节流
     2. 保留所有“URI非保留字符”原样不变
     3. 对其余字节做一次RFC 3986中规定的百分号编码（Percent-encoding），即一个“%”后面跟着两个表示该字节值的十六进制字母，字母一律采用大写形式。
     4. 可以选择斜杠（/）是否进行编码。把结果中所有的`%2F`都替换为`/`
     * author: 湛智
     * @param input 需要转换的串
     * @param encodeSlash “/”是否参与编码
     * date 2016/5/31 - 10:37
     **/
    public static String encode(CharSequence input, boolean encodeSlash) {
        try {
            String result = URLEncoder.encode(String.valueOf(input), DEFAULT_CHARSET);
            return encodeSlash ? result : result.replace("%2F", "/");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * explain: URI编码，默认不对“/” 进行编码
     * author: 湛智
     * @param input 需要转换的串
     * date 2016/5/31 - 10:42
     **/
    public static String encode(CharSequence input) {
        return encode(input, false);
    }

    /**
     * explain: url参数键值encode（默认不转换十六进制）
     * 对于key=value的项，转换为 encode(key) + "=" + encode(value) 的形式。这里value可以是空字符串
     * 编码后将上面转换后的所有字符串按照字典顺序排序
     * 将排序后的字符串按顺序用 & 符号链接起来
     * author: 湛智
     * @param param 参数
     * date 2016/5/31 - 15:02
     **/
    public static String kvEncode(Map<String, Object> param) {
        return kvEncode(param, false);
    }

    /**
     * explain: url参数键值encode(可选择是否对中文进行十六进制编码)
     * 对于key=value的项，转换为 encode(key) + "=" + encode(value) 的形式。这里value可以是空字符串
     * 编码后将上面转换后的所有字符串按照字典顺序排序
     * 将排序后的字符串按顺序用 & 符号链接起来
     * author: 湛智
     * @param param 参数
     * date 2016/5/31 - 15:02
     **/
    public static String kvEncode(Map<String, Object> param, boolean isHex) {
        StringBuilder url = new StringBuilder();
        List<String> params = new ArrayList<>();
        Set<String> keys = param.keySet();
        for (String key : keys) {
            String encodeKey = isHex ? encode(key) : key;
            String encodeValue = isHex ? encode(String.valueOf(param.get(key))) : String.valueOf(param.get(key));
            params.add(encodeKey + "=" + encodeValue + "&");
        }
        Collections.sort(params);
        for (String s : params) {
            url.append(s);
        }
        String result = url.toString();
        return result.substring(0, result.length() - 1);
    }

    /**
     * explain: url参数键值encode(数组，可选择是否转换十六进制)
     * 对于key=value的项，转换为 encode(key) + "=" + encode(value) 的形式。这里value可以是空字符串
     * 编码后将上面转换后的所有字符串按照字典顺序排序
     * 将排序后的字符串按顺序用 & 符号链接起来
     * author: 湛智
     * @param param 参数
     * date 2016/5/31 - 15:02
     **/
    public static String kvEncodes(Map<String, String[]> param) {
        return kvEncodes(param, false);
    }

    /**
     * explain: url参数键值encode(数组、默认不转化十六进制)
     * 对于key=value的项，转换为 encode(key) + "=" + encode(value) 的形式。这里value可以是空字符串
     * 编码后将上面转换后的所有字符串按照字典顺序排序
     * 将排序后的字符串按顺序用 & 符号链接起来
     * author: 湛智
     * @param param 参数
     * date 2016/5/31 - 15:02
     **/
    public static String kvEncodes(Map<String, String[]> param, boolean isHex) {
        StringBuilder url = new StringBuilder();
        List<String> params = new ArrayList<>();
        Set<String> keys = param.keySet();
        for (String key : keys) {
            String encodeKey = isHex ? encode(key) : key;
            String encodeValue = isHex ? encode(String.valueOf(param.get(key)[0])) : String.valueOf(param.get(key)[0]);
            params.add(encodeKey + "=" + encodeValue + "&");
        }
        Collections.sort(params);
        for (String s : params) {
            url.append(s);
        }
        String result = url.toString();
        return result.substring(0, result.length() - 1);
    }

    /**
     * explain: 对url以及参数进行编码
     * author: 湛智
     * @param url 请求路径
     * @param encodeSlash 是否对 "/" 进行编码
     * @param param 参数
     * date 2016/5/31 - 15:11
     **/
    public static String uri(CharSequence url, Map<String, Object> param, boolean encodeSlash) {
        return encode(url, encodeSlash) + "?" + kvEncode(param);
    }

    /**
     * explain: 对url以及参数进行编码 （默认不对 “/”进行编码）
     * author: 湛智
     * @param url 请求路径
     * @param param 参数
     * date 2016/5/31 - 15:11
     **/
    public static String uri(CharSequence url, Map<String, Object> param) {
        return uri(url, param, false);
    }

    /**
     * explain: 对请求header编码 （可选择是否对每组参数进行分行处理 以及是否对参数名小写处理）
     * author: 湛智
     * @param head 请求头参数
     * @param isEnter 是否对每个参数进行分行处理
     * date 2016/6/2 - 9:56
     **/
    public static String header(Map<String, Object> head, boolean isEnter, boolean isLowerCase) {
        StringBuilder headStr = new StringBuilder();
        List<String> heads = new ArrayList<>();
        Set<String> keys = head.keySet();
        for (String key : keys) {
            heads.add((isLowerCase ? encode(key.toLowerCase()) : encode(key)) + ":" + encode(String.valueOf(head.get(key)).trim(), true) + (isEnter ? "\n" : ""));
        }
        Collections.sort(heads);
        for (String s : heads) {
            headStr.append(s);
        }
        return headStr.toString();
    }

    /**
     * explain: 对请求header编码 (默认对每组参数进行分行 以及参数名小写)
     * author: 湛智
     * @param head 请求头参数
     * date 2016/6/2 - 9:56
     **/
    public static String header(Map<String, Object> head) {
        return header(head, true, true);
    }

    /**
     * explain: 对参数key排序并使用joinkey 分隔 可选择是否对参数小写转换
     * author: 湛智
     * @param param 参数
     * @param joinKey 分隔符
     * @return
     * date 2016/6/2 - 10:40
     **/
    public static String sortKeyJoin(Map<String, Object> param, String joinKey, boolean isLowerCase) {
        Set<String> keys = param.keySet();
        StringBuilder joinResult = new StringBuilder();
        for (String key : keys) {
            joinResult.append(key).append(joinKey);
        }
        String result = joinResult.toString();
        result = isLowerCase ? result.toLowerCase() : result;
        return result.substring(0, result.length() - 1);
    }

    /**
     * explain: 对参数key排序并使用joinkey 分隔 默认对参数小写转换
     * author: 湛智
     * @param param 参数
     * @param joinKey 分隔符
     * @return
     * date 2016/6/2 - 10:40
     **/
    public static String sortKeyJoin(Map<String, Object> param, String joinKey) {
        return sortKeyJoin(param, joinKey, true);
    }
}
