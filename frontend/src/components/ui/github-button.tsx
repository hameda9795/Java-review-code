"use client";

import { Button } from "@/components/ui/button";
import { githubApi } from "@/lib/api/github";
import { Github } from "lucide-react";
import { useState } from "react";

interface GitHubConnectButtonProps {
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function GitHubConnectButton({ onSuccess, onError }: GitHubConnectButtonProps) {
  const [isConnecting, setIsConnecting] = useState(false);

  const handleConnect = async () => {
    try {
      setIsConnecting(true);
      // Get GitHub Client ID from environment variable
      const clientId = process.env.NEXT_PUBLIC_GITHUB_CLIENT_ID;
      
      if (!clientId) {
        throw new Error("GitHub Client ID not configured");
      }

      // Redirect to GitHub OAuth
      githubApi.initiateOAuth(clientId);
    } catch (error) {
      setIsConnecting(false);
      onError?.(error as Error);
    }
  };

  return (
    <Button
      onClick={handleConnect}
      disabled={isConnecting}
      className="gap-2"
    >
      <Github className="h-4 w-4" />
      {isConnecting ? "Connecting..." : "Connect GitHub"}
    </Button>
  );
}

interface GitHubDisconnectButtonProps {
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function GitHubDisconnectButton({ onSuccess, onError }: GitHubDisconnectButtonProps) {
  const [isDisconnecting, setIsDisconnecting] = useState(false);

  const handleDisconnect = async () => {
    if (!confirm("Are you sure you want to disconnect your GitHub account?")) {
      return;
    }

    try {
      setIsDisconnecting(true);
      await githubApi.disconnect();
      onSuccess?.();
    } catch (error) {
      onError?.(error as Error);
    } finally {
      setIsDisconnecting(false);
    }
  };

  return (
    <Button
      onClick={handleDisconnect}
      disabled={isDisconnecting}
      variant="outline"
      className="gap-2"
    >
      <Github className="h-4 w-4" />
      {isDisconnecting ? "Disconnecting..." : "Disconnect GitHub"}
    </Button>
  );
}
