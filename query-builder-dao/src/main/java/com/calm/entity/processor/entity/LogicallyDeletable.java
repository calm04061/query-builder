package com.calm.entity.processor.entity;

/**
 * Created by dingqihui on 2016/9/8.
 *
 * @author dingqihui
 */
public interface LogicallyDeletable {
    /**
     * 是否已经删除.
     *
     * @return 判定结果
     */
    Boolean getDeleted();

    /**
     * 标识是否已删除.
     *
     * @param deleted 标识
     */
    void setDeleted(Boolean deleted);
}
