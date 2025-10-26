export interface CodeQualityScore {
  overallScore: number;
  grade: string;
  securityScore: number;
  performanceScore: number;
  maintainabilityScore: number;
  bestPracticesScore: number;
  testCoverageScore: number;
}

export interface ReviewFinding {
  id: string;
  title: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO';
  category: string;
  description: string;
  filePath: string;
  lineNumber?: number;
  codeSnippet?: string;
  suggestedFix?: string;
  isResolved: boolean;
}

export interface CodeReview {
  id: string;
  title: string;
  description?: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  totalFilesAnalyzed: number;
  totalLinesAnalyzed: number;
  tokensUsed: number;
  costUsd: number;
  qualityScore?: CodeQualityScore;
  findings: ReviewFinding[];
}

export interface CreateReviewRequest {
  title: string;
  files: Record<string, string>;
}

export interface ReviewStats {
  totalReviews: number;
  completedReviews: number;
  averageQualityScore: number;
  totalFindings: number;
  criticalIssues: number;
}
