"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { githubApi } from "@/lib/api/github";
import { useAuthStore } from "@/lib/store/authStore";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2, CheckCircle2, XCircle } from "lucide-react";

export default function GitHubCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, setAuth } = useAuthStore();
  const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
  const [message, setMessage] = useState("Connecting to GitHub...");

  useEffect(() => {
    const handleCallback = async () => {
      const code = searchParams.get("code");
      const error = searchParams.get("error");

      if (error) {
        setStatus("error");
        setMessage("GitHub authorization was denied or failed.");
        githubApi.clearLinkingFlow();
        setTimeout(() => router.push("/dashboard/settings"), 3000);
        return;
      }

      if (!code) {
        setStatus("error");
        setMessage("No authorization code received from GitHub.");
        githubApi.clearLinkingFlow();
        setTimeout(() => router.push("/dashboard/settings"), 3000);
        return;
      }

      try {
        // Check if this is a linking flow (user already logged in)
        const isLinking = githubApi.isLinkingFlow();
        
        if (isLinking && user) {
          // User is already logged in, link GitHub to their existing account
          setMessage("Linking GitHub to your account...");
          const response = await githubApi.linkAccount(code);
          
          githubApi.clearLinkingFlow();
          setStatus("success");
          setMessage("GitHub account linked successfully! Redirecting...");
          
          // Update the user's GitHub info in the store without changing their session
          setAuth(
            {
              ...user,
              githubUsername: response.githubUsername,
              githubConnected: response.githubConnected,
            },
            localStorage.getItem('token') || ''
          );
          
          setTimeout(() => router.push("/dashboard/settings"), 2000);
        } else {
          // New user logging in via GitHub
          setMessage("Exchanging authorization code...");
          const response = await githubApi.handleCallback(code);

          githubApi.clearLinkingFlow();
          
          // This is a new login, set the auth with the token from response
          if (response.token) {
            setAuth(
              {
                id: response.userId,
                username: response.username,
                email: response.email,
                subscriptionTier: response.subscriptionTier,
                githubUsername: response.githubUsername,
                githubConnected: response.githubConnected,
              },
              response.token
            );
            setStatus("success");
            setMessage("Successfully authenticated with GitHub! Redirecting to dashboard...");
            setTimeout(() => router.push("/dashboard"), 2000);
          } else {
            throw new Error("No token received from GitHub authentication");
          }
        }
      } catch (err) {
        console.error("GitHub callback error:", err);
        githubApi.clearLinkingFlow();
        setStatus("error");
        setMessage(
          err instanceof Error
            ? err.message
            : "Failed to connect GitHub account. Please try again."
        );
        setTimeout(() => router.push("/dashboard/settings"), 3000);
      }
    };

    handleCallback();
  }, [searchParams, router, setAuth, user]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            {status === "loading" && <Loader2 className="h-5 w-5 animate-spin" />}
            {status === "success" && <CheckCircle2 className="h-5 w-5 text-green-600" />}
            {status === "error" && <XCircle className="h-5 w-5 text-destructive" />}
            GitHub OAuth
          </CardTitle>
          <CardDescription>
            {status === "loading" && "Processing your GitHub authorization..."}
            {status === "success" && "Authorization successful"}
            {status === "error" && "Authorization failed"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">{message}</p>
        </CardContent>
      </Card>
    </div>
  );
}
