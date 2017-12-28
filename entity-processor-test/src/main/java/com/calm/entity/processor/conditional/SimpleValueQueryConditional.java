package com.calm.entity.processor.conditional;

public abstract class SimpleValueQueryConditional implements QueryConditional {
    private final String field;
    private final Object[] value;

    SimpleValueQueryConditional(String field, Object... value) {
        this.field = field;
        this.value = value;
    }

    public String buildQuery() {
        StringBuilder sqlPart = new StringBuilder();
        sqlPart.append("`");
        sqlPart.append(field);
        sqlPart.append("`");
        sqlPart.append(operate());
        sqlPart.append("?");
        return sqlPart.toString();
    }

    protected abstract String operate();

    @Override
    public Object[] getArgs() {
        return value;
    }

}
