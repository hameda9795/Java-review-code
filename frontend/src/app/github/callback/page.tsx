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
  const { setAuth } = useAuthStore();
  const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
  const [message, setMessage] = useState("Connecting to GitHub...");

  useEffect(() => {
    const handleCallback = async () => {
      const code = searchParams.get("code");
      const error = searchParams.get("error");

      if (error) {
        setStatus("error");
        setMessage("GitHub authorization was denied or failed.");
        setTimeout(() => router.push("/dashboard/settings"), 3000);
        return;
      }

      if (!code) {
        setStatus("error");
        setMessage("No authorization code received from GitHub.");
        setTimeout(() => router.push("/dashboard/settings"), 3000);
        return;
      }

      try {
        setMessage("Exchanging authorization code...");
        const response = await githubApi.handleCallback(code);

        // If the response contains a token, this is a new user logging in via GitHub
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
          // Existing user connected GitHub account
          setStatus("success");
          setMessage("GitHub account connected successfully! Redirecting...");
          setTimeout(() => router.push("/dashboard/settings"), 2000);
        }
      } catch (err) {
        console.error("GitHub callback error:", err);
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
  }, [searchParams, router, setAuth]);

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
