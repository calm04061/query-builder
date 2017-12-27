package com.calm.entity.processor;

public class LtQueryConditional extends SimpleValueQueryConditional {
    public LtQueryConditional(String field, Object... value) {
        super(field, value);
    }

    @Override
    protected String operate() {
        return "<";
    }
}
