package com.calm.query.builder.nati.builder;

import com.calm.query.builder.nati.MethodBuilder;
import com.calm.query.builder.nati.Support;
import com.calm.query.builder.nati.SupportType;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import java.io.PrintWriter;

@Support({
        @SupportType(classType = "java.lang.Integer", queryTypes = {"Between"}),
        @SupportType(classType = "java.lang.Long", queryTypes = {"Between"}),
        @SupportType(classType = "java.util.Date", queryTypes = {"Between"}),
})
@AutoService(MethodBuilder.class)
public class BetweenMethodBuilder implements MethodBuilder {

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
        out.print(" start,");
        out.print(fieldType);
        out.print(" end");
        out.println("){");
        out.print("\t\tif(start");
        out.println("!= null) {");
        out.print("\t\t\tand(new com.calm.entity.processor.");
        out.print(cond);
        out.print("QueryConditional(\"");
        out.print(fieldName);
        out.print("\",");
        out.print("start, end");
        out.println("));");
        out.println("\t\t}");
        out.println("\t\treturn this;");
        out.println("\t}");
    }
}
