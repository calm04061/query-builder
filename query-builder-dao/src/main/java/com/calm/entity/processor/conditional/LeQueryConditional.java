package com.calm.entity.processor.conditional;

public class LeQueryConditional extends SimpleValueQueryConditional {
    public LeQueryConditional(String field, Object... value) {
        super(field, value);
    }

    @Override
    protected String operate() {
        return "<=";
    }
}
