package com.calm.entity.processor.entity;

import java.util.List;

/**
 * Created by dingqihui on 2016/9/8.
 *
 * @author dingqihui
 */
public interface Tree<I, T> extends BaseEntity<I> {
    I getParentId();

    void setParentId(I parentId);

    List<T> getChildren();

    void setChildren(List<T> children);

    T getParent();

    void setParent(T parent);
}
