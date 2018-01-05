package com.calm.entity.processor.dao;


import com.calm.entity.processor.context.UserHolder;
import com.calm.entity.processor.entity.*;
import com.calm.entity.processor.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * MybatisBaseDao.
 * Created by dingqihui on 2016/9/8.
 *
 * @param <I> id类型
 * @param <T> 实体类型
 * @author dingqihui
 */
public class MybatisBaseDao<I, T extends BaseEntity<I>, M extends BaseMapper<I, T>> {
    /**
     * mapper.
     */
    private M mapper;

    /**
     * 根据对象删除，如果是LogicallyDeletable类型执行逻辑删除.
     *
     * @param t 删除对象
     * @return 影响记录数
     */
    public int deleteEntity(final T t) {
        boolean delete = true;
        if (t instanceof LogicallyDeletable) {
            ((LogicallyDeletable) t).setDeleted(true);
            delete = false;
        }
        if (delete) {
            return mapper.deleteByPrimaryKey(t.getId());
        } else {
            return updateByPrimaryKeySelective(t);
        }
    }

    /**
     * 插入对象的所有属性,同时支持,CreateTime,Creator,LastUpdateTime,LastUpdater.
     *
     * @param record 被插入对象
     * @return 影响记录数
     */
    public int insert(final T record) {
        create(record);
        lastUpdate(record);
        deleted(record);
        return mapper.insert(record);
    }

    /**
     * 插入对象的非空属性,同时支持,CreateTime,Creator,LastUpdateTime,LastUpdater.
     *
     * @param record 被插入对象
     * @return 影响记录数
     */
    public int insertSelective(final T record) {
        create(record);
        lastUpdate(record);
        deleted(record);
        return mapper.insertSelective(record);
    }

    private void deleted(T record) {
        if (record instanceof LogicallyDeletable) {
            ((LogicallyDeletable) record).setDeleted(false);
        }
    }

    /**
     * 根据主键更新对象的非空属性,同时支持,LastUpdateTime,LastUpdater.
     *
     * @param record 被更新对象
     * @return 影响记录数
     */
    public int updateByPrimaryKeySelective(final T record) {
        lastUpdate(record);
        return mapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 根据主键更新对象的所有属性,同时支持,LastUpdateTime,LastUpdater.
     *
     * @param record 被更新对象
     * @return 影响记录数
     */
    public int updateByPrimaryKey(final T record) {
        lastUpdate(record);
        return mapper.updateByPrimaryKey(record);
    }

    /**
     * 根据试题对象查询集合.
     *
     * @param record 更新的对象
     * @return 结果集合
     */
    public List<T> listByEntity(final T record) {
        return mapper.listByEntity(record);
    }

    /**
     * 根据试题对象查询集合.
     *
     * @param record 更新的对象
     * @return 结果集合
     */
    public T loadByEntity(final T record) {
        return mapper.loadByEntity(record);
    }

    /**
     * 根据主键查询对象.
     *
     * @param id 主键ID
     * @return 查询结果对象
     */
    public T selectByPrimaryKey(final I id) {
        return mapper.selectByPrimaryKey(id);
    }

    /**
     * LastUpdateTime LastUpdater支持.
     *
     * @param record 实体对象
     */
    private void lastUpdate(final T record) {
        if (record instanceof LastUpdateTime) {
            ((LastUpdateTime) record).setLastUpdateTime(new Date());
        }
        if (record instanceof LastUpdater) {
            ((LastUpdater) record).setLastUpdaterId(UserHolder.get());
        }
    }

    /**
     * CreateTime Creator支持.
     *
     * @param record 实体对象
     */
    private void create(final T record) {
        if (record instanceof CreateTime) {
            ((CreateTime) record).setCreateTime(new Date());
        }
        if (record instanceof Creator) {
            ((Creator) record).setCreatorId(UserHolder.get());
        }
    }

    public void setMapper(final M mapper) {
        this.mapper = mapper;
    }

    public M getMapper() {
        return mapper;
    }
}
