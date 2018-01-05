package com.calm.entity.processor.entity;

/**
 * Created by dingqihui on 2016/12/10.
 * 排序
 */
public interface Sortable {
    /**
     * @return 序号
     */
    Integer getOrderIndex();

    void setOrderIndex(Integer orderIndex);
}
