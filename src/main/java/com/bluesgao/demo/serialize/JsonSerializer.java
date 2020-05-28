package com.bluesgao.demo.serialize;

import com.bluesgao.demo.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte getSerializerAlogrithm() {
        return 1;
    }

    @Override
    public byte[] serialize(Object object) {
        return JsonUtils.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JsonUtils.parseObject(clazz, bytes);
    }
}
