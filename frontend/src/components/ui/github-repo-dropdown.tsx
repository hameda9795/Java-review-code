"use client";

import { useState, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { githubApi } from "@/lib/api/github";
import { useToast } from "@/components/ui/use-toast";
import { useAuthStore } from "@/lib/store/authStore";
import { Github, Loader2, ChevronDown, Search, FolderGit2 } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

interface Repository {
  id: number;
  name: string;
  fullName: string;
  description: string | null;
  url: string;
  language: string | null;
  defaultBranch: string;
  isPrivate: boolean;
}

interface GitHubRepoDropdownProps {
  onFilesLoaded: (files: Record<string, string>, repoName: string) => void;
}

export function GitHubRepoDropdown({ onFilesLoaded }: GitHubRepoDropdownProps) {
  const { user } = useAuthStore();
  const { toast } = useToast();
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [filteredRepos, setFilteredRepos] = useState<Repository[]>([]);
  const [selectedRepo, setSelectedRepo] = useState<Repository | null>(null);
  const [loadingRepos, setLoadingRepos] = useState(false);
  const [analyzingRepo, setAnalyzingRepo] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };

    if (isDropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isDropdownOpen]);

  useEffect(() => {
    if (user?.githubConnected) {
      loadRepositories();
    }
  }, [user?.githubConnected]);

  useEffect(() => {
    if (searchQuery.trim() === "") {
      setFilteredRepos(repositories);
    } else {
      const query = searchQuery.toLowerCase();
      setFilteredRepos(
        repositories.filter(
          (repo) =>
            repo.name.toLowerCase().includes(query) ||
            repo.fullName.toLowerCase().includes(query) ||
            (repo.description && repo.description.toLowerCase().includes(query))
        )
      );
    }
  }, [searchQuery, repositories]);

  const loadRepositories = async () => {
    setLoadingRepos(true);
    try {
      const repos = await githubApi.getRepositories();
      setRepositories(repos);
      setFilteredRepos(repos);
      
      toast({
        title: "Repositories loaded",
        description: `Found ${repos.length} repositories`,
      });
    } catch (error: any) {
      console.error("Error loading repositories:", error);
      
      let errorMessage = "Failed to load repositories";
      if (error.response?.status === 401) {
        errorMessage = "Authentication failed. Please reconnect your GitHub account.";
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }
      
      toast({
        title: "Failed to load repositories",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setLoadingRepos(false);
    }
  };

  const handleAnalyze = async () => {
    if (!selectedRepo) return;

    const [owner, repo] = selectedRepo.fullName.split("/");
    setAnalyzingRepo(true);

    try {
      const files = await githubApi.analyzeRepository(owner, repo);
      const fileCount = Object.keys(files).length;

      if (fileCount === 0) {
        toast({
          title: "No Java files found",
          description: "This repository doesn't contain any Java files to review",
          variant: "destructive",
        });
        return;
      }

      onFilesLoaded(files, selectedRepo.fullName);

      toast({
        title: "Repository loaded",
        description: `Found ${fileCount} Java file(s) from ${selectedRepo.fullName}`,
      });
    } catch (error: any) {
      console.error("Error analyzing repository:", error);

      let errorMessage = "Please try again";
      if (error.response?.status === 403) {
        errorMessage = "Access denied. Make sure you have access to this repository.";
      } else if (error.response?.status === 404) {
        errorMessage = "Repository not found.";
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }

      toast({
        title: "Failed to analyze repository",
        description: errorMessage,
        variant: "destructive",
      });
    } finally {
      setAnalyzingRepo(false);
    }
  };

  if (!user?.githubConnected) {
    return (
      <div className="p-6 border border-dashed rounded-lg text-center">
        <Github className="h-10 w-10 mx-auto mb-3 text-muted-foreground" />
        <p className="text-sm font-medium mb-1">GitHub Not Connected</p>
        <p className="text-xs text-muted-foreground mb-4">
          Connect your GitHub account to select repositories
        </p>
        <Button
          variant="outline"
          size="sm"
          onClick={() => (window.location.href = "/dashboard/settings")}
        >
          Go to Settings
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label>Select GitHub Repository</Label>
        
        {loadingRepos ? (
          <div className="flex items-center justify-center p-8 border border-dashed rounded-lg">
            <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            <span className="ml-2 text-sm text-muted-foreground">Loading repositories...</span>
          </div>
        ) : repositories.length === 0 ? (
          <div className="p-6 border border-dashed rounded-lg text-center">
            <FolderGit2 className="h-8 w-8 mx-auto mb-2 text-muted-foreground" />
            <p className="text-sm text-muted-foreground mb-3">No repositories found</p>
            <Button variant="outline" size="sm" onClick={loadRepositories}>
              Refresh
            </Button>
          </div>
        ) : (
          <div className="space-y-2 relative" ref={dropdownRef}>
            {/* Selected Repository Display */}
            <button
              onClick={() => setIsDropdownOpen(!isDropdownOpen)}
              className="w-full flex items-center justify-between p-3 border rounded-lg hover:bg-accent transition-colors text-left"
            >
              {selectedRepo ? (
                <div className="flex-1 min-w-0">
                  <div className="font-medium truncate">{selectedRepo.fullName}</div>
                  {selectedRepo.description && (
                    <div className="text-xs text-muted-foreground truncate">
                      {selectedRepo.description}
                    </div>
                  )}
                  <div className="flex items-center gap-2 mt-1">
                    {selectedRepo.language && (
                      <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-600 dark:text-blue-400">
                        {selectedRepo.language}
                      </span>
                    )}
                    {selectedRepo.isPrivate && (
                      <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400">
                        Private
                      </span>
                    )}
                  </div>
                </div>
              ) : (
                <span className="text-muted-foreground">Choose a repository...</span>
              )}
              <ChevronDown
                className={`h-4 w-4 ml-2 transition-transform ${
                  isDropdownOpen ? "transform rotate-180" : ""
                }`}
              />
            </button>

            {/* Dropdown Menu */}
            {isDropdownOpen && (
              <Card className="absolute z-10 w-full max-w-2xl mt-1 max-h-96 overflow-hidden shadow-lg">
                <div className="p-2 border-b sticky top-0 bg-background">
                  <div className="relative">
                    <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search repositories..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="pl-8"
                    />
                  </div>
                </div>
                <div className="overflow-y-auto max-h-80">
                  {filteredRepos.length === 0 ? (
                    <div className="p-4 text-center text-sm text-muted-foreground">
                      No repositories match your search
                    </div>
                  ) : (
                    filteredRepos.map((repo) => (
                      <button
                        key={repo.id}
                        onClick={() => {
                          setSelectedRepo(repo);
                          setIsDropdownOpen(false);
                          setSearchQuery("");
                        }}
                        className="w-full p-3 hover:bg-accent transition-colors text-left border-b last:border-b-0"
                      >
                        <div className="font-medium">{repo.fullName}</div>
                        {repo.description && (
                          <div className="text-xs text-muted-foreground mt-0.5 line-clamp-1">
                            {repo.description}
                          </div>
                        )}
                        <div className="flex items-center gap-2 mt-1">
                          {repo.language && (
                            <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-600 dark:text-blue-400">
                              {repo.language}
                            </span>
                          )}
                          {repo.isPrivate && (
                            <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400">
                              Private
                            </span>
                          )}
                        </div>
                      </button>
                    ))
                  )}
                </div>
              </Card>
            )}

            {/* Analyze Button */}
            <Button
              onClick={handleAnalyze}
              disabled={!selectedRepo || analyzingRepo}
              className="w-full"
            >
              {analyzingRepo ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Analyzing Repository...
                </>
              ) : (
                <>
                  <Github className="mr-2 h-4 w-4" />
                  Analyze Repository
                </>
              )}
            </Button>
          </div>
        )}

        <p className="text-xs text-muted-foreground">
          Select a repository to automatically import all Java files for review
        </p>
      </div>
    </div>
  );
}
