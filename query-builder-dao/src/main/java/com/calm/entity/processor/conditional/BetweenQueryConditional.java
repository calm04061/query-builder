package com.calm.entity.processor.conditional;

public class BetweenQueryConditional implements QueryConditional {
    private final String field;
    private final Object[] value;

    public BetweenQueryConditional(String field, Object... value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public Object[] getArgs() {
        return value;
    }

    @Override
    public String buildQuery() {
        StringBuilder sqlPart = new StringBuilder();
        sqlPart.append("`");
        sqlPart.append(field);
        sqlPart.append("` between(");
        sqlPart.append("?,?)");

        return sqlPart.toString();
    }
}
