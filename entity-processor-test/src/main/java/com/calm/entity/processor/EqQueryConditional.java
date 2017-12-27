package com.calm.entity.processor;

public class EqQueryConditional  extends SimpleValueQueryConditional {
    public EqQueryConditional(String field, Object... value) {
        super(field, value);
    }

    @Override
    protected String operate() {
        return "=";
    }
}
