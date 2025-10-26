"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import {
  Briefcase,
  Target,
  TrendingUp,
  AlertTriangle,
  CheckCircle2,
  Clock,
  DollarSign,
  Award,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface HiringImpactPanelProps {
  overallScore: number;
  criticalIssues: number;
  highIssues: number;
  securityScore: number;
}

export function HiringImpactPanel({
  overallScore,
  criticalIssues,
  highIssues,
  securityScore,
}: HiringImpactPanelProps) {
  const getHireabilityLevel = () => {
    if (overallScore >= 85)
      return {
        level: "Senior",
        description: "Your code demonstrates senior-level craftsmanship",
        color: "text-purple-600",
        bgColor: "bg-purple-50 dark:bg-purple-950/20",
        borderColor: "border-purple-200",
        icon: Award,
        percentile: 90,
        salary: "$120K - $180K",
      };
    if (overallScore >= 75)
      return {
        level: "Mid-Level",
        description: "Code quality meets mid-level professional standards",
        color: "text-blue-600",
        bgColor: "bg-blue-50 dark:bg-blue-950/20",
        borderColor: "border-blue-200",
        icon: Briefcase,
        percentile: 70,
        salary: "$80K - $120K",
      };
    if (overallScore >= 60)
      return {
        level: "Junior+",
        description: "Suitable for junior positions with some improvements",
        color: "text-green-600",
        bgColor: "bg-green-50 dark:bg-green-950/20",
        borderColor: "border-green-200",
        icon: TrendingUp,
        percentile: 45,
        salary: "$60K - $80K",
      };
    return {
      level: "Entry Level",
      description: "Needs significant improvement before job applications",
      color: "text-orange-600",
      bgColor: "bg-orange-50 dark:bg-orange-950/20",
      borderColor: "border-orange-200",
      icon: AlertTriangle,
      percentile: 25,
      salary: "$45K - $60K",
    };
  };

  const getTimeToImprove = () => {
    const issueCount = criticalIssues * 3 + highIssues * 2;
    if (issueCount > 20) return { time: "4-6 weeks", effort: "High" };
    if (issueCount > 10) return { time: "2-3 weeks", effort: "Medium" };
    if (issueCount > 5) return { time: "1-2 weeks", effort: "Low" };
    return { time: "< 1 week", effort: "Minimal" };
  };

  const getInterviewReadiness = () => {
    if (criticalIssues === 0 && securityScore >= 80)
      return {
        status: "Ready",
        color: "text-green-600",
        bgColor: "bg-green-500",
        message: "Your code is ready to showcase to employers",
      };
    if (criticalIssues <= 2)
      return {
        status: "Almost Ready",
        color: "text-yellow-600",
        bgColor: "bg-yellow-500",
        message: "Fix critical issues before applying",
      };
    return {
      status: "Not Ready",
      color: "text-red-600",
      bgColor: "bg-red-500",
      message: "Significant work needed before interviews",
    };
  };

  const hireability = getHireabilityLevel();
  const improvement = getTimeToImprove();
  const readiness = getInterviewReadiness();
  const HireabilityIcon = hireability.icon;

  const topIssuesForEmployers = [
    {
      issue: "Security Vulnerabilities",
      impact: criticalIssues > 0 ? "Critical Red Flag" : "Looking Good",
      status: criticalIssues > 0 ? "danger" : "success",
      description:
        criticalIssues > 0
          ? `${criticalIssues} critical security issues will raise immediate concerns`
          : "No critical security issues detected",
    },
    {
      issue: "Code Architecture",
      impact: overallScore >= 70 ? "Professional Level" : "Needs Improvement",
      status: overallScore >= 70 ? "success" : "warning",
      description:
        overallScore >= 70
          ? "Architecture demonstrates good design principles"
          : "Architecture patterns need refinement",
    },
    {
      issue: "Best Practices",
      impact: highIssues <= 3 ? "Industry Standard" : "Below Standard",
      status: highIssues <= 3 ? "success" : "warning",
      description:
        highIssues <= 3
          ? "Follows Spring Boot best practices"
          : `${highIssues} high-priority issues to address`,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Hiring Level Card */}
      <Card
        className={cn("border-2", hireability.borderColor, hireability.bgColor)}
      >
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className={cn("p-3 rounded-full bg-white dark:bg-gray-900", hireability.color)}>
              <HireabilityIcon className="h-6 w-6" />
            </div>
            <div className="flex-1">
              <CardTitle className="text-xl">Hiring Level Assessment</CardTitle>
              <CardDescription>{hireability.description}</CardDescription>
            </div>
            <Badge className={cn("text-lg px-4 py-2", hireability.color)}>
              {hireability.level}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid md:grid-cols-3 gap-4">
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Your Percentile</p>
              <div className="flex items-baseline gap-2">
                <p className={cn("text-3xl font-bold", hireability.color)}>
                  {hireability.percentile}%
                </p>
                <p className="text-sm text-muted-foreground">of developers</p>
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Expected Salary Range</p>
              <div className="flex items-baseline gap-2">
                <DollarSign className="h-5 w-5 text-green-600" />
                <p className="text-xl font-semibold">{hireability.salary}</p>
              </div>
            </div>
            <div className="space-y-1">
              <p className="text-sm text-muted-foreground">Time to Improve</p>
              <div className="flex items-baseline gap-2">
                <Clock className="h-5 w-5 text-blue-600" />
                <p className="text-xl font-semibold">{improvement.time}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Interview Readiness */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Interview Readiness</CardTitle>
              <CardDescription>Can you showcase this code to employers?</CardDescription>
            </div>
            <Badge
              variant="outline"
              className={cn("px-4 py-2 text-base", readiness.color)}
            >
              {readiness.status}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center gap-4">
              <div className="flex-1 bg-muted rounded-full h-3 overflow-hidden">
                <div
                  className={cn("h-full transition-all duration-1000", readiness.bgColor)}
                  style={{ width: `${(overallScore / 80) * 100}%` }}
                />
              </div>
              <span className="text-sm font-medium whitespace-nowrap">
                {overallScore}/80 (Interview Ready)
              </span>
            </div>
            <p className="text-sm text-muted-foreground">{readiness.message}</p>
          </div>
        </CardContent>
      </Card>

      {/* Employer Red Flags */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Target className="h-5 w-5 text-primary" />
            What Employers Are Looking For
          </CardTitle>
          <CardDescription>Key areas technical recruiters evaluate</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {topIssuesForEmployers.map((item, index) => (
              <div
                key={index}
                className="flex items-start gap-4 p-3 rounded-lg border bg-card hover:bg-muted/50 transition-colors"
              >
                <div
                  className={cn(
                    "mt-1 h-2 w-2 rounded-full flex-shrink-0",
                    item.status === "success" && "bg-green-500",
                    item.status === "warning" && "bg-yellow-500",
                    item.status === "danger" && "bg-red-500"
                  )}
                />
                <div className="flex-1 space-y-1">
                  <div className="flex items-center justify-between">
                    <h4 className="font-semibold text-sm">{item.issue}</h4>
                    <Badge
                      variant="outline"
                      className={cn(
                        item.status === "success" && "text-green-600 border-green-600",
                        item.status === "warning" && "text-yellow-600 border-yellow-600",
                        item.status === "danger" && "text-red-600 border-red-600"
                      )}
                    >
                      {item.impact}
                    </Badge>
                  </div>
                  <p className="text-xs text-muted-foreground">{item.description}</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Next Steps */}
      <Card className="bg-gradient-to-br from-primary/5 to-transparent border-primary/20">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-primary" />
            Your Path to Hire-Worthy Code
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex items-start gap-3">
              <div className="mt-1 h-6 w-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-sm font-bold flex-shrink-0">
                1
              </div>
              <div>
                <h4 className="font-semibold">Fix All Critical Issues</h4>
                <p className="text-sm text-muted-foreground">
                  {criticalIssues > 0
                    ? `Address ${criticalIssues} critical security/bug issues immediately`
                    : "✓ No critical issues - great job!"}
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="mt-1 h-6 w-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-sm font-bold flex-shrink-0">
                2
              </div>
              <div>
                <h4 className="font-semibold">Improve Security Score to 80+</h4>
                <p className="text-sm text-muted-foreground">
                  Current: {securityScore}/100 - Focus on OWASP Top 10 compliance
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="mt-1 h-6 w-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-sm font-bold flex-shrink-0">
                3
              </div>
              <div>
                <h4 className="font-semibold">Reach Overall Score of 75+</h4>
                <p className="text-sm text-muted-foreground">
                  {overallScore >= 75
                    ? "✓ You've achieved professional-level code quality!"
                    : `${75 - overallScore} points away from professional level`}
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
