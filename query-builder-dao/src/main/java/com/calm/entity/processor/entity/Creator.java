package com.calm.entity.processor.entity;

/**
 * Created by dingqihui on 2016/9/8.
 *
 * @author dingqihui
 */
public interface Creator {
    /**
     * 获得创建人ID.
     *
     * @return 创建人id
     */
    Integer getCreatorId();

    /**
     * 设置创建人ID.
     *
     * @param creatorId 创建人ID
     */
    void setCreatorId(Integer creatorId);
}
