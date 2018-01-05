package com.calm.entity.processor.mapper;

import com.calm.entity.processor.entity.BaseEntity;

import java.util.List;

/**
 * Created by dingqihui on 2016/9/7.
 *
 * @author dingqihui
 */
public interface BaseMapper<I, T extends BaseEntity<I>> {
    /**
     * 根据主键删除.
     *
     * @param id 主键ID
     * @return 影响记录数
     */
    int deleteByPrimaryKey(I id);

    /**
     * 插入所有基本属性数据.
     *
     * @param record 被查入的对象
     * @return 影响记录数
     */
    int insert(T record);

    /**
     * 插入所有非空属性数据.
     *
     * @param record 被查入的对象
     * @return 影响记录数
     */
    int insertSelective(T record);

    /**
     * 根据ID查询对象.
     *
     * @param id id
     * @return 结果对象
     */
    T selectByPrimaryKey(I id);

    /**
     * 更新所有非空属性数据.
     *
     * @param record 更新入的对象
     * @return 影响记录数
     */
    int updateByPrimaryKeySelective(T record);

    /**
     * 更新所有基本属性数据.
     *
     * @param record 更新的对象
     * @return 影响记录数
     */
    int updateByPrimaryKey(T record);

    /**
     * 根据试题对象查询集合.
     *
     * @param record 更新的对象
     * @return 结果集合
     */
    List<T> listByEntity(T record);

    /**
     * 根据试题对象查询对象.
     *
     * @param record 更新的对象
     * @return 结果对象
     */
    T loadByEntity(T record);
}
