"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ProgressCircle } from "@/components/ui/progress-circle";
import { Badge } from "@/components/ui/badge";
import {
  Shield,
  Zap,
  Code2,
  BookCheck,
  Trophy,
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface QualityScore {
  overallScore: number;
  grade: string;
  securityScore: number;
  performanceScore: number;
  maintainabilityScore: number;
  bestPracticesScore: number;
  testCoverageScore?: number;
}

interface QualityScoreDashboardProps {
  score: QualityScore;
  findings?: {
    total: number;
    critical: number;
    high: number;
    medium: number;
    low: number;
  };
}

export function QualityScoreDashboard({ score, findings }: QualityScoreDashboardProps) {
  const getGradeColor = (grade: string) => {
    if (grade.startsWith("A")) return "text-green-500";
    if (grade.startsWith("B")) return "text-blue-500";
    if (grade.startsWith("C")) return "text-yellow-500";
    if (grade.startsWith("D")) return "text-orange-500";
    return "text-red-500";
  };

  const getHireabilityStatus = () => {
    if (score.overallScore >= 80)
      return {
        status: "Interview Ready",
        color: "bg-green-500",
        icon: CheckCircle2,
        description: "Your code meets professional hiring standards",
      };
    if (score.overallScore >= 60)
      return {
        status: "Needs Improvement",
        color: "bg-yellow-500",
        icon: AlertTriangle,
        description: "Address critical issues before showcasing to employers",
      };
    return {
      status: "Not Hire-Ready",
      color: "bg-red-500",
      icon: AlertTriangle,
      description: "Significant improvements needed for employment consideration",
    };
  };

  const hireability = getHireabilityStatus();
  const StatusIcon = hireability.icon;

  const metrics = [
    {
      title: "Security",
      score: score.securityScore,
      icon: Shield,
      description: "Vulnerability & OWASP compliance",
    },
    {
      title: "Performance",
      score: score.performanceScore,
      icon: Zap,
      description: "Efficiency & scalability",
    },
    {
      title: "Maintainability",
      score: score.maintainabilityScore,
      icon: Code2,
      description: "Code quality & readability",
    },
    {
      title: "Best Practices",
      score: score.bestPracticesScore,
      icon: BookCheck,
      description: "Spring Boot standards",
    },
  ];

  return (
    <div className="space-y-6">
      {/* Hero Score Card */}
      <Card className="border-2 border-primary/20 bg-gradient-to-br from-primary/5 to-transparent">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-2xl">Code Quality Score</CardTitle>
              <CardDescription className="text-base">
                Overall assessment based on industry standards
              </CardDescription>
            </div>
            <Trophy className="h-8 w-8 text-primary" />
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-2 gap-8">
            {/* Overall Score */}
            <div className="flex flex-col items-center justify-center space-y-4">
              <ProgressCircle value={score.overallScore} size={160} strokeWidth={12} />
              <div className="text-center space-y-2">
                <div className={cn("text-6xl font-bold", getGradeColor(score.grade))}>
                  {score.grade}
                </div>
                <p className="text-sm text-muted-foreground">Grade</p>
              </div>
            </div>

            {/* Employability Status */}
            <div className="flex flex-col justify-center space-y-4">
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <div className={cn("p-2 rounded-full", hireability.color)}>
                    <StatusIcon className="h-5 w-5 text-white" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-lg">{hireability.status}</h3>
                    <p className="text-sm text-muted-foreground">{hireability.description}</p>
                  </div>
                </div>

                {/* Key Metrics Summary */}
                {findings && (
                  <div className="grid grid-cols-2 gap-3 mt-6 p-4 bg-muted/50 rounded-lg">
                    <div>
                      <p className="text-2xl font-bold text-red-500">{findings.critical}</p>
                      <p className="text-xs text-muted-foreground">Critical Issues</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-orange-500">{findings.high}</p>
                      <p className="text-xs text-muted-foreground">High Priority</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold">{findings.total}</p>
                      <p className="text-xs text-muted-foreground">Total Findings</p>
                    </div>
                    <div>
                      <p className="text-2xl font-bold text-green-500">
                        {((1 - findings.total / 100) * 100).toFixed(0)}%
                      </p>
                      <p className="text-xs text-muted-foreground">Clean Code</p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Category Scores */}
      <div className="grid md:grid-cols-4 gap-4">
        {metrics.map((metric) => {
          const Icon = metric.icon;
          return (
            <Card key={metric.title} className="hover:shadow-lg transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <Icon className="h-5 w-5 text-primary" />
                  <Badge variant={metric.score >= 70 ? "default" : "destructive"}>
                    {metric.score}/100
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                <div>
                  <h3 className="font-semibold">{metric.title}</h3>
                  <p className="text-xs text-muted-foreground mt-1">{metric.description}</p>
                </div>
                <ProgressCircle value={metric.score} size={80} strokeWidth={6} showValue={false} />
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Benchmark Comparison */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-primary" />
            <CardTitle>Benchmark Comparison</CardTitle>
          </div>
          <CardDescription>How your code compares to other Spring Boot projects</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Your Score</span>
                <span className="font-semibold">{score.overallScore}/100</span>
              </div>
              <div className="relative h-8 bg-muted rounded-full overflow-hidden">
                <div
                  className="absolute inset-y-0 left-0 bg-gradient-to-r from-red-500 via-yellow-500 to-green-500 transition-all duration-1000"
                  style={{ width: `${score.overallScore}%` }}
                />
                <div className="absolute inset-0 flex items-center justify-center text-xs font-medium">
                  Top {100 - score.overallScore}%
                </div>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4 pt-4 border-t">
              <div className="text-center">
                <p className="text-2xl font-bold text-muted-foreground">25th</p>
                <p className="text-xs text-muted-foreground">Percentile (50/100)</p>
              </div>
              <div className="text-center border-x">
                <p className="text-2xl font-bold text-primary">50th</p>
                <p className="text-xs text-muted-foreground">Percentile (65/100)</p>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold text-green-500">75th</p>
                <p className="text-xs text-muted-foreground">Percentile (80/100)</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
