package com.calm.entity.processor.dao;

import com.calm.entity.processor.Constants;
import com.calm.entity.processor.entity.BaseEntity;
import com.calm.entity.processor.exception.DaoException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dingqihui on 2017/6/20.
 */
public class AbstractDao<I, T extends BaseEntity<I>> {
    protected final Class<T> beanType;
    protected final Class<I> keyType;
    private final String TABLE_NAME;

    protected final String SELECT_SENTENCE;
    protected final String PRIMARY_KEY_NAME;//= "id";
    private static Pattern linePattern = Pattern.compile("_(\\w)");

    protected AbstractDao() {
        this.beanType = ((Class<T>) (((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[1]));
        this.keyType = ((Class<I>) (((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[0]));
        this.TABLE_NAME = generateTableName();
        this.PRIMARY_KEY_NAME = "id";
        // TODO
        SELECT_SENTENCE = "select * from " + TABLE_NAME;
    }

    /**
     * @param tableName:如果非空则以此为表名，否则默认
     * @param primaryKeyName:如果非空则以此为主键列名，否则默认
     */
    protected AbstractDao(String tableName, String primaryKeyName) {
        this.beanType = ((Class<T>) (((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[1]));
        this.keyType = ((Class<I>) (((ParameterizedType) (this.getClass().getGenericSuperclass())).getActualTypeArguments()[0]));
        if (StringUtils.isNotBlank(tableName)) {
            this.TABLE_NAME = tableName;
        } else {
            this.TABLE_NAME = generateTableName();
        }
        if (StringUtils.isNotBlank(primaryKeyName)) {
            this.PRIMARY_KEY_NAME = primaryKeyName;
        } else {
            this.PRIMARY_KEY_NAME = "id";
        }
        SELECT_SENTENCE = "select * from " + tableName;
    }

    protected String generateTableName() {
        Table tableAnnotation = beanType.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            String tableName = tableAnnotation.name();
            if (StringUtils.isNotBlank(tableName)) {
                return tableName;
            }
        }

        String className = beanType.getSimpleName();
        return classNameToDbName(className).toString();
    }

    /**
     * java转下划线分割
     *
     * @param className
     * @return
     */
    protected StringBuilder classNameToDbName(String className) {
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(className.charAt(0)));
        if (className.length() >= 1) {
            for (int i = 1; i < className.length(); i++) {
                char c = className.charAt(i);
                if (Character.isUpperCase(className.charAt(i))) {
                    result.append("_");
                    result.append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            }
        }
        return result;
    }

    public Class<T> getBeanType() {
        return beanType;
    }

    protected String getTableName() {
        return this.TABLE_NAME;
    }

    /**
     * 将查询条件sql并入
     *
     * @param sql
     * @param conditions
     */
    protected void combineConditionSql(StringBuilder sql, List<String> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            return;
        }
        if (!sql.toString().contains("where")) {
            sql.append(" where ");
        }
        for (int i = 0; i < conditions.size(); i++) {
            if (i == conditions.size() - 1) {
                sql.append(conditions.get(i));
            } else {
                sql.append(conditions.get(i)).append(" and ");
            }
        }
    }

    protected boolean haveIgnoreAnnotation(Method m) {
        Transient aTransient = m.getAnnotation(Transient.class);
        return aTransient != null;
    }

    protected I convertToKeyType(Number value) {
        if (keyType.equals(Integer.class)) {
            return keyType.cast(value.intValue());
        } else if (keyType.equals(Long.class)) {
            return keyType.cast(value.longValue());
        }
        return null;
    }

    /**
     * 生成 in 条件查询所需语句
     *
     * @param list
     * @param placeHolder：如果不为空，则以此为占位符，不填充实际值
     * @return
     */
    protected String generateInSql(List<?> list, String placeHolder) {
        if (CollectionUtils.isEmpty(list)) {
            throw new DaoException(Constants.PARAM_CANNOT_EMPTY);
        }
        StringBuilder sql = new StringBuilder("(");
        if (StringUtils.isNotBlank(placeHolder)) {
            for (int i = 0; i < list.size(); i++) {
                sql.append(placeHolder).append(",");
            }
        } else {
            for (Object obj : list) {
                sql.append(obj).append(",");
            }
        }
        sql.setLength(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }

    /**
     * 下划线转驼峰，mysql字段 to java属性
     *
     * @param str
     * @return
     */
    protected String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
