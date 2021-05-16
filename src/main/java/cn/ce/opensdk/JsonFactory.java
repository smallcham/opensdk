package cn.ce.opensdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by wzz on 2016/11/24.
 * explain:
 */
public class JsonFactory {

    private JsonFactory() {}

    public static JsonUtil newInstance() {
        return new JsonUtil(newMapper());
    }

    public static JsonUtil newInstance(ObjectMapper mapper) {
        return new JsonUtil(mapper);
    }

    public static JsonUtil newInstance(JsonInclude.Include type) {
        return new JsonUtil(newMapper(type));
    }

    public static ObjectMapper newMapper(JsonInclude.Include type, DeserializationFeature feature, boolean state) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(type);
        mapper.configure(feature, state);
        return mapper;
    }

    public static ObjectMapper newMapper(JsonInclude.Include type) {
        return newMapper(type, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper newMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

}
