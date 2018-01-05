package com.calm.entity.processor.entity;

import java.io.Serializable;

/**
 * Created by dingqihui on 2016/9/8.
 *
 * @param <I> 主键类型
 * @author dingqihui
 */
public interface BaseEntity<I> extends Serializable {
    /**
     * 主键.
     *
     * @return 获得主键
     */
    I getId();

    /**
     * 设置主键.
     *
     * @param id 主键
     */
    void setId(I id);
}
