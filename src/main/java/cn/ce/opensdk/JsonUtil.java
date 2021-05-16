package cn.ce.opensdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by medusa on 2016/11/24.
 * explain: Json解析工具类 - 基于 Jackson<br/>
 * <b>为了不影响当前正在使用[xinnet-utils第一版]的工程，基于[xinnet-utils 1.3] 重构 为 [xinnet-utils2] </b><br/>
 * <hr/>
 * <b>该版本主要做以下更改：</b>
 * <li>将单例的工具类修改为需要通过{@link JsonFactory#newInstance()} 等方法进行实例化，提高可扩展性</li>
 * <li>禁用new JsonUtil，实例化必须通过{@link JsonFactory#newInstance()} 等方法</li>
 * <li>提供基本配置的单例模式{@link Single}</li>
 * <li>默认不转换为null的字段</li>
 * <li>不存在的字段进行转换不抛出异常</li>
 */
public class JsonUtil {
    private static final Logger LOG;
    private final ObjectMapper mapper;

    static {
        LOG = Logger.getLogger(JsonUtil.class);
    }

    JsonUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }


    /**
     * object to Json 串
     * @param object 要转换的对象
     * @return String
     */
    public String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("object " + object + " cast to string failure", e);
            return "";
        }
    }

    /**
     * explain: json串 转 JsonNode
     * author: 湛智
     * @param json json字符串
     * @return JsonNode
     * date 2016/1/27 - 16:48
     **/
    public JsonNode toNode(String json) {
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            LOG.error("json cast to json node failure", e);
            return null;
        }
    }

    /**
     * Json串 to Bean
     * @param json json字符串
     * @param clazz 要返回的类型
     * @return T
     */
    public <T> T toBean(String json, Class<T> clazz) {
        return convert(json, mapper.getTypeFactory().uncheckedSimpleType(clazz));
    }

    /**
     * Json串 to List
     * @param json json字符串
     * @param clazz 泛型类型
     * @param <T> T
     * @return List<T>
     */
    public <T> List<T> toList(String json, Class<T> clazz) {
        return convert(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    /**
     * Json 串 to Map（自定义 K，V 类型）
     * @param json json字符串
     * @param k Key类型
     * @param v Value类型
     * @return Map
     */
    public <K, V> Map<K, V> toMap(String json, Class<?> k, Class<?> v) {
        return convert(json, mapper.getTypeFactory().constructMapType(Map.class, k, v));
    }

    /**
     * Json 串 to Map（默认K：String, V：Object）
     * @param json json字符串
     * @return Map<String,Object>
     */
    public Map<String, Object> toMap(String json) {
        return toMap(json, String.class, Object.class);
    }

    /**
     * Json串 to Bean （JavaType参数）
     * @param json json字符串
     * @param javaType JavaType参数
     * @param <T> T
     * @return T
     */
    private <T> T convert(String json, JavaType javaType) {
        try {
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            LOG.error("json string cast failure", e);
            return null;
        }
    }

    /**
     * 提供基本配置的单例模式JsonUtil
     */
    public static class Single {

        private final static JsonUtil JSON_UTILS = JsonFactory.newInstance();

        public static String toJson(Object object) {
            return JSON_UTILS.toJson(object);
        }

        public static <T> T toBean(String json, Class<T> clazz) {
            return JSON_UTILS.toBean(json, clazz);
        }

        public static <K, V> Map<K, V> toMap(String json, Class<?> k, Class<?> v) {
            return JSON_UTILS.toMap(json, k, v);
        }

        public static Map<String, Object> toMap(String json) {
            return JSON_UTILS.toMap(json);
        }

        public static <T> List<T> toList(String json, Class<T> clazz) {
            return JSON_UTILS.toList(json, clazz);
        }

        public static JsonNode toNode(String json) {
            return JSON_UTILS.toNode(json);
        }
    }
}
