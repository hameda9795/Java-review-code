"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useParams, useRouter } from "next/navigation";
import { reviewsApi } from "@/lib/api/reviews";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/components/ui/use-toast";
import {
  ArrowLeft,
  Download,
  FileText,
  Loader2,
  Trash2,
  BarChart3,
  ListChecks,
  Briefcase,
  GraduationCap,
} from "lucide-react";
import { formatDateTime } from "@/lib/utils";
import Link from "next/link";
import { useState } from "react";

// Import new components
import { QualityScoreDashboard } from "@/components/review/quality-score-dashboard";
import { FindingsExplorer } from "@/components/review/findings-explorer";
import { HiringImpactPanel } from "@/components/review/hiring-impact-panel";
import { LearningPath } from "@/components/review/learning-path";

export default function ReviewDetailsPageEnhanced() {
  const params = useParams();
  const router = useRouter();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const reviewId = params.id as string;
  const [isDownloading, setIsDownloading] = useState(false);
  const [showDownloadMenu, setShowDownloadMenu] = useState(false);
  const [activeTab, setActiveTab] = useState("overview");

  const { data: review, isLoading } = useQuery({
    queryKey: ["review", reviewId],
    queryFn: () => reviewsApi.getById(reviewId),
  });

  const deleteMutation = useMutation({
    mutationFn: () => reviewsApi.delete(reviewId),
    onSuccess: () => {
      toast({
        title: "Review deleted",
        description: "The review has been deleted successfully",
      });
      router.push("/dashboard/reviews");
    },
  });

  const resolveFindingMutation = useMutation({
    mutationFn: (findingId: string) => reviewsApi.resolveFinding(reviewId, findingId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["review", reviewId] });
      toast({
        title: "Finding resolved",
        description: "The finding has been marked as resolved",
      });
    },
  });

  const handleDownload = async (format: "markdown" | "html" | "csv") => {
    setIsDownloading(true);
    setShowDownloadMenu(false);
    try {
      if (format === "markdown") {
        await reviewsApi.downloadMarkdown(reviewId);
      } else if (format === "html") {
        await reviewsApi.downloadHtml(reviewId);
      } else if (format === "csv") {
        await reviewsApi.downloadCsv(reviewId);
      }
      toast({
        title: "Download started",
        description: `Review report (${format.toUpperCase()}) is being downloaded`,
      });
    } catch (error) {
      toast({
        title: "Download failed",
        description: "Failed to download the report. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsDownloading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center space-y-4">
          <Loader2 className="h-12 w-12 animate-spin text-primary mx-auto" />
          <p className="text-muted-foreground">Loading your code review...</p>
        </div>
      </div>
    );
  }

  if (!review) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold mb-2">Review not found</h2>
        <Button asChild>
          <Link href="/dashboard/reviews">Back to Reviews</Link>
        </Button>
      </div>
    );
  }

  const criticalFindings = review.findings.filter((f) => f.severity === "CRITICAL");
  const highFindings = review.findings.filter((f) => f.severity === "HIGH");
  const mediumFindings = review.findings.filter((f) => f.severity === "MEDIUM");
  const lowFindings = review.findings.filter((f) => f.severity === "LOW");
  const infoFindings = review.findings.filter((f) => f.severity === "INFO");

  const findingsCounts = {
    total: review.findings.length,
    critical: criticalFindings.length,
    high: highFindings.length,
    medium: mediumFindings.length,
    low: lowFindings.length,
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" asChild>
          <Link href="/dashboard/reviews">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Reviews
          </Link>
        </Button>
      </div>

      {/* Title and Actions */}
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <h1 className="text-4xl font-bold mb-3">{review.title}</h1>
          <div className="flex items-center gap-4 text-sm text-muted-foreground flex-wrap">
            <span>{formatDateTime(review.createdAt)}</span>
            <Badge variant="outline" className="font-normal">
              {review.status}
            </Badge>
            <span>{review.totalFilesAnalyzed} files analyzed</span>
            <span>{review.totalLinesAnalyzed} lines of code</span>
            <span className="text-primary font-medium">
              {((review.analysisTime || 0) / 1000).toFixed(1)}s analysis time
            </span>
          </div>
        </div>
        <div className="flex gap-2">
          <div className="relative">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowDownloadMenu(!showDownloadMenu)}
              disabled={isDownloading}
            >
              {isDownloading ? (
                <Loader2 className="h-4 w-4 animate-spin mr-2" />
              ) : (
                <Download className="h-4 w-4 mr-2" />
              )}
              Export
            </Button>
            {showDownloadMenu && (
              <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-10 overflow-hidden">
                <div className="py-1">
                  <button
                    className="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 transition-colors"
                    onClick={() => handleDownload("markdown")}
                  >
                    <FileText className="h-4 w-4" />
                    Markdown (.md)
                  </button>
                  <button
                    className="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 transition-colors"
                    onClick={() => handleDownload("html")}
                  >
                    <FileText className="h-4 w-4" />
                    HTML (.html)
                  </button>
                  <button
                    className="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2 transition-colors"
                    onClick={() => handleDownload("csv")}
                  >
                    <FileText className="h-4 w-4" />
                    CSV (.csv)
                  </button>
                </div>
              </div>
            )}
          </div>
          <Button
            variant="destructive"
            size="sm"
            onClick={() => deleteMutation.mutate()}
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin mr-2" />
            ) : (
              <Trash2 className="h-4 w-4 mr-2" />
            )}
            Delete
          </Button>
        </div>
      </div>

      {/* Tabbed Interface */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="overview" className="flex items-center gap-2">
            <BarChart3 className="h-4 w-4" />
            Quality Score
          </TabsTrigger>
          <TabsTrigger value="findings" className="flex items-center gap-2">
            <ListChecks className="h-4 w-4" />
            Findings ({review.findings.length})
          </TabsTrigger>
          <TabsTrigger value="hiring" className="flex items-center gap-2">
            <Briefcase className="h-4 w-4" />
            Hiring Impact
          </TabsTrigger>
          <TabsTrigger value="learning" className="flex items-center gap-2">
            <GraduationCap className="h-4 w-4" />
            Learning Path
          </TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value="overview" className="space-y-6">
          {review.qualityScore ? (
            <QualityScoreDashboard score={review.qualityScore} findings={findingsCounts} />
          ) : (
            <Card>
              <CardContent className="py-12 text-center">
                <Loader2 className="h-12 w-12 animate-spin text-primary mx-auto mb-4" />
                <h3 className="text-lg font-semibold mb-2">Calculating Quality Score</h3>
                <p className="text-muted-foreground">
                  Please wait while we analyze your code quality...
                </p>
              </CardContent>
            </Card>
          )}

          {/* Quick Stats */}
          <div className="grid md:grid-cols-3 gap-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-sm font-medium">AI Model Used</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-2xl font-bold">{review.aiModel || "GPT-4"}</p>
                <p className="text-xs text-muted-foreground mt-1">
                  {review.tokensUsed ? `${review.tokensUsed.toLocaleString()} tokens` : ""}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-sm font-medium">Analysis Time</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-2xl font-bold">
                  {((review.analysisTime || 0) / 1000).toFixed(1)}s
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  ~{(review.totalLinesAnalyzed / ((review.analysisTime || 1) / 1000)).toFixed(0)}{" "}
                  lines/sec
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-sm font-medium">Code Coverage</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-2xl font-bold">
                  {review.qualityScore?.testCoverageScore || "N/A"}
                  {review.qualityScore?.testCoverageScore && "%"}
                </p>
                <p className="text-xs text-muted-foreground mt-1">Test coverage estimate</p>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Findings Tab */}
        <TabsContent value="findings" className="space-y-6">
          <FindingsExplorer
            findings={review.findings}
            onResolveFinding={(findingId) => resolveFindingMutation.mutate(findingId)}
          />
        </TabsContent>

        {/* Hiring Impact Tab */}
        <TabsContent value="hiring" className="space-y-6">
          {review.qualityScore && (
            <HiringImpactPanel
              overallScore={review.qualityScore.overallScore}
              criticalIssues={criticalFindings.length}
              highIssues={highFindings.length}
              securityScore={review.qualityScore.securityScore}
            />
          )}
        </TabsContent>

        {/* Learning Path Tab */}
        <TabsContent value="learning" className="space-y-6">
          <LearningPath
            findings={review.findings}
            overallScore={review.qualityScore?.overallScore || 0}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
}
