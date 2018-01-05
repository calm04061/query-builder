package com.calm.entity.processor.conditional;

public class GtQueryConditional extends SimpleValueQueryConditional {
    public GtQueryConditional(String field, Object... value) {
        super(field, value);
    }

    @Override
    protected String operate() {
        return ">";
    }
}
