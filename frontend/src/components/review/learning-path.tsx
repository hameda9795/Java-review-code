"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  BookOpen,
  ExternalLink,
  GraduationCap,
  Clock,
  CheckCircle2,
  Target,
  TrendingUp,
  Trophy,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface Finding {
  severity: string;
  category: string;
}

interface LearningPathProps {
  findings: Finding[];
  overallScore: number;
}

export function LearningPath({ findings, overallScore }: LearningPathProps) {
  // Analyze findings to determine learning priorities
  const getCategoryCount = (category: string) => {
    return findings.filter((f) => f.category === category).length;
  };

  const securityIssues = getCategoryCount("SECURITY_VULNERABILITY");
  const performanceIssues = getCategoryCount("PERFORMANCE");
  const architectureIssues = getCategoryCount("ARCHITECTURE");
  const codeSmells = getCategoryCount("CODE_SMELL");

  const learningModules = [
    {
      priority: 1,
      title: "Spring Security Fundamentals",
      description:
        "Master authentication, authorization, and OWASP Top 10 vulnerabilities",
      duration: "3-5 days",
      difficulty: "Intermediate",
      relevant: securityIssues > 0,
      issuesCount: securityIssues,
      resources: [
        {
          title: "Spring Security in Action",
          type: "Book",
          url: "https://www.manning.com/books/spring-security-in-action",
        },
        {
          title: "OWASP Top 10 Guide",
          type: "Documentation",
          url: "https://owasp.org/www-project-top-ten/",
        },
        {
          title: "Spring Security Course",
          type: "Video",
          url: "https://spring.io/guides/topicals/spring-security-architecture/",
        },
      ],
      skills: [
        "@PreAuthorize annotations",
        "Input validation with @Valid",
        "JWT token security",
        "SQL injection prevention",
      ],
    },
    {
      priority: 2,
      title: "Performance Optimization & JPA",
      description: "Learn to write efficient database queries and avoid N+1 problems",
      duration: "2-4 days",
      difficulty: "Intermediate",
      relevant: performanceIssues > 0,
      issuesCount: performanceIssues,
      resources: [
        {
          title: "High-Performance Java Persistence",
          type: "Book",
          url: "https://vladmihalcea.com/books/high-performance-java-persistence/",
        },
        {
          title: "Spring Data JPA Best Practices",
          type: "Article",
          url: "https://www.baeldung.com/jpa-hibernate-projections",
        },
      ],
      skills: [
        "@EntityGraph for eager loading",
        "Query optimization",
        "Connection pooling",
        "Caching strategies",
      ],
    },
    {
      priority: 3,
      title: "Clean Architecture & DDD",
      description: "Master hexagonal architecture and domain-driven design patterns",
      duration: "5-7 days",
      difficulty: "Advanced",
      relevant: architectureIssues > 0 || overallScore < 70,
      issuesCount: architectureIssues,
      resources: [
        {
          title: "Clean Architecture by Robert Martin",
          type: "Book",
          url: "https://www.amazon.com/Clean-Architecture-Craftsmans-Software-Structure/dp/0134494164",
        },
        {
          title: "Domain-Driven Design Distilled",
          type: "Book",
          url: "https://www.amazon.com/Domain-Driven-Design-Distilled-Vaughn-Vernon/dp/0134434420",
        },
      ],
      skills: [
        "Hexagonal architecture",
        "Rich domain models",
        "SOLID principles",
        "Dependency injection",
      ],
    },
    {
      priority: 4,
      title: "Spring Boot Best Practices",
      description: "Learn professional Spring Boot patterns and anti-patterns",
      duration: "2-3 days",
      difficulty: "Beginner",
      relevant: codeSmells > 2 || overallScore < 75,
      issuesCount: codeSmells,
      resources: [
        {
          title: "Spring Boot Reference Documentation",
          type: "Documentation",
          url: "https://docs.spring.io/spring-boot/docs/current/reference/html/",
        },
        {
          title: "Effective Spring Boot",
          type: "Course",
          url: "https://www.baeldung.com/spring-boot",
        },
      ],
      skills: [
        "@RestControllerAdvice for errors",
        "DTO pattern implementation",
        "Proper logging with SLF4J",
        "Configuration properties",
      ],
    },
  ];

  // Sort by relevance and priority
  const sortedModules = learningModules
    .filter((m) => m.relevant)
    .sort((a, b) => {
      if (a.relevant && !b.relevant) return -1;
      if (!a.relevant && b.relevant) return 1;
      return a.priority - b.priority;
    });

  const totalEstimatedDays = sortedModules.reduce((sum, module) => {
    const days = parseInt(module.duration.split("-")[0]);
    return sum + days;
  }, 0);

  const getDifficultyColor = (difficulty: string) => {
    if (difficulty === "Beginner") return "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200";
    if (difficulty === "Intermediate") return "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200";
    return "bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200";
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/20">
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-full bg-primary/10">
              <GraduationCap className="h-6 w-6 text-primary" />
            </div>
            <div className="flex-1">
              <CardTitle className="text-2xl">Your Personalized Learning Path</CardTitle>
              <CardDescription className="text-base">
                Based on your code review, here's what to learn next to reach hire-worthy level
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-3 gap-4">
            <div className="flex items-center gap-3 p-3 bg-white dark:bg-gray-900 rounded-lg">
              <Target className="h-5 w-5 text-primary" />
              <div>
                <p className="text-sm text-muted-foreground">Learning Modules</p>
                <p className="text-xl font-bold">{sortedModules.length}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 p-3 bg-white dark:bg-gray-900 rounded-lg">
              <Clock className="h-5 w-5 text-primary" />
              <div>
                <p className="text-sm text-muted-foreground">Estimated Time</p>
                <p className="text-xl font-bold">{totalEstimatedDays}+ days</p>
              </div>
            </div>
            <div className="flex items-center gap-3 p-3 bg-white dark:bg-gray-900 rounded-lg">
              <TrendingUp className="h-5 w-5 text-primary" />
              <div>
                <p className="text-sm text-muted-foreground">Score Improvement</p>
                <p className="text-xl font-bold">+{Math.min(30, 85 - overallScore)}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Learning Modules */}
      {sortedModules.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center">
            <CheckCircle2 className="h-12 w-12 text-green-500 mx-auto mb-4" />
            <h3 className="text-lg font-semibold mb-2">Excellent Work!</h3>
            <p className="text-muted-foreground">
              Your code quality is already at a professional level. Keep up the great work!
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {sortedModules.map((module, index) => (
            <Card
              key={index}
              className={cn(
                "transition-all hover:shadow-lg",
                index === 0 && "border-2 border-primary/50"
              )}
            >
              <CardHeader>
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 space-y-2">
                    <div className="flex items-center gap-3">
                      <div className="h-8 w-8 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-bold">
                        {index + 1}
                      </div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <CardTitle className="text-xl">{module.title}</CardTitle>
                        {index === 0 && (
                          <Badge className="bg-primary">Recommended First</Badge>
                        )}
                      </div>
                    </div>
                    <CardDescription className="text-base ml-11">
                      {module.description}
                    </CardDescription>
                    <div className="flex items-center gap-3 ml-11 flex-wrap">
                      <Badge variant="outline" className={getDifficultyColor(module.difficulty)}>
                        {module.difficulty}
                      </Badge>
                      <Badge variant="outline">
                        <Clock className="h-3 w-3 mr-1" />
                        {module.duration}
                      </Badge>
                      {module.issuesCount > 0 && (
                        <Badge variant="outline" className="text-red-600 border-red-600">
                          Fixes {module.issuesCount} issues
                        </Badge>
                      )}
                    </div>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4 ml-11">
                {/* Skills to Learn */}
                <div className="space-y-2">
                  <h4 className="text-sm font-semibold flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-primary" />
                    Skills You'll Learn
                  </h4>
                  <div className="flex flex-wrap gap-2">
                    {module.skills.map((skill, idx) => (
                      <Badge key={idx} variant="secondary">
                        {skill}
                      </Badge>
                    ))}
                  </div>
                </div>

                {/* Learning Resources */}
                <div className="space-y-2">
                  <h4 className="text-sm font-semibold flex items-center gap-2">
                    <BookOpen className="h-4 w-4 text-primary" />
                    Learning Resources
                  </h4>
                  <div className="grid md:grid-cols-2 gap-3">
                    {module.resources.map((resource, idx) => (
                      <a
                        key={idx}
                        href={resource.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center justify-between gap-2 p-3 border rounded-lg hover:bg-muted/50 transition-colors group"
                      >
                        <div className="flex-1">
                          <p className="text-sm font-medium group-hover:text-primary transition-colors">
                            {resource.title}
                          </p>
                          <p className="text-xs text-muted-foreground">{resource.type}</p>
                        </div>
                        <ExternalLink className="h-4 w-4 text-muted-foreground group-hover:text-primary transition-colors" />
                      </a>
                    ))}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Expected Outcome */}
      {sortedModules.length > 0 && (
        <Card className="border-green-200 dark:border-green-900 bg-green-50/50 dark:bg-green-950/20">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-green-700 dark:text-green-400">
              <Trophy className="h-5 w-5" />
              Expected Outcome
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-start gap-3">
              <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Code Quality Score</p>
                <p className="text-sm text-muted-foreground">
                  Improve from {overallScore}/100 to {Math.min(85, overallScore + 30)}/100
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Hiring Level</p>
                <p className="text-sm text-muted-foreground">
                  {overallScore < 75
                    ? "Reach professional mid-level standards"
                    : "Achieve senior-level code craftsmanship"}
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <CheckCircle2 className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Interview Confidence</p>
                <p className="text-sm text-muted-foreground">
                  Feel confident showcasing your GitHub portfolio to employers
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
