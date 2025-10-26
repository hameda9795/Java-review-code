import apiClient from './client';

export const githubApi = {
  /**
   * Initiate GitHub OAuth flow
   * Redirects user to GitHub for authorization
   */
  initiateOAuth: (clientId: string) => {
    const redirectUri = `${window.location.origin}/github/callback`;
    const scope = 'user:email read:user repo';
    const githubAuthUrl = `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent(scope)}`;
    window.location.href = githubAuthUrl;
  },

  /**
   * Exchange OAuth code for access token and authenticate user
   */
  handleCallback: async (code: string) => {
    // Don't use apiClient here to avoid auth headers
    const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
    const url = `${API_URL}/github/oauth/callback?code=${code}`;
    
    console.log('=== GitHub OAuth Callback ===');
    console.log('Sending POST to:', url);
    console.log('Code:', code.substring(0, 10) + '...');
    
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    
    console.log('Response status:', response.status);
    console.log('Response headers:', Object.fromEntries(response.headers.entries()));
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error response:', errorText);
      throw new Error(errorText || 'Failed to authenticate with GitHub');
    }
    
    const data = await response.json();
    console.log('Success! Received data:', { ...data, token: data.token ? '***' : undefined });
    return data;
  },

  /**
   * Get repository information
   */
  getRepository: async (owner: string, repo: string) => {
    const response = await apiClient.get(`/github/repos/${owner}/${repo}`);
    return response.data;
  },

  /**
   * Get user's repositories
   */
  getRepositories: async () => {
    const response = await apiClient.get('/github/repositories');
    return response.data;
  },

  /**
   * Analyze repository and get Java files
   */
  analyzeRepository: async (owner: string, repo: string) => {
    console.log('=== Analyze Repository ===');
    console.log('Owner:', owner);
    console.log('Repo:', repo);
    console.log('Token:', localStorage.getItem('token') ? 'Present' : 'Missing');
    console.log('UserId:', localStorage.getItem('userId'));
    
    const response = await apiClient.post(`/github/repos/${owner}/${repo}/analyze`);
    console.log('Response:', response.data);
    return response.data;
  },

  /**
   * Get specific files from repository
   */
  getFiles: async (owner: string, repo: string, files: string[]) => {
    const response = await apiClient.post(`/github/repos/${owner}/${repo}/files`, {
      files
    });
    return response.data;
  },

  /**
   * Disconnect GitHub account
   */
  disconnect: async () => {
    const response = await apiClient.delete('/github/disconnect');
    return response.data;
  }
};
