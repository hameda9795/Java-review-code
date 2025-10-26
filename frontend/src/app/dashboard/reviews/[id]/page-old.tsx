"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useParams, useRouter } from "next/navigation";
import { reviewsApi } from "@/lib/api/reviews";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/components/ui/use-toast";
import { ArrowLeft, CheckCircle, Download, FileText, Loader2, Trash2 } from "lucide-react";
import { formatDateTime, getSeverityColor, getGradeColor } from "@/lib/utils";
import Link from "next/link";
import { useState } from "react";

export default function ReviewDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const reviewId = params.id as string;
  const [isDownloading, setIsDownloading] = useState(false);
  const [showDownloadMenu, setShowDownloadMenu] = useState(false);

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

  const handleDownload = async (format: 'markdown' | 'html' | 'csv') => {
    setIsDownloading(true);
    setShowDownloadMenu(false);
    try {
      if (format === 'markdown') {
        await reviewsApi.downloadMarkdown(reviewId);
      } else if (format === 'html') {
        await reviewsApi.downloadHtml(reviewId);
      } else if (format === 'csv') {
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
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
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
  const unresolvedFindings = review.findings.filter((f) => !f.isResolved);

  return (
    <div className="space-y-6 max-w-6xl">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" asChild>
          <Link href="/dashboard/reviews">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Link>
        </Button>
      </div>

      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold mb-2">{review.title}</h1>
          <div className="flex items-center gap-3 text-sm text-muted-foreground">
            <span>{formatDateTime(review.createdAt)}</span>
            <Badge>{review.status}</Badge>
            <span>{review.totalFilesAnalyzed} files</span>
            <span>{review.totalLinesAnalyzed} lines</span>
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
              Download
            </Button>
            {showDownloadMenu && (
              <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-md shadow-lg z-10">
                <div className="py-1">
                  <button
                    className="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center"
                    onClick={() => handleDownload('markdown')}
                  >
                    <FileText className="h-4 w-4 mr-2" />
                    Markdown (.md)
                  </button>
                  <button
                    className="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center"
                    onClick={() => handleDownload('html')}
                  >
                    <FileText className="h-4 w-4 mr-2" />
                    HTML (.html)
                  </button>
                  <button
                    className="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center"
                    onClick={() => handleDownload('csv')}
                  >
                    <FileText className="h-4 w-4 mr-2" />
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
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4" />
            )}
          </Button>
        </div>
      </div>

      {review.qualityScore && (
        <div className="grid gap-4 md:grid-cols-5">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Overall</CardTitle>
            </CardHeader>
            <CardContent>
              <div className={`text-3xl font-bold ${getGradeColor(review.qualityScore.grade)}`}>
                {review.qualityScore.grade}
              </div>
              <p className="text-xs text-muted-foreground">{review.qualityScore.overallScore}/100</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Security</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{review.qualityScore.securityScore}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Performance</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{review.qualityScore.performanceScore}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Maintainability</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{review.qualityScore.maintainabilityScore}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium">Best Practices</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{review.qualityScore.bestPracticesScore}</div>
            </CardContent>
          </Card>
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Findings ({review.findings.length})</CardTitle>
          <CardDescription>
            {criticalFindings.length} critical, {highFindings.length} high priority,{" "}
            {unresolvedFindings.length} unresolved
          </CardDescription>
        </CardHeader>
        <CardContent>
          {review.findings.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No findings - great job! Your code looks clean.
            </div>
          ) : (
            <div className="space-y-4">
              {review.findings.map((finding) => (
                <div key={finding.id} className="border rounded-lg p-4 space-y-3">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <Badge
                          className={getSeverityColor(finding.severity)}
                          variant="outline"
                        >
                          {finding.severity}
                        </Badge>
                        <Badge variant="outline">{finding.category}</Badge>
                        {finding.isResolved && (
                          <Badge variant="secondary">
                            <CheckCircle className="h-3 w-3 mr-1" />
                            Resolved
                          </Badge>
                        )}
                      </div>
                      <h3 className="font-semibold text-lg">{finding.title}</h3>
                      <p className="text-sm text-muted-foreground mt-1">{finding.description}</p>
                    </div>
                    {!finding.isResolved && (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => resolveFindingMutation.mutate(finding.id)}
                        disabled={resolveFindingMutation.isPending}
                      >
                        Mark Resolved
                      </Button>
                    )}
                  </div>

                  {finding.filePath && (
                    <div className="text-sm">
                      <span className="text-muted-foreground">File: </span>
                      <code className="text-xs bg-muted px-2 py-1 rounded">
                        {finding.filePath}
                        {finding.lineNumber && `:${finding.lineNumber}`}
                      </code>
                    </div>
                  )}

                  {finding.codeSnippet && (
                    <div>
                      <p className="text-sm font-medium mb-2">Code:</p>
                      <pre className="bg-muted p-3 rounded-md overflow-x-auto text-xs">
                        <code>{finding.codeSnippet}</code>
                      </pre>
                    </div>
                  )}

                  {finding.suggestedFix && (
                    <div>
                      <p className="text-sm font-medium mb-2 text-green-600">Suggested Fix:</p>
                      <pre className="bg-green-50 dark:bg-green-950 p-3 rounded-md overflow-x-auto text-xs border border-green-200">
                        <code>{finding.suggestedFix}</code>
                      </pre>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
