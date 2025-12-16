import { useQuery, UseQueryResult } from '@tantml:query';
import api from '@/lib/api';
import { ApplicationDetail, CandidateStatus, StatusHistory } from '@/types';

// Query keys
export const candidateKeys = {
  all: ['candidate'] as const,
  status: (token: string) => [...candidateKeys.all, 'status', token] as const,
  applications: () => [...candidateKeys.all, 'applications'] as const,
  application: (id: number) => [...candidateKeys.all, 'application', id] as const,
  history: (id: number) => [...candidateKeys.all, 'history', id] as const,
};

// Get candidate status by token (public, no auth)
export function useCandidateStatus(token: string) {
  return useQuery({
    queryKey: candidateKeys.status(token),
    queryFn: async () => {
      const response = await api.get<CandidateStatus>(`/api/candidate/status?token=${token}`);
      return response.data;
    },
    enabled: !!token,
  });
}

/**
 * Hook to fetch all applications for the authenticated candidate
 */
export function useCandidateApplications(): UseQueryResult<ApplicationDetail[]> {
  return useQuery({
    queryKey: candidateKeys.applications(),
    queryFn: async () => {
      const response = await api.get('/api/candidate/me/applications');
      return response.data;
    },
  });
}

/**
 * Hook to fetch a specific application for the authenticated candidate
 */
export function useCandidateApplication(id: number): UseQueryResult<ApplicationDetail> {
  return useQuery({
    queryKey: candidateKeys.application(id),
    queryFn: async () => {
      const response = await api.get(`/api/candidate/me/applications/${id}`);
      return response.data;
    },
    enabled: !!id,
  });
}

/**
 * Hook to fetch status history for a specific application
 */
export function useCandidateStatusHistory(id: number): UseQueryResult<StatusHistory[]> {
  return useQuery({
    queryKey: candidateKeys.history(id),
    queryFn: async () => {
      const response = await api.get(`/api/candidate/me/applications/${id}/history`);
      return response.data;
    },
    enabled: !!id,
  });
}
