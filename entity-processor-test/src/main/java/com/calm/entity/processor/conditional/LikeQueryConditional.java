package com.calm.entity.processor.conditional;

public class LikeQueryConditional implements QueryConditional {

    private final String field;
    private final Object[] value;

    LikeQueryConditional(String field, Object... value) {
        this.field = field;
        this.value = value;
    }

    public String buildQuery() {
        StringBuilder sqlPart = new StringBuilder();
        sqlPart.append("`");
        sqlPart.append(field);
        sqlPart.append("`");
        sqlPart.append("like");
        sqlPart.append("?");
        return sqlPart.toString();
    }


    @Override
    public Object[] getArgs() {
        return value;
    }
}
