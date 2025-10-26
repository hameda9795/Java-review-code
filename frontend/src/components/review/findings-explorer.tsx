"use client";

import { useState, useMemo } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Search,
  Filter,
  AlertCircle,
  CheckCircle2,
  Code2,
  ExternalLink,
  TrendingUp,
  ChevronDown,
  ChevronUp,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface Finding {
  id: string;
  title: string;
  description: string;
  severity: "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | "INFO";
  category: string;
  filePath?: string;
  lineNumber?: number;
  codeSnippet?: string;
  suggestedFix?: string;
  explanation?: string;
  resourcesUrl?: string;
  metricsViolated?: string;
  impactScore?: number;
  isResolved: boolean;
}

interface FindingsExplorerProps {
  findings: Finding[];
  onResolveFinding?: (findingId: string) => void;
}

export function FindingsExplorer({ findings, onResolveFinding }: FindingsExplorerProps) {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedSeverity, setSelectedSeverity] = useState<string>("ALL");
  const [selectedCategory, setSelectedCategory] = useState<string>("ALL");
  const [showResolved, setShowResolved] = useState(false);
  const [expandedFindings, setExpandedFindings] = useState<Set<string>>(new Set());

  const severities = ["ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"];
  const categories = useMemo(() => {
    const cats = new Set(findings.map((f) => f.category));
    return ["ALL", ...Array.from(cats).sort()];
  }, [findings]);

  const filteredFindings = useMemo(() => {
    return findings.filter((finding) => {
      const matchesSearch =
        finding.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        finding.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        finding.filePath?.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesSeverity =
        selectedSeverity === "ALL" || finding.severity === selectedSeverity;

      const matchesCategory =
        selectedCategory === "ALL" || finding.category === selectedCategory;

      const matchesResolved = showResolved || !finding.isResolved;

      return matchesSearch && matchesSeverity && matchesCategory && matchesResolved;
    });
  }, [findings, searchTerm, selectedSeverity, selectedCategory, showResolved]);

  const severityCounts = useMemo(() => {
    return findings.reduce(
      (acc, finding) => {
        acc[finding.severity] = (acc[finding.severity] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );
  }, [findings]);

  const getSeverityColor = (severity: string) => {
    const colors = {
      CRITICAL: "bg-red-500 text-white hover:bg-red-600",
      HIGH: "bg-orange-500 text-white hover:bg-orange-600",
      MEDIUM: "bg-yellow-500 text-white hover:bg-yellow-600",
      LOW: "bg-blue-500 text-white hover:bg-blue-600",
      INFO: "bg-gray-500 text-white hover:bg-gray-600",
    };
    return colors[severity as keyof typeof colors] || "bg-gray-500";
  };

  const toggleExpanded = (findingId: string) => {
    const newExpanded = new Set(expandedFindings);
    if (newExpanded.has(findingId)) {
      newExpanded.delete(findingId);
    } else {
      newExpanded.add(findingId);
    }
    setExpandedFindings(newExpanded);
  };

  return (
    <div className="space-y-6">
      {/* Search and Filters */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Filter className="h-5 w-5 text-primary" />
            <CardTitle>Find & Filter Issues</CardTitle>
          </div>
          <CardDescription>
            Showing {filteredFindings.length} of {findings.length} findings
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Search Bar */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by title, description, or file path..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>

          {/* Severity Filter */}
          <div className="space-y-2">
            <label className="text-sm font-medium">Severity</label>
            <div className="flex flex-wrap gap-2">
              {severities.map((severity) => (
                <Button
                  key={severity}
                  variant={selectedSeverity === severity ? "default" : "outline"}
                  size="sm"
                  onClick={() => setSelectedSeverity(severity)}
                  className={cn(
                    "transition-all",
                    selectedSeverity === severity && severity !== "ALL"
                      ? getSeverityColor(severity)
                      : ""
                  )}
                >
                  {severity}
                  {severity !== "ALL" && severityCounts[severity] && (
                    <Badge variant="secondary" className="ml-2">
                      {severityCounts[severity]}
                    </Badge>
                  )}
                </Button>
              ))}
            </div>
          </div>

          {/* Category Filter */}
          <div className="space-y-2">
            <label className="text-sm font-medium">Category</label>
            <div className="flex flex-wrap gap-2">
              {categories.slice(0, 8).map((category) => (
                <Button
                  key={category}
                  variant={selectedCategory === category ? "default" : "outline"}
                  size="sm"
                  onClick={() => setSelectedCategory(category)}
                >
                  {category.replace(/_/g, " ")}
                </Button>
              ))}
            </div>
          </div>

          {/* Show Resolved Toggle */}
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="showResolved"
              checked={showResolved}
              onChange={(e) => setShowResolved(e.target.checked)}
              className="rounded"
            />
            <label htmlFor="showResolved" className="text-sm cursor-pointer">
              Show resolved issues
            </label>
          </div>
        </CardContent>
      </Card>

      {/* Findings List */}
      <div className="space-y-4">
        {filteredFindings.length === 0 ? (
          <Card>
            <CardContent className="py-12 text-center">
              <CheckCircle2 className="h-12 w-12 text-green-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">No findings match your filters</h3>
              <p className="text-muted-foreground">
                Try adjusting your search or filter criteria
              </p>
            </CardContent>
          </Card>
        ) : (
          filteredFindings.map((finding) => {
            const isExpanded = expandedFindings.has(finding.id);
            return (
              <Card
                key={finding.id}
                className={cn(
                  "transition-all hover:shadow-md",
                  finding.isResolved && "opacity-60"
                )}
              >
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 space-y-2">
                      <div className="flex items-center gap-2 flex-wrap">
                        <Badge className={getSeverityColor(finding.severity)}>
                          {finding.severity}
                        </Badge>
                        <Badge variant="outline">{finding.category.replace(/_/g, " ")}</Badge>
                        {finding.isResolved && (
                          <Badge variant="secondary">
                            <CheckCircle2 className="h-3 w-3 mr-1" />
                            Resolved
                          </Badge>
                        )}
                        {finding.impactScore && (
                          <Badge variant="outline">
                            <TrendingUp className="h-3 w-3 mr-1" />
                            Impact: {finding.impactScore}/10
                          </Badge>
                        )}
                      </div>
                      <h3 className="font-semibold text-lg">{finding.title}</h3>
                      <p className="text-sm text-muted-foreground">{finding.description}</p>
                      {finding.filePath && (
                        <div className="flex items-center gap-2 text-sm">
                          <Code2 className="h-4 w-4 text-muted-foreground" />
                          <code className="text-xs bg-muted px-2 py-1 rounded">
                            {finding.filePath}
                            {finding.lineNumber && `:${finding.lineNumber}`}
                          </code>
                        </div>
                      )}
                    </div>
                    <div className="flex flex-col gap-2">
                      {!finding.isResolved && onResolveFinding && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => onResolveFinding(finding.id)}
                        >
                          <CheckCircle2 className="h-4 w-4 mr-2" />
                          Resolve
                        </Button>
                      )}
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => toggleExpanded(finding.id)}
                      >
                        {isExpanded ? (
                          <>
                            <ChevronUp className="h-4 w-4 mr-1" />
                            Less
                          </>
                        ) : (
                          <>
                            <ChevronDown className="h-4 w-4 mr-1" />
                            More
                          </>
                        )}
                      </Button>
                    </div>
                  </div>
                </CardHeader>

                {isExpanded && (
                  <CardContent className="space-y-4 border-t pt-4">
                    {/* Metrics Violated */}
                    {finding.metricsViolated && (
                      <div className="space-y-2">
                        <h4 className="text-sm font-semibold flex items-center gap-2">
                          <AlertCircle className="h-4 w-4" />
                          Metrics Violated
                        </h4>
                        <div className="bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-900 rounded-md p-3">
                          <pre className="text-xs text-red-900 dark:text-red-100">
                            {finding.metricsViolated}
                          </pre>
                        </div>
                      </div>
                    )}

                    {/* Code Snippet */}
                    {finding.codeSnippet && (
                      <div className="space-y-2">
                        <h4 className="text-sm font-semibold">Code</h4>
                        <pre className="bg-muted p-4 rounded-md overflow-x-auto text-xs border">
                          <code>{finding.codeSnippet}</code>
                        </pre>
                      </div>
                    )}

                    {/* Suggested Fix */}
                    {finding.suggestedFix && (
                      <div className="space-y-2">
                        <h4 className="text-sm font-semibold text-green-600 dark:text-green-400">
                          Suggested Fix
                        </h4>
                        <pre className="bg-green-50 dark:bg-green-950/20 border border-green-200 dark:border-green-900 p-4 rounded-md overflow-x-auto text-xs">
                          <code>{finding.suggestedFix}</code>
                        </pre>
                      </div>
                    )}

                    {/* Explanation */}
                    {finding.explanation && (
                      <div className="space-y-2">
                        <h4 className="text-sm font-semibold">Why This Matters</h4>
                        <p className="text-sm text-muted-foreground bg-muted/50 p-3 rounded-md">
                          {finding.explanation}
                        </p>
                      </div>
                    )}

                    {/* Learn More Link */}
                    {finding.resourcesUrl && (
                      <div>
                        <a
                          href={finding.resourcesUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
                        >
                          <ExternalLink className="h-4 w-4" />
                          Learn more about this issue
                        </a>
                      </div>
                    )}
                  </CardContent>
                )}
              </Card>
            );
          })
        )}
      </div>
    </div>
  );
}
