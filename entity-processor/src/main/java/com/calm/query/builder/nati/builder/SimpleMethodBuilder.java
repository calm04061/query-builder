package com.calm.query.builder.nati.builder;

import com.calm.query.builder.nati.MethodBuilder;
import com.calm.query.builder.nati.Support;
import com.calm.query.builder.nati.SupportType;

import java.io.PrintWriter;
import java.text.MessageFormat;

@Support({
        @SupportType(classType = "java.lang.Integer", queryTypes = {"Eq", "Lt", "Gt", "Ge", "Le"}),
        @SupportType(classType = "java.lang.String", queryTypes = {"Eq", "StartWith", "EndWith"}),
        @SupportType(classType = "java.util.Date", queryTypes = {"Eq"}),
})
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
        out.print("\t\t\tand(new ");
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
