"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { reviewsApi } from "@/lib/api/reviews";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FileCode, CheckCircle, AlertCircle, TrendingUp, Plus } from "lucide-react";
import { formatDate, getGradeColor, getSeverityColor } from "@/lib/utils";

export default function DashboardPage() {
  const { data: reviews, isLoading: reviewsLoading } = useQuery({
    queryKey: ["reviews"],
    queryFn: () => reviewsApi.list(),
  });

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ["stats"],
    queryFn: () => reviewsApi.getStats(),
  });

  if (reviewsLoading || statsLoading) {
    return (
      <div className="space-y-8">
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <Card key={i}>
              <CardHeader>
                <div className="h-4 bg-muted rounded animate-pulse" />
              </CardHeader>
              <CardContent>
                <div className="h-8 bg-muted rounded animate-pulse" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  const recentReviews = reviews?.slice(0, 5) || [];

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
          <p className="text-muted-foreground">Welcome back! Here's your code quality overview.</p>
        </div>
        <Button asChild>
          <Link href="/dashboard/reviews/new">
            <Plus className="mr-2 h-4 w-4" />
            New Review
          </Link>
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Reviews</CardTitle>
            <FileCode className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.totalReviews || 0}</div>
            <p className="text-xs text-muted-foreground">
              {stats?.completedReviews || 0} completed
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Score</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats?.averageQualityScore ? stats.averageQualityScore.toFixed(0) : 0}
            </div>
            <p className="text-xs text-muted-foreground">Out of 100</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Findings</CardTitle>
            <AlertCircle className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.totalFindings || 0}</div>
            <p className="text-xs text-muted-foreground">
              {stats?.criticalIssues || 0} critical
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Completed</CardTitle>
            <CheckCircle className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.completedReviews || 0}</div>
            <p className="text-xs text-muted-foreground">Reviews done</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Recent Reviews</CardTitle>
            <Button variant="ghost" size="sm" asChild>
              <Link href="/dashboard/reviews">View All</Link>
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {recentReviews.length === 0 ? (
            <div className="text-center py-12">
              <FileCode className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-4">No reviews yet</p>
              <Button asChild>
                <Link href="/dashboard/reviews/new">Create Your First Review</Link>
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              {recentReviews.map((review) => (
                <Link
                  key={review.id}
                  href={`/dashboard/reviews/${review.id}`}
                  className="flex items-center justify-between border-b pb-4 last:border-0 hover:bg-muted/50 p-2 rounded-md transition-colors"
                >
                  <div className="flex-1">
                    <h3 className="font-medium">{review.title}</h3>
                    <div className="flex items-center gap-4 mt-1">
                      <p className="text-sm text-muted-foreground">
                        {formatDate(review.createdAt)}
                      </p>
                      <span className="text-xs px-2 py-1 rounded-full bg-muted">
                        {review.status}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {review.findings.length} findings
                      </span>
                    </div>
                  </div>
                  <div className="text-right">
                    {review.qualityScore ? (
                      <>
                        <div className={`text-2xl font-bold ${getGradeColor(review.qualityScore.grade)}`}>
                          {review.qualityScore.grade}
                        </div>
                        <p className="text-xs text-muted-foreground">
                          {review.qualityScore.overallScore}/100
                        </p>
                      </>
                    ) : (
                      <div className="text-sm text-muted-foreground">Pending</div>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
