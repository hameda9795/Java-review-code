package com.devmentor.infrastructure.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeAnalysis {
    private List<ClassInfo> classes = new ArrayList<>();
    private List<String> springBootPatterns = new ArrayList<>();
    private int complexityScore = 0;

    public void addClass(ClassInfo classInfo) {
        this.classes.add(classInfo);
    }

    public static CodeAnalysis empty() {
        return new CodeAnalysis();
    }
}
