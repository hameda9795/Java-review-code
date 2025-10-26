"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { githubApi } from "@/lib/api/github";
import { useToast } from "@/components/ui/use-toast";
import { useAuthStore } from "@/lib/store/authStore";
import { Github, Loader2 } from "lucide-react";

interface GitHubRepoSelectorProps {
  onFilesLoaded: (files: Record<string, string>, repoName: string) => void;
}

export function GitHubRepoSelector({ onFilesLoaded }: GitHubRepoSelectorProps) {
  const { user } = useAuthStore();
  const { toast } = useToast();
  const [repoUrl, setRepoUrl] = useState("");
  const [loading, setLoading] = useState(false);

  const parseRepoUrl = (url: string): { owner: string; repo: string } | null => {
    // Support formats: owner/repo, https://github.com/owner/repo, etc.
    const patterns = [
      /github\.com\/([^/]+)\/([^/]+)/,
      /^([^/]+)\/([^/]+)$/,
    ];

    for (const pattern of patterns) {
      const match = url.match(pattern);
      if (match) {
        return { owner: match[1], repo: match[2].replace(/\.git$/, '') };
      }
    }
    return null;
  };

  const handleAnalyze = async () => {
    if (!user) {
      toast({
        title: "Not authenticated",
        description: "Please log in to analyze repositories",
        variant: "destructive",
      });
      return;
    }

    const parsed = parseRepoUrl(repoUrl);
    
    if (!parsed) {
      toast({
        title: "Invalid repository",
        description: "Please enter a valid GitHub repository (e.g., owner/repo or full URL)",
        variant: "destructive",
      });
      return;
    }

    setLoading(true);

    try {
      console.log('Analyzing repository:', parsed);
      const files = await githubApi.analyzeRepository(parsed.owner, parsed.repo);
      const fileCount = Object.keys(files).length;
      
      if (fileCount === 0) {
        toast({
          title: "No Java files found",
          description: "This repository doesn't contain any Java files to review",
          variant: "destructive",
        });
        return;
      }

      onFilesLoaded(files, `${parsed.owner}/${parsed.repo}`);
      
      toast({
        title: "Repository loaded",
        description: `Found ${fileCount} Java file(s) from ${parsed.owner}/${parsed.repo}`,
      });
      
      setRepoUrl("");
    } catch (error: any) {
      console.error("Error analyzing repository:", error);
      
      let errorMessage = "Please check the repository name and try again";
      
      if (error.response?.status === 403) {
        errorMessage = "Access denied. Make sure your GitHub account is connected and you have access to this repository.";
      } else if (error.response?.status === 401) {
        errorMessage = "Authentication failed. Please reconnect your GitHub account in Settings.";
      } else if (error.response?.status === 404) {
        errorMessage = "Repository not found. Check the owner/repo name.";
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toast({
        title: "Failed to load repository",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  if (!user?.githubConnected) {
    return (
      <div className="p-4 border border-dashed rounded-lg text-center">
        <Github className="h-8 w-8 mx-auto mb-2 text-muted-foreground" />
        <p className="text-sm text-muted-foreground mb-2">
          Connect your GitHub account to analyze repositories
        </p>
        <Button variant="outline" size="sm" onClick={() => window.location.href = '/dashboard/settings'}>
          Go to Settings
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="repo-url">GitHub Repository</Label>
        <div className="flex gap-2">
          <Input
            id="repo-url"
            placeholder="owner/repo or https://github.com/owner/repo"
            value={repoUrl}
            onChange={(e) => setRepoUrl(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleAnalyze()}
            disabled={loading}
          />
          <Button onClick={handleAnalyze} disabled={loading || !repoUrl}>
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Loading...
              </>
            ) : (
              <>
                <Github className="mr-2 h-4 w-4" />
                Analyze
              </>
            )}
          </Button>
        </div>
        <p className="text-xs text-muted-foreground">
          Enter a GitHub repository to automatically import all Java files for review
        </p>
      </div>
    </div>
  );
}
