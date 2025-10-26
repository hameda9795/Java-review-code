import apiClient from './client';
import { CodeReview, CreateReviewRequest, ReviewStats } from '../types/review';

export const reviewsApi = {
  create: async (data: CreateReviewRequest): Promise<CodeReview> => {
    const response = await apiClient.post('/reviews', data);
    return response.data;
  },

  getById: async (id: string): Promise<CodeReview> => {
    const response = await apiClient.get(`/reviews/${id}`);
    return response.data;
  },

  list: async (): Promise<CodeReview[]> => {
    const response = await apiClient.get('/reviews');
    return response.data;
  },

  getRecent: async (limit: number = 5): Promise<CodeReview[]> => {
    const response = await apiClient.get(`/reviews/recent?limit=${limit}`);
    return response.data;
  },

  getStats: async (): Promise<ReviewStats> => {
    const response = await apiClient.get('/reviews/stats');
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/reviews/${id}`);
  },

  getSummary: async (id: string): Promise<string> => {
    const response = await apiClient.get(`/reviews/${id}/summary`);
    return response.data;
  },

  resolveFinding: async (reviewId: string, findingId: string): Promise<void> => {
    await apiClient.put(`/reviews/${reviewId}/findings/${findingId}/resolve`);
  },

  downloadMarkdown: async (id: string): Promise<void> => {
    const response = await apiClient.get(`/reviews/${id}/download/markdown`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `review_${id}.md`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  downloadHtml: async (id: string): Promise<void> => {
    const response = await apiClient.get(`/reviews/${id}/download/html`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `review_${id}.html`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  downloadCsv: async (id: string): Promise<void> => {
    const response = await apiClient.get(`/reviews/${id}/download/csv`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `review_findings_${id}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
