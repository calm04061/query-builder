package com.calm.entity.processor.entity;

import java.util.Date;

/**
 * Created by dingqihui on 2016/9/8.
 *
 * @author dingqihui
 */
public interface LastUpdateTime {
    /**
     * 设置最后更新时间.
     *
     * @param lastUpdateTime 最后更新时间
     */
    void setLastUpdateTime(Date lastUpdateTime);

    /**
     * 获得最后更新时间.
     *
     * @return 最后更新时间
     */
    Date getLastUpdateTime();
}
