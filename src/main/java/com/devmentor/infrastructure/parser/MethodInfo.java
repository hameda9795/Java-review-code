package com.devmentor.infrastructure.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MethodInfo {
    private String name;
    private String returnType;
    private boolean isPublic;
    private boolean isPrivate;
    private boolean isStatic;
    private int parameterCount;
    private int complexity;
    private List<String> annotations = new ArrayList<>();

    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }
}
