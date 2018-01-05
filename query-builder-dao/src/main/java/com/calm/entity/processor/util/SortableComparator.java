package com.calm.entity.processor.util;

import com.calm.entity.processor.entity.Sortable;

import java.util.Comparator;

/**
 * Created by dingqihui on 2016/12/20.
 */
public class SortableComparator implements Comparator<Sortable> {
    public static final SortableComparator DEFAULT_INST = new SortableComparator();

    @Override
    public int compare(Sortable o1, Sortable o2) {
        Integer orderIndex1 = o1.getOrderIndex();
        Integer orderIndex2 = o2.getOrderIndex();
        if (orderIndex1 != null && orderIndex2 == null) {
            return 1;
        }
        if (orderIndex1 == null && orderIndex2 != null) {
            return -1;
        }

        if (orderIndex1 == null) {
            return 0;
        }
        return orderIndex1.compareTo(orderIndex2);
    }
}
