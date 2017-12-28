package com.calm.entity.processor.conditional;

public class GeQueryConditional extends SimpleValueQueryConditional {
    public GeQueryConditional(String field, Object... value) {
        super(field, value);
    }

    @Override
    protected String operate() {
        return ">=";
    }
}
