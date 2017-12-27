package com.calm.query.builder.nati;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

//@Entity
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        "javax.persistence.Entity"
})
@AutoService(Processor.class)
public class EntityProcessor extends AbstractProcessor {

    private final Map<String, Set<TypeBuilder>> TYPE_SUPPORT = new HashMap<>();

    public EntityProcessor() {
        ServiceLoader<MethodBuilder> load = ServiceLoader.load(MethodBuilder.class, EntityProcessor.class.getClassLoader());
        for (MethodBuilder next : load) {
            Class<? extends MethodBuilder> type = next.getClass();
            Support annotation = type.getAnnotation(Support.class);
            SupportType[] value = annotation.value();
            for (SupportType supportType : value) {
                String classType = supportType.classType();
                String[] queryTypes = supportType.queryTypes();
                Set<TypeBuilder> typeBuilders = TYPE_SUPPORT.get(classType);
                if (typeBuilders == null) {
                    typeBuilders = new HashSet<>();
                    TYPE_SUPPORT.put(classType, typeBuilders);
                }
                for (String queryType : queryTypes) {
                    typeBuilders.add(new TypeBuilder(queryType, next));
                }
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement t : annotations) {
            for (Element e : roundEnv.getElementsAnnotatedWith(t)) {
                List<Element> elements = new ArrayList<>();
                TypeElement typeElement = (TypeElement) e;
                String className = typeElement.getQualifiedName().toString();
                List<? extends Element> enclosedElements = e.getEnclosedElements();
                for (Element element : enclosedElements) {

                    if (element.getKind() == ElementKind.FIELD) {
                        elements.add(element);
                    }
                }
                try {
                    writeBeanInfoFile(className, elements);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * Writes the source file for the BeanInfo class.
     *
     * @param elements a map of property names and their annotations
     */
    private void writeBeanInfoFile(String mname, List<Element> elements)
            throws IOException {
        String classFullName = mname + "Builder";

        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                classFullName);
        PrintWriter out = new PrintWriter(sourceFile.openWriter());
        int i = mname.lastIndexOf(".");
        if (i > 0) {
            out.print("package ");
            out.print(mname.substring(0, i));
            out.println(";");
        }
        String className = mname.substring(i + 1) + "Builder";
        out.print("public class ");
        out.print(className);
        out.print(" extends  com.calm.entity.processor.AbstractQueryBuilder ");
        out.println(" implements com.calm.entity.processor.QueryBuilder {");
        out.print("\tprivate ");
        out.print(className);
        out.println("(){");
        out.print("\t\tsuper(");
        out.print(mname);
        out.println(".class);");
        out.println("\t}");

        out.print("\tpublic static ");
        out.print(className);
        out.println(" createBuilder(){");
        out.print("\t\treturn new ");
        out.print(className);
        out.println("();");
        out.println("\t}");
        for (Element element : elements) {
            VariableElement temp = (VariableElement) element;
            String type = temp.asType().toString();
            Set<TypeBuilder> strings = TYPE_SUPPORT.get(type);
            if (strings == null) {
                continue;
            }
            String fieldName = element.getSimpleName().toString();
            String fieldNameUpper = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            for (TypeBuilder cond : strings) {
                cond.getMethodBuilder().buildMethod(out, type, fieldName, fieldNameUpper, cond.getName());
            }
        }
        out.println("}");
        out.close();
    }
}
