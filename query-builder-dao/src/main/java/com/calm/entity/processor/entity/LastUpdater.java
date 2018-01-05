package com.calm.entity.processor.entity;

/**
 * Created by dingqihui on 2016/9/8.
 *
 * @author dingqihui
 */
public interface LastUpdater {
    /**
     * 获得最后更新人ID.
     *
     * @return 更新人ID
     */
    Integer getLastUpdaterId();

    /**
     * 设置最后更新人ID.
     *
     * @param lastUpdaterId 跟新人ID
     */
    void setLastUpdaterId(Integer lastUpdaterId);
}
