package com.calm.query.builder.nati;

import java.util.Objects;

public class TypeBuilder {
    private String name;
    private MethodBuilder methodBuilder;

    public TypeBuilder(String name, MethodBuilder methodBuilder) {
        this.name = name;
        this.methodBuilder = methodBuilder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MethodBuilder getMethodBuilder() {
        return methodBuilder;
    }

    public void setMethodBuilder(MethodBuilder methodBuilder) {
        this.methodBuilder = methodBuilder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeBuilder that = (TypeBuilder) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(methodBuilder, that.methodBuilder);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, methodBuilder);
    }
}
