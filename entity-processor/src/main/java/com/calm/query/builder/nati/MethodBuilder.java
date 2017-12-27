package com.calm.query.builder.nati;

import java.io.PrintWriter;

public interface MethodBuilder {
    void buildMethod(PrintWriter out, String type, String fieldName, String fieldNameUpper, String name);
}
