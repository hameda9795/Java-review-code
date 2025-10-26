package com.devmentor.infrastructure.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for parsing and analyzing Java code using JavaParser
 */
@Service
@Slf4j
public class JavaParserService {

    private final JavaParser javaParser = new JavaParser();

    /**
     * Parse Java source code
     */
    public Optional<CompilationUnit> parse(String code) {
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(code);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                return result.getResult();
            } else {
                log.warn("Failed to parse Java code: {}", result.getProblems());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error parsing Java code", e);
            return Optional.empty();
        }
    }

    /**
     * Analyze code and extract metadata
     */
    public CodeAnalysis analyze(String code) {
        Optional<CompilationUnit> cuOpt = parse(code);
        if (cuOpt.isEmpty()) {
            return CodeAnalysis.empty();
        }

        CompilationUnit cu = cuOpt.get();
        CodeAnalysis analysis = new CodeAnalysis();

        // Extract classes
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setName(cls.getNameAsString());
            classInfo.setInterface(cls.isInterface());
            classInfo.setAbstract(cls.isAbstract());

            // Extract annotations
            cls.getAnnotations().forEach(ann ->
                classInfo.addAnnotation(ann.getNameAsString())
            );

            // Extract methods
            cls.getMethods().forEach(method -> {
                MethodInfo methodInfo = extractMethodInfo(method);
                classInfo.addMethod(methodInfo);
            });

            // Extract fields
            cls.getFields().forEach(field -> {
                FieldInfo fieldInfo = extractFieldInfo(field);
                classInfo.addField(fieldInfo);
            });

            analysis.addClass(classInfo);
        });

        // Detect Spring Boot patterns
        analysis.setSpringBootPatterns(detectSpringBootPatterns(cu));

        // Calculate complexity
        analysis.setComplexityScore(calculateComplexity(cu));

        return analysis;
    }

    /**
     * Extract method information
     */
    private MethodInfo extractMethodInfo(MethodDeclaration method) {
        MethodInfo info = new MethodInfo();
        info.setName(method.getNameAsString());
        info.setPublic(method.isPublic());
        info.setPrivate(method.isPrivate());
        info.setStatic(method.isStatic());
        info.setReturnType(method.getType().asString());
        info.setParameterCount(method.getParameters().size());

        // Extract annotations
        method.getAnnotations().forEach(ann ->
            info.addAnnotation(ann.getNameAsString())
        );

        // Calculate method complexity
        int complexity = calculateMethodComplexity(method);
        info.setComplexity(complexity);

        return info;
    }

    /**
     * Extract field information
     */
    private FieldInfo extractFieldInfo(FieldDeclaration field) {
        FieldInfo info = new FieldInfo();
        field.getVariables().forEach(var -> {
            info.setName(var.getNameAsString());
            info.setType(var.getType().asString());
        });

        info.setPublic(field.isPublic());
        info.setPrivate(field.isPrivate());
        info.setStatic(field.isStatic());
        info.setFinal(field.isFinal());

        // Extract annotations
        field.getAnnotations().forEach(ann ->
            info.addAnnotation(ann.getNameAsString())
        );

        return info;
    }

    /**
     * Detect Spring Boot specific patterns
     */
    private List<String> detectSpringBootPatterns(CompilationUnit cu) {
        List<String> patterns = new ArrayList<>();

        // Check for Spring annotations
        Set<String> annotations = new HashSet<>();
        cu.findAll(AnnotationExpr.class).forEach(ann ->
            annotations.add(ann.getNameAsString())
        );

        if (annotations.contains("RestController") || annotations.contains("Controller")) {
            patterns.add("REST_CONTROLLER");
        }
        if (annotations.contains("Service")) {
            patterns.add("SERVICE");
        }
        if (annotations.contains("Repository")) {
            patterns.add("REPOSITORY");
        }
        if (annotations.contains("Component")) {
            patterns.add("COMPONENT");
        }
        if (annotations.contains("Configuration")) {
            patterns.add("CONFIGURATION");
        }
        if (annotations.contains("Entity")) {
            patterns.add("JPA_ENTITY");
        }
        if (annotations.contains("Autowired")) {
            patterns.add("FIELD_INJECTION"); // Anti-pattern
        }

        return patterns;
    }

    /**
     * Calculate cyclomatic complexity
     */
    private int calculateComplexity(CompilationUnit cu) {
        int complexity = 1; // Base complexity

        // Count decision points
        complexity += cu.findAll(com.github.javaparser.ast.stmt.IfStmt.class).size();
        complexity += cu.findAll(com.github.javaparser.ast.stmt.ForStmt.class).size();
        complexity += cu.findAll(com.github.javaparser.ast.stmt.WhileStmt.class).size();
        complexity += cu.findAll(com.github.javaparser.ast.stmt.DoStmt.class).size();
        complexity += cu.findAll(com.github.javaparser.ast.stmt.SwitchEntry.class).size();
        complexity += cu.findAll(com.github.javaparser.ast.expr.ConditionalExpr.class).size();

        return complexity;
    }

    /**
     * Calculate method complexity
     */
    private int calculateMethodComplexity(MethodDeclaration method) {
        int complexity = 1;

        complexity += method.findAll(com.github.javaparser.ast.stmt.IfStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.ForStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.WhileStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.DoStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.SwitchEntry.class).size();
        complexity += method.findAll(com.github.javaparser.ast.expr.ConditionalExpr.class).size();
        complexity += method.findAll(com.github.javaparser.ast.expr.BinaryExpr.class)
                .stream()
                .filter(expr -> expr.getOperator().name().contains("AND") ||
                               expr.getOperator().name().contains("OR"))
                .count();

        return complexity;
    }

    /**
     * Check if code has specific issues with detailed analysis
     */
    public List<String> detectIssues(String code) {
        List<String> issues = new ArrayList<>();
        Optional<CompilationUnit> cuOpt = parse(code);

        if (cuOpt.isEmpty()) {
            issues.add("Unable to parse Java code");
            return issues;
        }

        CompilationUnit cu = cuOpt.get();

        // Check for field injection (Spring anti-pattern)
        cu.findAll(FieldDeclaration.class).forEach(field -> {
            if (field.getAnnotations().stream()
                    .anyMatch(ann -> ann.getNameAsString().equals("Autowired"))) {
                issues.add("Field injection detected - use constructor injection instead");
            }
        });

        // Check for methods that are too complex
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            int complexity = calculateMethodComplexity(method);
            if (complexity > 10) {
                issues.add(String.format("Method '%s' has high complexity (%d) - consider refactoring",
                        method.getNameAsString(), complexity));
            }
        });

        // Check for empty catch blocks
        cu.findAll(com.github.javaparser.ast.stmt.CatchClause.class).forEach(catchClause -> {
            if (catchClause.getBody().getStatements().isEmpty()) {
                issues.add("Empty catch block detected - add proper error handling");
            }
        });

        return issues;
    }

    /**
     * Calculate advanced code quality metrics
     */
    public Map<String, Object> calculateMetrics(String code) {
        Map<String, Object> metrics = new HashMap<>();
        Optional<CompilationUnit> cuOpt = parse(code);

        if (cuOpt.isEmpty()) {
            return metrics;
        }

        CompilationUnit cu = cuOpt.get();

        // Lines of code
        metrics.put("totalLines", code.split("\n").length);
        metrics.put("classes", cu.findAll(ClassOrInterfaceDeclaration.class).size());
        metrics.put("methods", cu.findAll(MethodDeclaration.class).size());
        metrics.put("fields", cu.findAll(FieldDeclaration.class).size());

        // Complexity metrics
        metrics.put("cyclomaticComplexity", calculateComplexity(cu));
        
        // Method length metrics
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
        if (!methods.isEmpty()) {
            int totalMethodLines = methods.stream()
                    .mapToInt(m -> m.getEnd().get().line - m.getBegin().get().line)
                    .sum();
            metrics.put("avgMethodLength", totalMethodLines / methods.size());
            metrics.put("maxMethodLength", methods.stream()
                    .mapToInt(m -> m.getEnd().get().line - m.getBegin().get().line)
                    .max().orElse(0));
        }

        // Coupling metrics (imports count)
        metrics.put("imports", cu.getImports().size());

        // Cohesion hint: ratio of fields to methods
        int fieldCount = cu.findAll(FieldDeclaration.class).size();
        int methodCount = cu.findAll(MethodDeclaration.class).size();
        if (methodCount > 0) {
            metrics.put("fieldToMethodRatio", (double) fieldCount / methodCount);
        }

        // Spring Boot specific
        long springAnnotations = cu.findAll(AnnotationExpr.class).stream()
                .filter(ann -> ann.getNameAsString().startsWith("Spring") || 
                              ann.getNameAsString().equals("Autowired") ||
                              ann.getNameAsString().equals("Service") ||
                              ann.getNameAsString().equals("Repository"))
                .count();
        metrics.put("springAnnotations", springAnnotations);

        // Code smells
        List<String> smells = new ArrayList<>();
        
        // God class detection (too many methods/fields)
        if (methodCount > 20) {
            smells.add("GOD_CLASS - Too many methods (" + methodCount + ")");
        }
        if (fieldCount > 10) {
            smells.add("TOO_MANY_FIELDS - Consider splitting class (" + fieldCount + ")");
        }

        // Long parameter list
        methods.forEach(m -> {
            if (m.getParameters().size() > 5) {
                smells.add("LONG_PARAMETER_LIST in " + m.getNameAsString() + 
                          " (" + m.getParameters().size() + " params) - use Builder or Parameter Object");
            }
        });

        // Feature envy detection (simple heuristic)
        methods.forEach(m -> {
            long methodCalls = m.findAll(com.github.javaparser.ast.expr.MethodCallExpr.class).size();
            if (methodCalls > 10) {
                smells.add("POSSIBLE_FEATURE_ENVY in " + m.getNameAsString() + 
                          " - too many external method calls");
            }
        });

        metrics.put("codeSmells", smells);

        return metrics;
    }

    /**
     * Detect SOLID principle violations
     */
    public List<String> detectSOLIDViolations(String code) {
        List<String> violations = new ArrayList<>();
        Optional<CompilationUnit> cuOpt = parse(code);

        if (cuOpt.isEmpty()) {
            return violations;
        }

        CompilationUnit cu = cuOpt.get();

        // Single Responsibility: Check method count as proxy
        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        classes.forEach(cls -> {
            int methodCount = cls.getMethods().size();
            if (methodCount > 15) {
                violations.add("SRP_VIOLATION in " + cls.getNameAsString() + 
                              " - Class has too many methods (" + methodCount + "), likely multiple responsibilities");
            }
        });

        // Liskov Substitution: Check for instanceof in methods
        cu.findAll(com.github.javaparser.ast.expr.InstanceOfExpr.class).forEach(instanceOf -> {
            violations.add("POSSIBLE_LSP_VIOLATION - instanceof check suggests type checking instead of polymorphism");
        });

        // Interface Segregation: Check interface size
        classes.stream().filter(ClassOrInterfaceDeclaration::isInterface).forEach(iface -> {
            int methodCount = iface.getMethods().size();
            if (methodCount > 10) {
                violations.add("ISP_VIOLATION in interface " + iface.getNameAsString() + 
                              " - Too many methods (" + methodCount + "), clients forced to implement unused methods");
            }
        });

        // Dependency Inversion: Check for 'new' keyword in non-constructor methods
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            // Check if method is a constructor by checking if name matches class name
            boolean isConstructor = method.getNameAsString().equals(
                method.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(ClassOrInterfaceDeclaration::getNameAsString)
                    .orElse("")
            );
            
            if (!isConstructor) {
                long newExpressions = method.findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class)
                        .stream()
                        .filter(expr -> !expr.getTypeAsString().contains("ArrayList") &&
                                       !expr.getTypeAsString().contains("HashMap") &&
                                       !expr.getTypeAsString().contains("HashSet") &&
                                       !expr.getTypeAsString().contains("Exception"))
                        .count();
                
                if (newExpressions > 0) {
                    violations.add("POSSIBLE_DIP_VIOLATION in " + method.getNameAsString() + 
                                  " - Using 'new' for dependencies instead of injection");
                }
            }
        });

        return violations;
    }

    /**
     * Detect missing design patterns opportunities
     */
    public List<String> suggestDesignPatterns(String code) {
        List<String> suggestions = new ArrayList<>();
        Optional<CompilationUnit> cuOpt = parse(code);

        if (cuOpt.isEmpty()) {
            return suggestions;
        }

        CompilationUnit cu = cuOpt.get();

        // Builder pattern suggestion
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            // Check if method is a constructor by checking if name matches class name
            boolean isConstructor = method.getNameAsString().equals(
                method.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(ClassOrInterfaceDeclaration::getNameAsString)
                    .orElse("")
            );
            
            if (isConstructor && method.getParameters().size() >= 4) {
                suggestions.add("BUILDER_PATTERN - Constructor in " + method.getNameAsString() + 
                              " has " + method.getParameters().size() + " parameters, consider Builder pattern");
            }
        });

        // Strategy pattern suggestion (many if-else)
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            long ifCount = method.findAll(com.github.javaparser.ast.stmt.IfStmt.class).size();
            if (ifCount > 5) {
                suggestions.add("STRATEGY_PATTERN - Method " + method.getNameAsString() + 
                              " has many if-else branches (" + ifCount + "), consider Strategy pattern");
            }
        });

        // Factory pattern suggestion (many object creations)
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            long newCount = method.findAll(com.github.javaparser.ast.expr.ObjectCreationExpr.class).size();
            if (newCount > 3) {
                suggestions.add("FACTORY_PATTERN - Method " + method.getNameAsString() + 
                              " creates many objects, consider Factory pattern");
            }
        });

        // Template Method pattern (multiple similar methods)
        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        classes.forEach(cls -> {
            List<String> methodNames = cls.getMethods().stream()
                    .map(m -> m.getNameAsString())
                    .toList();
            
            // Simple heuristic: methods with similar prefixes
            long processCount = methodNames.stream().filter(n -> n.startsWith("process")).count();
            long handleCount = methodNames.stream().filter(n -> n.startsWith("handle")).count();
            
            if (processCount > 3 || handleCount > 3) {
                suggestions.add("TEMPLATE_METHOD_PATTERN - Multiple similar methods in " + cls.getNameAsString() + 
                              ", consider Template Method pattern to reduce duplication");
            }
        });

        return suggestions;
    }
}

