package com.bluesgao.demo.util;

import com.alibaba.fastjson.JSON;

public class JsonUtils {
    public static byte[] toJSONBytes(Object obj) {
        return JSON.toJSONBytes(obj);
    }

    static public <T> T parseObject(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
