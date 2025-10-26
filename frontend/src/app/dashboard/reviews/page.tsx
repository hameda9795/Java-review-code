"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { reviewsApi } from "@/lib/api/reviews";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Plus, FileCode } from "lucide-react";
import { formatDate, getGradeColor } from "@/lib/utils";

export default function ReviewsPage() {
  const { data: reviews, isLoading } = useQuery({
    queryKey: ["reviews"],
    queryFn: () => reviewsApi.list(),
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">All Reviews</h1>
        <div className="grid gap-4">
          {[1, 2, 3].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <div className="h-6 bg-muted rounded animate-pulse mb-2" />
                <div className="h-4 bg-muted rounded animate-pulse w-1/3" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">All Reviews</h1>
        <Button asChild>
          <Link href="/dashboard/reviews/new">
            <Plus className="mr-2 h-4 w-4" />
            New Review
          </Link>
        </Button>
      </div>

      {reviews && reviews.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16">
            <FileCode className="h-16 w-16 text-muted-foreground mb-4" />
            <h2 className="text-xl font-semibold mb-2">No reviews yet</h2>
            <p className="text-muted-foreground mb-6">Get started by creating your first code review</p>
            <Button asChild>
              <Link href="/dashboard/reviews/new">
                <Plus className="mr-2 h-4 w-4" />
                Create First Review
              </Link>
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4">
          {reviews?.map((review) => (
            <Card key={review.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <Link href={`/dashboard/reviews/${review.id}`}>
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h2 className="text-xl font-semibold mb-2">{review.title}</h2>
                      <div className="flex items-center gap-4 text-sm text-muted-foreground mb-3">
                        <span>{formatDate(review.createdAt)}</span>
                        <span className="px-2 py-1 rounded-full bg-muted text-xs font-medium">
                          {review.status}
                        </span>
                        <span>{review.totalFilesAnalyzed} files</span>
                        <span>{review.findings.length} findings</span>
                      </div>
                      {review.description && (
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {review.description}
                        </p>
                      )}
                    </div>
                    {review.qualityScore && (
                      <div className="text-right ml-6">
                        <div className={`text-4xl font-bold ${getGradeColor(review.qualityScore.grade)}`}>
                          {review.qualityScore.grade}
                        </div>
                        <p className="text-sm text-muted-foreground mt-1">
                          {review.qualityScore.overallScore}/100
                        </p>
                      </div>
                    )}
                  </div>
                </Link>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
