package com.calm.entity.processor.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jiazebiao on 2016/11/28.
 */
public class CommonUtils {
    public static <T> List<T> pageList(List<T> list, int pageNo, int pageSize) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<T> result = new ArrayList<T>();
        int start = (pageNo - 1) * pageSize;
        if (start > list.size()) {
            return null;
        }
        int end = start + pageSize;
        end = end > list.size() ? list.size() : end;
        result.addAll(list.subList(start, end));
        return result;
    }

}
