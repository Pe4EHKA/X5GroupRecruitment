import { useQuery } from '@tanstack/react-query';
import api from '@/lib/api';
import { CandidateStatus } from '@/types';

// Query keys
export const candidateKeys = {
  all: ['candidate'] as const,
  status: (token: string) => [...candidateKeys.all, 'status', token] as const,
};

// Get candidate status by token
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
