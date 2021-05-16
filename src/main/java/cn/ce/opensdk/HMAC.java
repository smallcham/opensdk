package cn.ce.opensdk;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class HMAC {

    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     *
     * 功能描述:生成签名数据
     * author 湛智
     * @throws InvalidKeyException
     * time 2015年9月19日
     */
    private static byte[] getSignature(byte[] accessKeySecret, byte[] accessKey, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(accessKeySecret, algorithm));
        return mac.doFinal(accessKey);
    }

    /**
     * explain: HmacSHA1 + Base64
     * author: 湛智
     * date 2016/5/31 - 10:55
     **/
    public static String base64(byte[] accessKeySecret, byte[] accessKey) {
        try {
            return Base64.encodeBase64String(getSignature(accessKeySecret, accessKey, HMAC_SHA1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * explain: HmacSHA1 + Base64
     * author: 湛智
     * date 2016/5/31 - 10:55
     **/
    public static String base64(String accessKeySecret, String accessKey) {
        return base64(accessKeySecret.getBytes(), accessKey.getBytes());
    }

    /**
     * explain: HMAC SHA-256
     * author: 湛智
     * date 2016/5/31 - 10:55
     **/
    public static String sha256(byte[] accessKeySecret, byte[] accessKey) {
        try {
            return digest(accessKeySecret, accessKey, HMAC_SHA256);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * explain: HMAC SHA-256
     * author: 湛智
     * date 2016/5/31 - 10:55
     **/
    public static String sha256(String accessKeySecret, String accessKey) {
        return sha256(accessKeySecret.getBytes(), accessKey.getBytes());
    }

    /**
     * explain: HMAC 自定义加密
     * author: 湛智
     * @param algorithm 加密类型 如 ：“HmacSHA256”
     * date 2016/5/31 - 10:55
     **/
    public static String digest(byte[] accessKeySecret, byte[] accessKey, String algorithm) {
        try {
            return Hex.encodeHexString(getSignature(accessKeySecret, accessKey, algorithm));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * explain: HMAC 自定义加密 (默认HmacSHA1)
     * author: 湛智
     * date 2016/5/31 - 10:55
     **/
    public static String digest(byte[] accessKeySecret, byte[] accessKey) {
        return digest(accessKeySecret, accessKey, HMAC_SHA1);
    }
}
