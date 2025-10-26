package com.devmentor.infrastructure.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassInfo {
    private String name;
    private boolean isInterface;
    private boolean isAbstract;
    private List<String> annotations = new ArrayList<>();
    private List<MethodInfo> methods = new ArrayList<>();
    private List<FieldInfo> fields = new ArrayList<>();

    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }

    public void addMethod(MethodInfo method) {
        this.methods.add(method);
    }

    public void addField(FieldInfo field) {
        this.fields.add(field);
    }
}
