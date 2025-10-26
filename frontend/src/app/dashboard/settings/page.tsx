"use client";

import { useAuthStore } from "@/lib/store/authStore";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { GitHubConnectButton, GitHubDisconnectButton } from "@/components/ui/github-button";
import { useToast } from "@/components/ui/use-toast";
import { User, Crown, CheckCircle2 } from "lucide-react";
import { useState } from "react";

export default function SettingsPage() {
  const { user, setAuth } = useAuthStore();
  const { toast } = useToast();

  const handleGitHubSuccess = () => {
    // Update user in store to show GitHub connected
    if (user) {
      setAuth({ ...user, githubConnected: true }, useAuthStore.getState().token || '');
    }
    toast({
      title: "GitHub Connected",
      description: "Your GitHub account has been successfully connected.",
    });
  };

  const handleGitHubDisconnect = () => {
    // Update user in store to show GitHub disconnected
    if (user) {
      setAuth({ ...user, githubConnected: false, githubUsername: undefined }, useAuthStore.getState().token || '');
    }
    toast({
      title: "GitHub Disconnected",
      description: "Your GitHub account has been disconnected.",
    });
  };

  const handleError = (error: Error) => {
    toast({
      title: "Error",
      description: error.message,
      variant: "destructive",
    });
  };

  if (!user) return null;

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-3xl font-bold">Settings</h1>
        <p className="text-muted-foreground">Manage your account settings and preferences</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Account Information</CardTitle>
          <CardDescription>Your DevMentor AI account details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center gap-4">
            <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center">
              <User className="h-8 w-8 text-primary" />
            </div>
            <div>
              <h3 className="font-semibold text-lg">{user.username}</h3>
              <p className="text-sm text-muted-foreground">{user.email}</p>
            </div>
          </div>

          <div className="pt-4 border-t">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium">Subscription Tier</p>
                <p className="text-sm text-muted-foreground">
                  {user.subscriptionTier === "FREE"
                    ? "5 reviews per month"
                    : "100 reviews per month"}
                </p>
              </div>
              <Badge
                variant={user.subscriptionTier === "PREMIUM" ? "default" : "secondary"}
                className="text-sm px-3 py-1"
              >
                {user.subscriptionTier === "PREMIUM" && (
                  <Crown className="h-3 w-3 mr-1" />
                )}
                {user.subscriptionTier}
              </Badge>
            </div>
          </div>

          {user.subscriptionTier === "FREE" && (
            <div className="pt-4 border-t bg-primary/5 p-4 rounded-lg">
              <h4 className="font-semibold mb-2">Upgrade to Premium</h4>
              <p className="text-sm text-muted-foreground mb-3">
                Get 100 reviews per month, advanced AI analysis, GitHub integration, and priority
                support for just $29/month.
              </p>
              <p className="text-xs text-muted-foreground">
                Contact support to upgrade your account.
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>GitHub Integration</CardTitle>
          <CardDescription>Connect your GitHub account for repository analysis</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {user?.githubConnected ? (
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm text-green-600 dark:text-green-400">
                <CheckCircle2 className="h-4 w-4" />
                <span>GitHub account connected</span>
                {user.githubUsername && (
                  <span className="text-muted-foreground">(@{user.githubUsername})</span>
                )}
              </div>
              <p className="text-sm text-muted-foreground">
                You can now analyze repositories directly from GitHub. Use the review creation
                page to analyze entire repositories.
              </p>
              <GitHubDisconnectButton 
                onSuccess={handleGitHubDisconnect} 
                onError={handleError}
              />
            </div>
          ) : (
            <div className="space-y-3">
              <p className="text-sm text-muted-foreground">
                Connect your GitHub account to analyze entire repositories directly from GitHub.
                This will allow you to:
              </p>
              <ul className="text-sm text-muted-foreground list-disc list-inside space-y-1">
                <li>Browse and select your GitHub repositories</li>
                <li>Analyze entire repository codebases</li>
                <li>Get AI-powered reviews on all Java files</li>
                <li>Track repository changes over time</li>
              </ul>
              <GitHubConnectButton 
                onSuccess={handleGitHubSuccess} 
                onError={handleError}
              />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
