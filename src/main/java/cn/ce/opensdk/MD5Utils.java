package cn.ce.opensdk;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 王湛智 on 2016/3/24.
 * MD5
 */
public class MD5Utils {
    private static MessageDigest digest;
    private static final String DEFAULT_CHARSET = "UTF-8";

    static {
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * explain: MD5加密并转换为16进制字符串 (自定义编码)
     * author: 湛智
     * @param data 需要加密的字符串
     * @return String 加密串 失败返回空字符串
     * date 2016/3/24 - 10:53
     **/
    public static String encode(String data, String charset) {
        try {
            return Hex.encodeHexString(digest.digest(data.getBytes(charset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * explain: MD5加密（返回字节）
     * author: 湛智
     * @param data 需要加密的字节
     * date 2017/3/7 - 13:39
     **/
    public static byte[] encode(byte[] data) {
        return digest.digest(data);
    }

    /**
     * explain: MD5加密并转换为16进制字符串 (默认UTF-8)
     * author: 湛智
     * @param data 需要加密的字符串
     * @return  String 加密串 失败返回空字符串
     * date 2016/3/29 - 14:21
     **/
    public static String encode(String data) {
        return encode(data, DEFAULT_CHARSET);
    }

    public static void main(String[] args) {
        System.out.println(encode("asd"));
    }
}
