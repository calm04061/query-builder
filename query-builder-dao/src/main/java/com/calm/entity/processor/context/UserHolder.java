package com.calm.entity.processor.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dingqihui on 2017/2/9.
 */
public class UserHolder {

    private static ThreadLocal<Map<String, Integer>> idMapHolder = new ThreadLocal<>();

    @Deprecated
    protected static void set(Integer id) {
        set(UserIdTransfer.USER_ID_KEY, id);
    }

    @Deprecated
    public static Integer get() {
        return get(UserIdTransfer.USER_ID_KEY);
    }

    public static void set(String key, Integer id) {
        Map<String, Integer> stringIntegerMap = idMapHolder.get();
        if (stringIntegerMap == null) {
            stringIntegerMap = new ConcurrentHashMap<>();
            idMapHolder.set(stringIntegerMap);
        }
        if (id == null) {
            return;
        }
        stringIntegerMap.put(key, id);
    }

    public static Integer get(String key) {
        Map<String, Integer> stringIntegerMap = idMapHolder.get();
        if (stringIntegerMap == null) {
            return null;
        }
        return stringIntegerMap.get(key);
    }
}
