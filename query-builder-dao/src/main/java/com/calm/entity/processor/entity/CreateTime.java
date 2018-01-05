package com.calm.entity.processor.entity;

import java.util.Date;

/**
 * Created by dingqihui on 2016/9/8.
 *
 * @author dingqihui
 */
public interface CreateTime {
    /**
     * 设置创建时间.
     *
     * @param createTime 创建时间
     */
    void setCreateTime(Date createTime);

    /**
     * 获得创建时间.
     *
     * @return 获得创建时间
     */
    Date getCreateTime();
}
