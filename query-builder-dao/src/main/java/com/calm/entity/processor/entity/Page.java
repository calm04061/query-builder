package com.calm.entity.processor.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangxing on 2016/10/11.
 */
public class Page<T> implements Serializable {
    /**
     * 页码.
     */
    private Integer currentPage = 1;
    /**
     * 每页显示条数.
     */
    private Integer pageSize = 10;
    /**
     * 总条数.
     */
    private Integer totalCount = 0;
    /**
     * List<T>.
     */
    private List<T> data;

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(final Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPage() {
        return (totalCount + pageSize - 1) / pageSize;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(final Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(final List<T> data) {
        this.data = data;
    }
}
