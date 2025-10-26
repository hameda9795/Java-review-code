"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import { reviewsApi } from "@/lib/api/reviews";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useToast } from "@/components/ui/use-toast";
import { GitHubRepoDropdown } from "@/components/ui/github-repo-dropdown";
import { Loader2, Upload, Github } from "lucide-react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

export default function NewReviewPage() {
  const router = useRouter();
  const { toast } = useToast();
  const [title, setTitle] = useState("");
  const [files, setFiles] = useState<Record<string, string>>({});
  const [fileName, setFileName] = useState("");
  const [fileContent, setFileContent] = useState("");

  const createReviewMutation = useMutation({
    mutationFn: (data: { title: string; files: Record<string, string> }) =>
      reviewsApi.create(data),
    onSuccess: (review) => {
      toast({
        title: "Review created",
        description: "Your code review has been started",
      });
      router.push(`/dashboard/reviews/${review.id}`);
    },
    onError: (error: any) => {
      toast({
        title: "Failed to create review",
        description: error.response?.data?.message || "Please try again",
        variant: "destructive",
      });
    },
  });

  const handleGitHubFilesLoaded = (gitHubFiles: Record<string, string>, repoName: string) => {
    setFiles(gitHubFiles);
    if (!title) {
      setTitle(`Review: ${repoName}`);
    }
    toast({
      title: "Repository loaded",
      description: `${Object.keys(gitHubFiles).length} Java files ready for review`,
    });
  };

  const handleAddFile = () => {
    if (fileName && fileContent) {
      setFiles({ ...files, [fileName]: fileContent });
      setFileName("");
      setFileContent("");
      toast({
        title: "File added",
        description: `${fileName} has been added to the review`,
      });
    }
  };

  const handleRemoveFile = (name: string) => {
    const newFiles = { ...files };
    delete newFiles[name];
    setFiles(newFiles);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const uploadedFiles = e.target.files;
    if (uploadedFiles) {
      Array.from(uploadedFiles).forEach((file) => {
        const reader = new FileReader();
        reader.onload = (event) => {
          const content = event.target?.result as string;
          setFiles((prev) => ({ ...prev, [file.name]: content }));
        };
        reader.readAsText(file);
      });
      toast({
        title: "Files uploaded",
        description: `${uploadedFiles.length} file(s) added`,
      });
    }
  };

  const handleSubmit = () => {
    if (!title.trim()) {
      toast({
        title: "Title required",
        description: "Please enter a review title",
        variant: "destructive",
      });
      return;
    }

    if (Object.keys(files).length === 0) {
      toast({
        title: "No files",
        description: "Please add at least one file to review",
        variant: "destructive",
      });
      return;
    }

    createReviewMutation.mutate({ title, files });
  };

  return (
    <div className="max-w-4xl space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Create New Review</h1>
        <p className="text-muted-foreground">Upload your code files for AI-powered analysis</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Review Details</CardTitle>
          <CardDescription>Provide a title and add your code files</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label htmlFor="title">Review Title</Label>
            <Input
              id="title"
              placeholder="e.g., Review Spring Boot REST API"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div className="space-y-4">
            <Label>Add Files</Label>
            <Tabs defaultValue="github" className="w-full">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="github">
                  <Github className="h-4 w-4 mr-2" />
                  GitHub Repository
                </TabsTrigger>
                <TabsTrigger value="manual">
                  <Upload className="h-4 w-4 mr-2" />
                  Manual Upload
                </TabsTrigger>
              </TabsList>

              <TabsContent value="github" className="space-y-4">
                <GitHubRepoDropdown onFilesLoaded={handleGitHubFilesLoaded} />
              </TabsContent>

              <TabsContent value="manual" className="space-y-4">
                <div className="border-2 border-dashed rounded-lg p-6 text-center">
                  <Upload className="h-8 w-8 text-muted-foreground mx-auto mb-2" />
                  <p className="text-sm text-muted-foreground mb-4">
                    Upload .java files or paste code manually
                  </p>
                  <Input
                    type="file"
                    accept=".java"
                    multiple
                    onChange={handleFileUpload}
                    className="max-w-xs mx-auto"
                  />
                </div>

                <div className="space-y-4">
                  <Label>Or Add File Manually</Label>
                  <div className="space-y-2">
                    <Input
                      placeholder="File name (e.g., UserController.java)"
                      value={fileName}
                      onChange={(e) => setFileName(e.target.value)}
                    />
                      <textarea
                      className="w-full min-h-[200px] p-3 rounded-md border border-input bg-background text-sm font-mono"
                      placeholder="Paste your code here..."
                      value={fileContent}
                      onChange={(e) => setFileContent(e.target.value)}
                    />
                    <Button onClick={handleAddFile} variant="outline" className="w-full">
                      Add File
                    </Button>
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </div>

          {Object.keys(files).length > 0 && (
            <div className="space-y-2">
              <Label>Files to Review ({Object.keys(files).length})</Label>
              <div className="border rounded-lg divide-y">
                {Object.entries(files).map(([name, content]) => (
                  <div key={name} className="p-3 flex items-center justify-between">
                    <div>
                      <p className="font-medium text-sm">{name}</p>
                      <p className="text-xs text-muted-foreground">
                        {content.split("\n").length} lines
                      </p>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleRemoveFile(name)}
                    >
                      Remove
                    </Button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <Button
            onClick={handleSubmit}
            disabled={createReviewMutation.isPending}
            className="w-full"
            size="lg"
          >
            {createReviewMutation.isPending && (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            )}
            {createReviewMutation.isPending ? "Creating Review..." : "Create Review"}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
