package com.calm.entity.processor;

public interface QueryBuilder {
    String buildQuery();
    Object[] buildArgs();
}
