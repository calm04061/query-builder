package com.calm.entity.processor;

public class GtQueryConditional extends SimpleValueQueryConditional {
    public GtQueryConditional(String field, Object... value) {
        super(field, value);
    }

    @Override
    protected String operate() {
        return ">";
    }
}
