package com.calm.entity.processor.conditional;

public class StartWithQueryConditional extends LikeQueryConditional {

    public StartWithQueryConditional(String field, Object... value) {
        super(field, value);
    }

    @Override
    public Object[] getArgs() {
        Object[] args = super.getArgs();
        String[] result = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = args[i] + "%";
        }
        return result;
    }
}
