"use client";

import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Sparkles, Copy, Check, Download, AlertCircle } from 'lucide-react';
import { reviewsApi } from '@/lib/api/reviews';
import { GeneratedPrompt } from '@/lib/types/review';

interface AIPromptGeneratorProps {
  reviewId: string;
}

export function AIPromptGenerator({ reviewId }: AIPromptGeneratorProps) {
  const [prompt, setPrompt] = useState<GeneratedPrompt | null>(null);
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGenerate = async () => {
    setLoading(true);
    setError(null);
    try {
      const generated = await reviewsApi.generatePrompt(reviewId);
      setPrompt(generated);
    } catch (err) {
      setError('Failed to generate prompt. Please try again.');
      console.error('Error generating prompt:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    if (!prompt) return;
    
    try {
      await navigator.clipboard.writeText(prompt.prompt);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  const handleDownload = () => {
    if (!prompt) return;

    const blob = new Blob([prompt.prompt], { type: 'text/markdown' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `ai-fixing-prompt-${reviewId}.md`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };

  return (
    <Card className="border-purple-200 dark:border-purple-800">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-purple-600" />
            <CardTitle>AI Code Fixing Prompt</CardTitle>
          </div>
          <Badge variant="outline" className="bg-purple-50 text-purple-700 dark:bg-purple-950 dark:text-purple-300">
            Powered by Claude
          </Badge>
        </div>
        <CardDescription>
          Generate a comprehensive prompt to fix all code issues using AI assistants like Claude or ChatGPT
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-4">
        {!prompt && !error && (
          <Button
            onClick={handleGenerate}
            disabled={loading}
            className="w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700"
          >
            <Sparkles className="mr-2 h-4 w-4" />
            {loading ? 'Generating Prompt...' : 'Generate AI Fixing Prompt'}
          </Button>
        )}

        {error && (
          <div className="flex items-center gap-2 p-4 rounded-lg bg-red-50 dark:bg-red-950 text-red-700 dark:text-red-300">
            <AlertCircle className="h-5 w-5" />
            <p className="text-sm">{error}</p>
          </div>
        )}

        {prompt && (
          <div className="space-y-4">
            {/* Statistics */}
            <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
              <div className="text-center p-3 rounded-lg bg-gray-50 dark:bg-gray-800">
                <div className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  {prompt.totalFindings}
                </div>
                <div className="text-xs text-gray-600 dark:text-gray-400">Total Issues</div>
              </div>
              <div className="text-center p-3 rounded-lg bg-red-50 dark:bg-red-950">
                <div className="text-2xl font-bold text-red-600 dark:text-red-400">
                  {prompt.criticalCount}
                </div>
                <div className="text-xs text-red-600 dark:text-red-400">Critical</div>
              </div>
              <div className="text-center p-3 rounded-lg bg-orange-50 dark:bg-orange-950">
                <div className="text-2xl font-bold text-orange-600 dark:text-orange-400">
                  {prompt.highCount}
                </div>
                <div className="text-xs text-orange-600 dark:text-orange-400">High</div>
              </div>
              <div className="text-center p-3 rounded-lg bg-yellow-50 dark:bg-yellow-950">
                <div className="text-2xl font-bold text-yellow-600 dark:text-yellow-400">
                  {prompt.mediumCount}
                </div>
                <div className="text-xs text-yellow-600 dark:text-yellow-400">Medium</div>
              </div>
              <div className="text-center p-3 rounded-lg bg-green-50 dark:bg-green-950">
                <div className="text-2xl font-bold text-green-600 dark:text-green-400">
                  {prompt.lowCount}
                </div>
                <div className="text-xs text-green-600 dark:text-green-400">Low</div>
              </div>
            </div>

            {/* Instructions */}
            <div className="p-4 rounded-lg bg-blue-50 dark:bg-blue-950 border border-blue-200 dark:border-blue-800">
              <p className="text-sm text-blue-800 dark:text-blue-200">
                <strong>How to use:</strong> {prompt.instructions}
              </p>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2">
              <Button
                onClick={handleCopy}
                variant="default"
                className="flex-1"
              >
                {copied ? (
                  <>
                    <Check className="mr-2 h-4 w-4" />
                    Copied!
                  </>
                ) : (
                  <>
                    <Copy className="mr-2 h-4 w-4" />
                    Copy to Clipboard
                  </>
                )}
              </Button>
              <Button
                onClick={handleDownload}
                variant="outline"
              >
                <Download className="mr-2 h-4 w-4" />
                Download .md
              </Button>
            </div>

            {/* Prompt Preview */}
            <div className="relative">
              <div className="absolute top-2 right-2 z-10">
                <Badge variant="secondary" className="text-xs">
                  {prompt.prompt.length.toLocaleString()} characters
                </Badge>
              </div>
              <div className="max-h-96 overflow-y-auto rounded-lg border bg-gray-50 dark:bg-gray-900 p-4">
                <pre className="text-xs font-mono whitespace-pre-wrap text-gray-800 dark:text-gray-200">
                  {prompt.prompt}
                </pre>
              </div>
            </div>

            {/* Regenerate option */}
            <Button
              onClick={handleGenerate}
              variant="ghost"
              size="sm"
              className="w-full"
              disabled={loading}
            >
              <Sparkles className="mr-2 h-3 w-3" />
              Regenerate Prompt
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
