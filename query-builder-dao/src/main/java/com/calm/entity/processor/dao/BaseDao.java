package com.calm.entity.processor.dao;


import com.calm.entity.processor.Constants;
import com.calm.entity.processor.QueryBuilder;
import com.calm.entity.processor.context.InstitutionIdTransfer;
import com.calm.entity.processor.context.UserHolder;
import com.calm.entity.processor.context.UserIdTransfer;
import com.calm.entity.processor.entity.*;
import com.calm.entity.processor.exception.DaoException;
import com.calm.entity.processor.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by dingqihui on 2016/11/26.
 *
 * @author dingqihui
 */
public abstract class BaseDao<I, T extends BaseEntity<I>> extends AbstractDao<I, T> {
    private Logger logger = LoggerFactory.getLogger(BaseDao.class);
    @Resource
    private JdbcTemplate diyJdbcTemplate;

    private static final String SORT_ROW_NAME = "order_index";

    //位置移动逻辑需要加锁
    private Object sortableMoveLock = new Object();

    private static final int DATA_COUNT_PER_CONNECTION = 100;

    private static final Map<String, String> SQL_CACHE = new HashMap<>();
    private static final Set<Class<?>> ACCESS_TYPES = new HashSet<>();

    static {
        ACCESS_TYPES.add(int.class);
        ACCESS_TYPES.add(Integer.class);
        ACCESS_TYPES.add(String.class);
        ACCESS_TYPES.add(Date.class);
        ACCESS_TYPES.add(java.sql.Date.class);
        ACCESS_TYPES.add(Timestamp.class);
        ACCESS_TYPES.add(double.class);
        ACCESS_TYPES.add(float.class);
        ACCESS_TYPES.add(Double.class);
        ACCESS_TYPES.add(Float.class);
        ACCESS_TYPES.add(short.class);
        ACCESS_TYPES.add(Short.class);
        ACCESS_TYPES.add(Boolean.class);
        ACCESS_TYPES.add(boolean.class);
        ACCESS_TYPES.add(byte.class);
        ACCESS_TYPES.add(Byte.class);
    }

    protected BaseDao() {
        super();
    }

    /**
     * @param tableName:如果非空则以此为表名，否则默认
     * @param primaryKeyName:如果非空则以此为主键列名，否则默认
     */
    protected BaseDao(String tableName, String primaryKeyName) {
        super(tableName, primaryKeyName);
    }

    /**
     * 按id查询
     *
     * @param id
     * @return
     */
    public T selectById(I id) {
        StringBuilder sql = new StringBuilder(SELECT_SENTENCE).append(" where ").append(this.PRIMARY_KEY_NAME).append(" = ?");
        if (LogicallyDeletable.class.isAssignableFrom(getBeanType())) {
            sql.append(" and deleted = 0");
        }
        List<T> list = getJdbcTemplate().query(sql.toString(), new Object[]{id}, new BeanPropertyRowMapper<>(beanType));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 根据id列表值in查询列表
     *
     * @param ids
     * @return
     */
    public List<T> selectByIds(List<I> ids) {
        return selectRowValueInSingRequest(this.PRIMARY_KEY_NAME, ids, null, null, false);
    }

    /**
     * 根据字段名和值in查询列表
     *
     * @param values
     * @param columnName
     * @return
     */
    public List<T> selectByColumnNameAndValues(String columnName, List<I> values) {
        StringBuilder sql = new StringBuilder(SELECT_SENTENCE).append(" where ").append(columnName).append(" in ?");
        if (LogicallyDeletable.class.isAssignableFrom(getBeanType())) {
            sql.append(" and deleted = 0");
        }
        return getJdbcTemplate().query(sql.toString(), values.toArray(), new BeanPropertyRowMapper<>(beanType));
    }

    /**
     * 分批次查询(如果id数量大于100，一次获取100条，最后合并结果集）,不保证按查询条件的顺序
     *
     * @param ids
     * @return
     */
    public List<T> selectByIds(List<I> ids, List<String> orderRows) {
        return selectByIds(ids, orderRows, false);
    }

    /**
     * 按主键批量查询，将查询结果组装为map(id:object)
     *
     * @param ids
     * @return
     */
    public Map<I, T> selectResutMapByIds(List<I> ids, List<String> orderRows) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException(Constants.PARAM_CANNOT_EMPTY);
        }
        Map<I, T> result = new HashMap<>();
        List<T> list = this.selectByIds(ids, orderRows, true);
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        Field f = null;
        try {
            f = list.get(0).getClass().getField(this.PRIMARY_KEY_NAME);
            f.setAccessible(true);
            for (T item : list) {
                result.put((I) f.get(item), item);
            }
        } catch (Exception e) {
            logger.error("selectResutMapByIds", e);
            throw new DaoException();
        }
        return result;
    }

    /**
     * 分批次查询(如果id数量大于100，一次获取100条，最后合并结果集）
     *
     * @param ids
     * @param inOrder:结果集是否维持查询条件的顺序
     * @return
     */
    public List<T> selectByIds(List<I> ids, List<String> orderRows, boolean inOrder) {
        return selectRowValueIn(PRIMARY_KEY_NAME, ids, null, orderRows, inOrder);
    }

    /**
     * in 查询根据 in 条件拼装结果集
     *
     * @param rowName
     * @param keyType     : 返回的 key类型
     * @param params
     * @param otherParams
     * @param <R>
     * @return
     */
    protected <R> Map<R, List<T>> selectResultMapByRowValueIn(String rowName, Class<R> keyType, List<? extends Serializable> params, Map<String, Object> otherParams, List<String> orderRows) {
        List<T> list = this.selectRowValueIn(rowName, params, otherParams, orderRows, true);
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        Map<R, List<T>> result = new HashMap<>();
        String propertyName = this.lineToHump(rowName);
        try {
            Field f = this.beanType.getDeclaredField(propertyName);
            f.setAccessible(true);
            for (T entity : list) {
                R key = (R) f.get(entity);
                List<T> valueList = result.get(key);
                if (CollectionUtils.isEmpty(valueList)) {
                    valueList = new ArrayList<>();
                    result.put(key, valueList);
                }
                valueList.add(entity);
            }
            return result;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("selectResultMapByRowValueIn --> " + e.getMessage(), e);
            throw new DaoException(e);
        }
    }

    protected List<T> selectRowValueIn(String rowName, List<Object> params, List<String> orderRows) {
        return selectRowValueIn(rowName, params, null, orderRows, false);
    }

    /**
     * @param rowName     要查询的mysql字段名
     * @param params      字段值列表，用于in 查询条件
     * @param otherParams : 其他的字段 key-value,全等条件
     * @param inOrder
     * @return
     */
    protected List<T> selectRowValueIn(String rowName, List<?> params, Map<String, Object> otherParams, List<String> orderRows, boolean inOrder) {
        if (CollectionUtils.isEmpty(params)) {
            return new ArrayList<>();
        }
        if (params.size() <= DATA_COUNT_PER_CONNECTION) {
            return selectRowValueInSingRequest(rowName, params, otherParams, orderRows, inOrder);
        }
        List<T> result = new ArrayList<>();
        for (int page = 1; ; page++) {
            List<?> subParams = CommonUtils.pageList(params, page, DATA_COUNT_PER_CONNECTION);
            if (CollectionUtils.isEmpty(subParams)) {
                return result;
            } else if (subParams.size() < DATA_COUNT_PER_CONNECTION) {
                result.addAll(selectRowValueInSingRequest(rowName, subParams, otherParams, orderRows, inOrder));
                return result;
            }
            result.addAll(selectRowValueInSingRequest(rowName, subParams, otherParams, orderRows, inOrder));

        }
    }

    /**
     * 列值准确匹配
     *
     * @param params : key=列名,value=列值
     * @return
     */
    public List<T> selectRowValueEqual(Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            throw new IllegalArgumentException(Constants.PARAM_CANNOT_EMPTY);
        }
        StringBuilder sql = new StringBuilder(SELECT_SENTENCE).append(" where ");
        List<Object> paramValues = new ArrayList<>();
        boolean start = true;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String rn = e.getKey();
            Object value = e.getValue();
            if (Constants.ORDER_SORT.equals(rn)) {
                continue;
            }
            paramValues.add(value);
            if (start) {
                sql.append(" ").append(rn).append(" = ?");
                start = false;
            } else {
                sql.append(" and ").append(rn).append(" = ?");
            }
        }
        if (LogicallyDeletable.class.isAssignableFrom(getBeanType())) {
            sql.append(" and deleted = 0");
        }
        if (params.get(Constants.ORDER_SORT) != null) {
            sql.append(" order by ").append(params.get(Constants.ORDER_SORT));
        } else {
            if (Sortable.class.isAssignableFrom(getBeanType())) {
                sql.append(" order by order_index asc");
            } else if (CreateTime.class.isAssignableFrom(getBeanType())) {
                sql.append(" order by create_time desc");
            }
        }
        logger.info("selectRowValueEqual -->" + sql);
        List<T> list = getJdbcTemplate().query(sql.toString(), paramValues.toArray(), new BeanPropertyRowMapper<>(beanType));
        return list;
    }

    /**
     * @param rowName
     * @param params::in查询的条件,如[1,2,3]
     * @param otherParams:其他附加的等值查询条件，key是列名，value是列值
     * @param inOrder
     * @return
     */
    public List<T> selectRowValueInSingRequest(String rowName, List<?> params, Map<String, Object> otherParams, List<String> orderRows, boolean inOrder) {
        StringBuilder sql = new StringBuilder(SELECT_SENTENCE).append(" where ").append(rowName).append(" in (");
        if (CollectionUtils.isEmpty(params)) {
            logger.error("selectRowValueInSingRequest --> 非法传参 : params = " + params);
            throw new IllegalArgumentException(Constants.PARAM_CANNOT_EMPTY);
        }
        if (params.size() == 1) {
            Map<String, Object> map = new HashMap<>();
            map.put(rowName, params.get(0));
            if (MapUtils.isNotEmpty(otherParams)) {
                for (Map.Entry<String, Object> e : otherParams.entrySet()) {
                    map.put(e.getKey(), e.getValue());
                }
            }
            return selectRowValueEqual(map);
        }
        for (int i = 0; i < params.size() - 1; i++) {
            sql.append("?,");
        }
        sql.append("?)");
        List<Object> otherParamValues = new ArrayList<>();
        if (MapUtils.isNotEmpty(otherParams)) {
            for (Map.Entry<String, Object> e : otherParams.entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                otherParamValues.add(value);
                sql.append(" and ").append(key).append(" = ?");
            }
        }
        if (LogicallyDeletable.class.isAssignableFrom(getBeanType())) {
            sql.append(" and deleted = 0");
        }
        if (inOrder) {
            sql.append(" order by field(").append(rowName).append(",");
            for (int i = 0; i < params.size() - 1; i++) {
                sql.append("?,");
            }
            sql.append("?)");
            if (CollectionUtils.isNotEmpty(orderRows)) {
                for (String row : orderRows) {
                    sql.append(",").append(row);
                }
            }
            Object[] params2 = ArrayUtils.addAll(params.toArray(), otherParamValues.toArray());
            params2 = ArrayUtils.addAll(params2, params.toArray());
            logger.info("selectRowValueInSingRequest --> " + sql);
            return getJdbcTemplate().query(sql.toString(), params2, new BeanPropertyRowMapper<>(beanType));
        } else {
            if (CollectionUtils.isNotEmpty(orderRows)) {
                sql.append(" order by ");
                for (String row : orderRows) {
                    sql.append(row).append(",");
                }
                sql.setLength(sql.length() - 1);
            }
            Object[] finalParams = ArrayUtils.addAll(params.toArray(), otherParamValues.toArray());
            logger.info("selectRowValueInSingRequestb --> " + sql);
            return getJdbcTemplate().query(sql.toString(), finalParams, new BeanPropertyRowMapper<>(beanType));
        }
    }


    protected JdbcTemplate getJdbcTemplate() {
        return diyJdbcTemplate;
    }

    protected void setJdbcTemplate(JdbcTemplate jt) {
        this.diyJdbcTemplate = jt;
    }

    /**
     * 插入对象,并生成id
     *
     * @param obj 实体对象
     */
    public void insert(T obj) {
        this.insert(obj, true);
    }

    /**
     * 插入对象
     *
     * @param obj      实体对象
     * @param createId 是否生成id
     */
    public void insert(T obj, boolean createId) {
        create(obj);
        institutional(obj);
        lastUpdate(obj);
        if (obj instanceof LogicallyDeletable) {
            ((LogicallyDeletable) obj).setDeleted(false);
        }
        String sql = SQL_CACHE.get(getBeanType().getName());
        Class<?> clazz = getBeanType();
        Method getters[] = clazz.getMethods();
        if (StringUtils.isEmpty(sql)) {
            sql = buildSql(clazz, getters);
        }
        final List<Object> args = new ArrayList<>();

        buildArgs(obj, args, getters);

        insert(obj, createId, sql, args);
    }

    /*
     * 批量添加（可以生成实体id）
     * @param list
     */
    public void batchInsert(Collection<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            logger.warn("batchInsert --> 批量插入数据为空 : " + list);
            return;
        }
        String sql = SQL_CACHE.get(getBeanType().getName());
        Class<?> clazz = getBeanType();
        Method getters[] = clazz.getMethods();
        if (StringUtils.isEmpty(sql)) {
            sql = buildSql(clazz, getters);
        }
        final List<Object[]> argsList = new ArrayList<>();
        for (T obj : list) {
            create(obj);
            lastUpdate(obj);
            if (obj instanceof LogicallyDeletable) {
                ((LogicallyDeletable) obj).setDeleted(false);
            }
            final List<Object> args = new ArrayList<>();

            buildArgs(obj, args, getters);
            argsList.add(args.toArray());
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();

        batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] objects = argsList.get(i);
                int index = 1;
                for (Object obj : objects) {
                    ps.setObject(index++, obj);
                }
            }

            @Override
            public int getBatchSize() {
                return argsList.size();
            }
        }, keyHolder);
        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        Iterator<T> iterator = list.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            Map<String, Object> stringObjectMap = keyList.get(index);
            for (Map.Entry<String, Object> e : stringObjectMap.entrySet()) {
                Number value = (Number) e.getValue();
                t.setId(convertToKeyType(value));
            }
            index++;
        }
    }

    private String buildSql(Class<?> clazz, Method[] getters) {
        String sql;
        StringBuilder stringBuilder = new StringBuilder("insert into ");
        stringBuilder.append(generateTableName());
        stringBuilder.append("(");

        List<String> fields = new ArrayList<>();
        List<String> or = new ArrayList<>();

        buildSql(getters, fields, or);
        stringBuilder.append(StringUtils.join(fields, ","));
        stringBuilder.append(") values (");
        stringBuilder.append(StringUtils.join(or, ","));
        stringBuilder.append(")");

        sql = stringBuilder.toString();
        SQL_CACHE.put(clazz.getName(), sql);
        return sql;
    }

    private void buildSql(Method[] getters, List<String> fields, List<String> or) {
        for (Method m : getters) {
            String name = m.getName();
            if (!name.startsWith("get")) {
                continue;
            }
            if (!ACCESS_TYPES.contains(m.getReturnType())) {
                continue;
            }
            if (haveIgnoreAnnotation(m)) {
                continue;
            }
            StringBuilder columnName = classNameToDbName(name.substring(3));
            fields.add(columnName.toString());
            or.add("?");
        }
    }

    private void insert(T obj, boolean createId, final String sql, final List<Object> args) {
        if (createId) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            getJdbcTemplate().update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {
                    PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    for (Object obj : args) {
                        ps.setObject(index++, obj);
                    }
                    return ps;
                }
            }, keyHolder);
            obj.setId(convertToKeyType(keyHolder.getKey()));
        } else {
            getJdbcTemplate().update(sql, args.toArray());
        }
    }

    /**
     * 插入对象(非空属性)，并生成ID,
     *
     * @param obj 实体对象
     */
    public void insertNotNull(T obj) {
        this.insertNotNull(obj, true);
    }

    /**
     * 插入对象(非空属性)
     *
     * @param obj      实体对象
     * @param createId 是否生成ID
     */
    public void insertNotNull(T obj, boolean createId) {
        create(obj);
        institutional(obj);
        lastUpdate(obj);
        if (obj instanceof LogicallyDeletable) {
            ((LogicallyDeletable) obj).setDeleted(false);
        }
        Class<T> clazz = getBeanType();
        Method getters[] = clazz.getMethods();
        List<Object> args = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        List<String> or = new ArrayList<>();
        for (Method m : getters) {
            String name = m.getName();
            if (!name.startsWith("get")) {
                continue;
            }
            if (!ACCESS_TYPES.contains(m.getReturnType())) {
                continue;
            }
            if (haveIgnoreAnnotation(m)) {
                continue;
            }
            try {
                Object o = m.invoke(obj);
                if (o != null) {
                    args.add(o);
                    StringBuilder columnName = classNameToDbName(name.substring(3));
                    fields.add(columnName.toString());
                    or.add("?");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }
        }
        String field = StringUtils.join(fields, ",");
        String sql = SQL_CACHE.get(getBeanType().getName() + ":insert:" + field);


        if (sql == null) {
            String stringBuilder = "insert into " + generateTableName() +
                    "(" +
                    StringUtils.join(fields, ",") +
                    ") values (" +
                    StringUtils.join(or, ",") +
                    ")";
            sql = stringBuilder;

            SQL_CACHE.put(getBeanType().getName() + ":insert:" + field, sql);
        }
        logger.info(sql);
        insert(obj, createId, sql, args);
    }

    /**
     * 更新对象到数据库
     *
     * @param obj 实体对象
     */
    public void update(T obj) {
        lastUpdate(obj);
        List<Object> args = new ArrayList<>();
        String sql = buildUpdateParameters(args, obj);
        getJdbcTemplate().update(sql, args.toArray());
    }

    public void batchUpdate(Collection<T> objs) {
        List<String> sqls = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        for (T obj : objs) {
            lastUpdate(obj);
            String sql = buildUpdateParameters(args, obj);
            sqls.add(sql);
        }
        getJdbcTemplate().update(StringUtils.join(sqls, ";"), args.toArray());
    }

    private String buildUpdateParameters(List<Object> args, T obj) {
        String sql = SQL_CACHE.get(getBeanType().getName() + ":update");
        Class<?> clazz = getBeanType();
        Method getters[] = clazz.getMethods();
        if (sql == null) {
            StringBuilder stringBuilder = new StringBuilder("update ");
            stringBuilder.append(generateTableName());
            stringBuilder.append(" set ");

            List<String> fields = new ArrayList<>();
            for (Method m : getters) {
                String name = m.getName();
                if (!name.startsWith("get")) {
                    continue;
                }
                if (!ACCESS_TYPES.contains(m.getReturnType())) {
                    continue;
                }
                if (haveIgnoreAnnotation(m)) {
                    continue;
                }
                StringBuilder columnName = classNameToDbName(name.substring(3));
                columnName.append("= ?");
                fields.add(columnName.toString());
            }
            String field = StringUtils.join(fields, ",");
            stringBuilder.append(field);

            stringBuilder.append(" where ");
            stringBuilder.append(PRIMARY_KEY_NAME);
            stringBuilder.append(" = ?");
            sql = stringBuilder.toString();

            SQL_CACHE.put(clazz.getName() + ":update", sql);
        }
        buildArgs(obj, args, getters);
        args.add(obj.getId());
        return sql;
    }

    /**
     * 更新对象（非空属性）到数据库
     *
     * @param obj 实体对象
     */
    public void updateNotNull(T obj) {
        lastUpdate(obj);
        List<Object> args = new ArrayList<>();
        String sql = buildUpdateNotNullSqlAndParameter(args, obj);

        logger.info(sql);
        getJdbcTemplate().update(sql, args.toArray());
    }

    /**
     * 更新对象（非空属性）到数据库
     *
     * @param objs 实体对象
     */
    public void batchUpdateNotNull(Collection<T> objs) {
        List<String> sqls = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        for (T obj : objs) {
            lastUpdate(obj);
            String sql = buildUpdateNotNullSqlAndParameter(args, obj);
            sqls.add(sql);
        }
        getJdbcTemplate().update(StringUtils.join(sqls, ";"), args.toArray());
    }

    private String buildUpdateNotNullSqlAndParameter(List<Object> args, T obj) {
        Class<?> clazz = getBeanType();
        Method getters[] = clazz.getMethods();
        List<String> fields = new ArrayList<>();
        for (Method m : getters) {
            String name = m.getName();
            if (!name.startsWith("get")) {
                continue;
            }
            if (!ACCESS_TYPES.contains(m.getReturnType())) {
                continue;
            }
            if (haveIgnoreAnnotation(m)) {
                continue;
            }
            try {
                Object o = m.invoke(obj);
                if (o != null) {
                    args.add(o);
                    StringBuilder columnName = classNameToDbName(name.substring(3));
                    fields.add(columnName.toString() + "= ? ");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new DaoException(e);
            }
        }
        String field = StringUtils.join(fields, ",");
        String sql = SQL_CACHE.get(getBeanType().getName() + ":update:" + field);


        if (sql == null) {
            sql = "update " + generateTableName() +
                    " set " +
                    field +
                    " where " +
                    PRIMARY_KEY_NAME +
                    " =  ?";

            SQL_CACHE.put(getBeanType().getName() + ":update:" + field, sql);
        }

        args.add(obj.getId());
        return sql;
    }

    /**
     * 删除对象，如果是LogicallyDeletable 则逻辑删除
     *
     * @param obj 实体对象
     */
    public void deleteObj(T obj) {
        if (obj instanceof LogicallyDeletable) {
            ((LogicallyDeletable) obj).setDeleted(true);
            updateNotNull(obj);
        } else {
            String sql = SQL_CACHE.get(getBeanType().getName() + ":delete");
            if (sql == null) {
                sql = "delete from " + getTableName() + " where " + PRIMARY_KEY_NAME + "= ?";
                SQL_CACHE.put(getBeanType().getName() + ":delete", sql);
            }
            getJdbcTemplate().update(sql, obj.getId());
        }
    }

    /**
     * 根据id删除对象
     *
     * @param id 实体对象
     */
    public void deleteById(I id) throws IllegalAccessException, InstantiationException {
        T obj = getBeanType().newInstance();
        obj.setId(id);
        deleteObj(obj);
    }

    /**
     * 根据id批量删除
     *
     * @param ids
     */
    public void batchDelete(List<I> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        for (int page = 1; ; page++) {
            List<T> result = new ArrayList<>();
            List<?> subParams = CommonUtils.pageList(ids, page, DATA_COUNT_PER_CONNECTION);
            if (CollectionUtils.isEmpty(subParams)) {
                return;
            }
            StringBuilder sql = new StringBuilder();
            if (LogicallyDeletable.class.isAssignableFrom(getBeanType())) {
                sql.append("update ").append(getTableName()).append(" set deleted = 1 where id in").append(generateInSql(ids, "?"));
            } else {
                sql.append("delete from ").append(getTableName()).append(" where id in").append(generateInSql(ids, "?"));
            }
            logger.info("batchDelete --> sql = " + sql);
            getJdbcTemplate().update(sql.toString(), ids.toArray());

            if (subParams.size() < DATA_COUNT_PER_CONNECTION) {
                return;
            }
        }
    }

    /**
     * 可排序实体位置拖动接口
     *
     * @param targetId      要拖动的实体id
     * @param destId        将拖动的实体加在destId对应实体的后面
     * @param parentRowName : 父级元素的列名
     * @param toTop         是否放在同级目录顶层
     */
    public void sortableMove(I targetId, I destId, String parentRowName, boolean toTop, Integer destParentRowId) {
        if (targetId == null || targetId.equals(destId)) {
            throw new IllegalArgumentException("参数不合法:targetId不能为空且不能相等");
        }
        if (!toTop && destId == null) {
            throw new IllegalArgumentException("非置顶情况下,目标id不能为空");
        }
        if (!Sortable.class.isAssignableFrom(getBeanType())) {
            throw new DaoException("非sortable类型");
        }
        synchronized (sortableMoveLock) {
            Sortable target = (Sortable) selectById(targetId);
            try {
                Field f = target.getClass().getDeclaredField(this.lineToHump(parentRowName));
                f.setAccessible(true);
                Integer targetParentId = (Integer) f.get(target);
                Integer targetOrder = target.getOrderIndex();
                //先将要移动对象所在的列比该对象大的下标统一减一
                String targetDecreseSql = "update " + getTableName() + " set " + SORT_ROW_NAME + " = " + SORT_ROW_NAME + " - 1  where " + parentRowName + " = ? and " + SORT_ROW_NAME + " > ? ";
                logger.info("sortableMove --> targetDecreseSql = " + targetDecreseSql);
                getJdbcTemplate().update(targetDecreseSql, targetParentId, targetOrder);
                StringBuilder destIncrementSql = new StringBuilder("update ").append(getTableName()).append(" set ").append(SORT_ROW_NAME).append(" = ").append(SORT_ROW_NAME).append(" + 1  where ").append(parentRowName).append(" = ").append(destParentRowId).append(" and id != ").append(targetId);
                if (!toTop) {
                    //如果不为置顶，获取目标对象的信息
                    Sortable dest = (Sortable) selectById(destId);
                    Integer destParentId = (Integer) f.get(dest);
                    Integer destOrder = dest.getOrderIndex();
                    /*if((targetParentId.equals(destParentId) && targetOrder == destOrder+1) || targetOrder == destOrder){
                        return;
                    }*/
                    //将比目标对象大的下标统一加一
                    String sql = destIncrementSql.append(" and ").append(SORT_ROW_NAME).append(" > ").append(destOrder).toString();
                    logger.info("sortableMove --> destIncrementSql = " + sql);
                    getJdbcTemplate().update(sql);
                    target.setOrderIndex(destOrder + 1);
                    f.set(target, destParentId);
                    this.update((T) target);
                } else {
                    if (destParentRowId == null) {
                        throw new IllegalArgumentException("置顶时，要移动到的层级id不能为空");
                    }
                    logger.info("sortableMove --> destIncrementSql = " + destIncrementSql);
                    getJdbcTemplate().update(destIncrementSql.toString());
                    target.setOrderIndex(Constants.MIN_ORDER);
                    f.set(target, destParentRowId);
                    this.update((T) target);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                logger.error("move --> ", e);
                throw new DaoException(e.getMessage(), e);
            }
        }
    }


    /**
     * 获得当前目录下元素列表的最大序列号，如某一讲次下的知识点列表的最大序列号
     *
     * @param rowName   : 可以标志父目录（主表）的字段名，如lecture_kownledge_point 表中的lecture_id
     * @param rowValue: 字段值
     * @return
     */
    protected int selectMaxOrderIndex(String rowName, Integer rowValue) {
        if (!Sortable.class.isAssignableFrom(getBeanType())) {
            throw new DaoException("非Sortable类型不能调用此方法");
        }
        StringBuilder sql = new StringBuilder("select max(order_index) from ").append(this.getTableName()).append(" where ").append(rowName).append("=").append(rowValue);
        logger.info("selectMaxOrderIndex --> sql = " + sql);
        Integer maxIndex = getJdbcTemplate().queryForObject(sql.toString(), Integer.class);
        return maxIndex == null ? 0 : maxIndex;
    }

    public List<T> listByQueryBuilder(QueryBuilder builder) {
        return null;
    }

    public T findByQueryBuilder(QueryBuilder builder) {
        return null;
    }

    private void buildArgs(T obj, List<Object> args, Method[] getters) {
        for (Method m : getters) {
            String name = m.getName();
            if (!name.startsWith("get")) {
                continue;
            }
            if (!ACCESS_TYPES.contains(m.getReturnType())) {
                continue;
            }
            if (haveIgnoreAnnotation(m)) {
                continue;
            }
            try {
                Object o = m.invoke(obj);
                args.add(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("buildArgs", e);
            }
        }
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
            ((LastUpdater) record).setLastUpdaterId(UserHolder.get(UserIdTransfer.USER_ID_KEY));
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
            ((Creator) record).setCreatorId(UserHolder.get(UserIdTransfer.USER_ID_KEY));
        }
    }

    /**
     * CreateTime Creator支持.
     *
     * @param record 实体对象
     */
    private void institutional(final T record) {
        if (record instanceof Institutional) {
            ((Institutional) record).setInstitutionId(UserHolder.get(InstitutionIdTransfer.INSTITUTION_ID_KEY));
        }
    }

    private int[] batchUpdate(final String sql,
                              final BatchPreparedStatementSetter pss, final KeyHolder generatedKeyHolder)
            throws DataAccessException {

        return (int[]) getJdbcTemplate().execute(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    }
                },
                new PreparedStatementCallback() {
                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
                        try {
                            int batchSize = pss.getBatchSize();
                            InterruptibleBatchPreparedStatementSetter ipss =
                                    (pss instanceof InterruptibleBatchPreparedStatementSetter ?
                                            (InterruptibleBatchPreparedStatementSetter) pss : null);
                            if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
                                for (int i = 0; i < batchSize; i++) {
                                    pss.setValues(ps, i);
                                    if (ipss != null && ipss.isBatchExhausted(i)) {
                                        break;
                                    }
                                    ps.addBatch();
                                }
                                int[] rowsAffected = ps.executeBatch();
                                List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
                                generatedKeys.clear();
                                ResultSet keys = ps.getGeneratedKeys();
                                if (keys != null) {
                                    try {
                                        RowMapper rowMapper = new ColumnMapRowMapper();
                                        RowMapperResultSetExtractor rse = new RowMapperResultSetExtractor(rowMapper, 1);
                                        generatedKeys.addAll(rse.extractData(keys));
                                    } finally {
                                        JdbcUtils.closeResultSet(keys);
                                    }
                                }
                                if (logger.isDebugEnabled()) {
                                    logger.debug("SQL update affected " + rowsAffected + " rows and returned " + generatedKeys.size() + " keys");
                                }
                                return rowsAffected;
                            } else {
                                List rowsAffected = new ArrayList();
                                for (int i = 0; i < batchSize; i++) {
                                    pss.setValues(ps, i);
                                    if (ipss != null && ipss.isBatchExhausted(i)) {
                                        break;
                                    }
                                    rowsAffected.add(new Integer(ps.executeUpdate()));
                                }
                                int[] rowsAffectedArray = new int[rowsAffected.size()];
                                for (int i = 0; i < rowsAffectedArray.length; i++) {
                                    rowsAffectedArray[i] = ((Integer) rowsAffected.get(i)).intValue();
                                }
                                return rowsAffectedArray;
                            }
                        } finally {
                            if (pss instanceof ParameterDisposer) {
                                ((ParameterDisposer) pss).cleanupParameters();
                            }
                        }
                    }
                });
    }
}
