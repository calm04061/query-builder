package com.calm.query.builder.nati.builder;

import com.calm.query.builder.nati.MethodBuilder;
import com.calm.query.builder.nati.Support;
import com.calm.query.builder.nati.SupportType;
import com.google.auto.service.AutoService;

import java.io.PrintWriter;
import java.util.Date;

@Support({
        @SupportType(classType = Integer.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = int.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = Long.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = long.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = Short.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = short.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = Double.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = double.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = Float.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = float.class, queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = String.class, queryTypes = {"Eq", "StartWith", "EndWith"}),
        @SupportType(classType = Date.class, queryTypes = {"Eq"}),
        @SupportType(classType = Date.class, queryTypes = {"Eq"}),
        @SupportType(classType = java.sql.Date.class, queryTypes = {"Eq"})
})
@AutoService(MethodBuilder.class)
public class SimpleMethodBuilder implements MethodBuilder {

    @Override
    public void buildMethod(PrintWriter out, String type, String fieldName, String fieldNameUpper, String name) {
        buildMethod(out, type, fieldName, fieldNameUpper, name, "and");
        buildMethod(out, type, fieldName, fieldNameUpper, name, "or");
    }

    private void buildMethod(PrintWriter out, String fieldType, String fieldName, String fieldNameUpper, String cond, String methodType) {
        out.print("\tpublic UserBuilder ");
        out.print(methodType);
        out.print(fieldNameUpper);
        out.print(cond);
        out.print("(");
        out.print(fieldType);
        out.print(" ");
        out.print(fieldName);
        out.println("){");
        out.print("\t\tif(");
        out.print(fieldName);
        out.println("!= null) {");
        out.print("\t\t\tand(new com.calm.entity.processor.conditional.");
        out.print(cond);
        out.print("QueryConditional(\"");
        out.print(fieldName);
        out.print("\",");
        out.print(fieldName);
        out.println("));");
        out.println("\t\t}");
        out.println("\t\treturn this;");
        out.println("\t}");
    }
}
