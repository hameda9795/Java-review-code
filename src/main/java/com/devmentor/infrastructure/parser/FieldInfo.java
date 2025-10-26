package com.devmentor.infrastructure.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FieldInfo {
    private String name;
    private String type;
    private boolean isPublic;
    private boolean isPrivate;
    private boolean isStatic;
    private boolean isFinal;
    private List<String> annotations = new ArrayList<>();

    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }
}
